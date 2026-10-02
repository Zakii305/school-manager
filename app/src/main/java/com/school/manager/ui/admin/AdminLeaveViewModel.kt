package com.school.manager.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AdminLeaveRow(
    val id: String,
    val teacherName: String,
    val fromDate: Long,
    val toDate: Long,
    val reason: String,
    val status: String
) {
    fun fromText() = SimpleDateFormat("dd MMM", Locale.US).format(Date(fromDate))
    fun toText() = SimpleDateFormat("dd MMM", Locale.US).format(Date(toDate))
}

data class AdminLeaveUiState(
    val isLoading: Boolean = true,
    val rows: List<AdminLeaveRow> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminLeaveViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _ui = MutableStateFlow(AdminLeaveUiState())
    val uiState: StateFlow<AdminLeaveUiState> = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val snap = firestore.collection("leave_requests").get().await()
                val rows = snap.documents.map { d ->
                    AdminLeaveRow(
                        id = d.id,
                        teacherName = d.getString("teacherName") ?: "Teacher",
                        fromDate = d.getLong("fromDate") ?: 0L,
                        toDate = d.getLong("toDate") ?: 0L,
                        reason = d.getString("reason") ?: "-",
                        status = d.getString("status") ?: "pending"
                    )
                }.sortedByDescending { it.fromDate }
                _ui.update { it.copy(isLoading = false, rows = rows) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun approve(id: String) = updateStatus(id, "approved")
    fun reject(id: String) = updateStatus(id, "rejected")

    private fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            try {
                firestore.collection("leave_requests").document(id)
                    .update("status", status).await()
                _ui.update { it.copy(successMessage = "Leave $status") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
