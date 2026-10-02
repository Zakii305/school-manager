package com.school.manager.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.UserRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ChatPartner(val uid: String, val name: String, val role: String)

data class ChatSummary(
    val chatId: String,
    val partnerName: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val partnerUid: String
) {
    fun timeText(): String =
        SimpleDateFormat("dd MMM hh:mm a", Locale.US).format(Date(lastTimestamp))
}

data class Message(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long
) {
    fun timeText(): String =
        SimpleDateFormat("hh:mm a", Locale.US).format(Date(timestamp))
}

data class ChatListUiState(
    val isLoading: Boolean = true,
    val chats: List<ChatSummary> = emptyList(),
    val partners: List<ChatPartner> = emptyList(),
    val isParent: Boolean = false,
    val error: String? = null
)

data class ChatRoomUiState(
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val partnerName: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _listState = MutableStateFlow(ChatListUiState())
    val listState: StateFlow<ChatListUiState> = _listState.asStateFlow()

    private val _roomState = MutableStateFlow(ChatRoomUiState())
    val roomState: StateFlow<ChatRoomUiState> = _roomState.asStateFlow()

    /** Loads chat list. For parents: their existing chats with teachers.
     *  For teachers: existing chats + list of parents to start new chat with. */
    fun loadChats() {
        val uid = auth.currentUser?.uid ?: return
        _listState.value = _listState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val meDoc = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
                val isParent = meDoc.getString("role") == UserRoles.PARENT

                // existing chats where I'm a participant
                val chatSnap = firestore.collection(FirestoreCollections.CHATS)
                    .whereArrayContains("participants", uid)
                    .get().await()

                val chats = chatSnap.documents.map { d ->
                    val participants: List<String> = (d.get("participants") as? List<String>) ?: emptyList<String>()
                    val partnerUid = participants.firstOrNull { it != uid } ?: ""
                    val names: Map<String, String> = (d.get("participantNames") as? Map<String, String>) ?: emptyMap()
                    ChatSummary(
                        chatId = d.id,
                        partnerName = names[partnerUid] ?: "User",
                        lastMessage = d.getString("lastMessage") ?: "",
                        lastTimestamp = d.getLong("lastTimestamp") ?: 0L,
                        partnerUid = partnerUid
                    )
                }.sortedByDescending { it.lastTimestamp }

                // if teacher/admin: get list of parents to start new chat
                val partners = if (!isParent) {
                    val pSnap = firestore.collection(FirestoreCollections.USERS)
                        .whereEqualTo("role", UserRoles.PARENT).get().await()
                    pSnap.documents.map {
                        ChatPartner(
                            uid = it.id,
                            name = it.getString("name") ?: "-",
                            role = it.getString("role") ?: ""
                        )
                    }
                } else emptyList()

                _listState.value = ChatListUiState(
                    isLoading = false,
                    chats = chats,
                    partners = partners,
                    isParent = isParent
                )
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /** Create (or reuse) a chat with a partner. Returns chatId via callback. */
    fun startChatWith(partner: ChatPartner, onReady: (String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val meDoc = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
                val myName = meDoc.getString("name") ?: "Me"

                // Check if a chat already exists
                val existing = firestore.collection(FirestoreCollections.CHATS)
                    .whereArrayContains("participants", uid).get().await()
                val found = existing.documents.firstOrNull { d ->
                    val parts: List<String> = (d.get("participants") as? List<String>) ?: emptyList<String>()
                    parts.contains(partner.uid) && parts.size == 2
                }
                if (found != null) {
                    onReady(found.id)
                    return@launch
                }

                val data = mapOf(
                    "participants" to listOf(uid, partner.uid),
                    "participantNames" to mapOf(uid to myName, partner.uid to partner.name),
                    "lastMessage" to "",
                    "lastTimestamp" to System.currentTimeMillis(),
                    "createdAt" to System.currentTimeMillis()
                )
                val ref = firestore.collection(FirestoreCollections.CHATS).add(data).await()
                onReady(ref.id)
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(error = e.message)
            }
        }
    }

    /** Real-time listener on messages in a chat. */
    fun listenToMessages(chatId: String) {
        _roomState.value = _roomState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val chatDoc = firestore.collection(FirestoreCollections.CHATS).document(chatId).get().await()
                val uid = auth.currentUser?.uid ?: ""
                val names: Map<String, String> = (chatDoc.get("participantNames") as? Map<String, String>) ?: emptyMap()
                val participants: List<String> = (chatDoc.get("participants") as? List<String>) ?: emptyList<String>()
                val partnerName = names[participants.firstOrNull { it != uid }] ?: "Chat"
                _roomState.value = _roomState.value.copy(partnerName = partnerName, isLoading = false)
            } catch (_: Exception) {}

            firestore.collection(FirestoreCollections.CHATS)
                .document(chatId)
                .collection(FirestoreCollections.MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snap, err ->
                    if (err != null) {
                        _roomState.value = _roomState.value.copy(error = err.message)
                        return@addSnapshotListener
                    }
                    val msgs = snap?.documents?.map { d ->
                        Message(
                            id = d.id,
                            senderId = d.getString("senderId") ?: "",
                            senderName = d.getString("senderName") ?: "",
                            text = d.getString("text") ?: "",
                            timestamp = d.getLong("timestamp") ?: 0L
                        )
                    } ?: emptyList<Message>()
                    _roomState.value = _roomState.value.copy(messages = msgs, isLoading = false)
                }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        val uid = auth.currentUser?.uid ?: return
        val myName = auth.currentUser?.email?.substringBefore("@") ?: "Me"
        _roomState.value = _roomState.value.copy(isSending = true)

        viewModelScope.launch {
            try {
                val msgRef = firestore.collection(FirestoreCollections.CHATS)
                    .document(chatId).collection(FirestoreCollections.MESSAGES).document()

                val msgData = mapOf(
                    "senderId" to uid,
                    "senderName" to myName,
                    "text" to text.trim(),
                    "timestamp" to System.currentTimeMillis()
                )
                msgRef.set(msgData).await()

                firestore.collection(FirestoreCollections.CHATS).document(chatId)
                    .update(mapOf(
                        "lastMessage" to text.trim().take(80),
                        "lastTimestamp" to System.currentTimeMillis()
)).await()

                _roomState.value = _roomState.value.copy(isSending = false)
            } catch (e: Exception) {
                _roomState.value = _roomState.value.copy(isSending = false, error = e.message)
            }
        }
    }
}
