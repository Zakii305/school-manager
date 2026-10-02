package com.school.manager.ui.notice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.AnalyticsHelper
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

data class Notice(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val createdBy: String = "",
    val authorName: String = "Admin"
) {
    fun formattedDate(): String =
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(createdAt))
}

data class NoticeUiState(
    val isLoading: Boolean = true,
    val notices: List<Notice> = emptyList(),
    val isAdmin: Boolean = false,
    val isPosting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val analytics: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoticeUiState())
    val uiState: StateFlow<NoticeUiState> = _uiState.asStateFlow()

    init { loadNotices() }

    fun loadNotices() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid
                var isAdmin = false
                if (uid != null) {
                    val userDoc = firestore.collection(FirestoreCollections.USERS)
                        .document(uid).get().await()
                    isAdmin = userDoc.getString("role") == UserRoles.ADMIN
                }
                val snap = firestore.collection(FirestoreCollections.NOTICES).get().await()
                val list = snap.documents.mapNotNull { d ->
                    val title = d.getString("title") ?: return@mapNotNull null
                    Notice(
                        id = d.id,
                        title = title,
                        body = d.getString("body") ?: "",
                        createdAt = d.getLong("createdAt") ?: 0L,
                        createdBy = d.getString("createdBy") ?: "",
                        authorName = d.getString("authorName") ?: "Admin"
                    )
                }.sortedByDescending { it.createdAt }

                _uiState.value = _uiState.value.copy(
                    isLoading = false, notices = list, isAdmin = isAdmin
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Load failed"
                )
            }
        }
    }

    fun postNotice(title: String, body: String) {
        if (title.isBlank() || body.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Title and body required")
            return
        }
        _uiState.value = _uiState.value.copy(isPosting = true, error = null,
            successMessage = null)
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: throw Exception("Not signed in")
                val authorName = auth.currentUser?.email?.substringBefore("@") ?: "Admin"
                val data = mapOf(
                    "title" to title.trim(),
                    "body" to body.trim(),
                    "createdAt" to System.currentTimeMillis(),
                    "createdBy" to uid,
                    "authorName" to authorName
                )
                firestore.collection(FirestoreCollections.NOTICES).add(data).await()
                analytics.logNoticeCreated(title)
                _uiState.value = _uiState.value.copy(
                    isPosting = false, successMessage = "Notice published ✅"
                )
                loadNotices()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPosting = false, error = e.message ?: "Publish failed"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
