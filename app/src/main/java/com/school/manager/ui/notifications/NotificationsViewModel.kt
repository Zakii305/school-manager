package com.school.manager.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.school.manager.util.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val authorName: String
) {
    fun timeAgo(): String {
        val diff = System.currentTimeMillis() - createdAt
        val min = diff / 60000
        val hour = diff / 3600000
        val day = diff / 86400000
        return when {
            min < 1 -> "Just now"
            min < 60 -> "$min min ago"
            hour < 24 -> "$hour hr ago"
            day < 7 -> "$day d ago"
            else -> SimpleDateFormat("dd MMM", Locale.US).format(Date(createdAt))
        }
    }
}

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val items: List<NotificationItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { listen() }

    fun reload() = listen()

    private fun listen() {
        firestore.collection(FirestoreCollections.NOTICES)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message)
                    return@addSnapshotListener
                }
                val items = snap?.documents?.map { d ->
                    NotificationItem(
                        id = d.id,
                        title = d.getString("title") ?: "-",
                        body = d.getString("body") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L,
                        authorName = d.getString("authorName") ?: "Admin"
                    )
                } ?: emptyList()
                _uiState.value = NotificationsUiState(isLoading = false, items = items)
            }
    }
}
