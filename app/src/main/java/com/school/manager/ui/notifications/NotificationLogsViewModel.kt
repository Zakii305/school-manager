package com.school.manager.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class NotificationLog(
    val id: String,
    val event: String,
    val recipient: String,
    val subject: String,
    val emailStatus: String,
    val whatsappStatus: String,
    val sentAt: Long
) {
    fun timeText(): String =
        SimpleDateFormat("dd MMM HH:mm", Locale.US).format(Date(sentAt))
}

data class NotificationLogsState(
    val isLoading: Boolean = true,
    val logs: List<NotificationLog> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class NotificationLogsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(NotificationLogsState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val snap = firestore.collection("notification_logs").get().await()
                val logs = snap.documents.map { d ->
                    NotificationLog(
                        id = d.id,
                        event = d.getString("event") ?: "-",
                        recipient = d.getString("recipient") ?: "-",
                        subject = d.getString("subject") ?: "",
                        emailStatus = d.getString("emailStatus") ?: "—",
                        whatsappStatus = d.getString("whatsappStatus") ?: "—",
                        sentAt = d.getLong("sentAt") ?: 0L
                    )
                }.sortedByDescending { it.sentAt }
                _ui.update { it.copy(isLoading = false, logs = logs) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun reload() = load()
}
