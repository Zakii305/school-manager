package com.school.manager.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

data class LeaveRow(
    val id: String,
    val fromDate: Long,
    val toDate: Long,
    val reason: String,
    val status: String
) {
    fun fromText() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(fromDate))
    fun toText() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(toDate))
}

data class LeaveUiState(
    val isLoading: Boolean = true,
    val rows: List<LeaveRow> = emptyList(),
    val fromDate: Long = System.currentTimeMillis(),
    val toDate: Long = System.currentTimeMillis(),
    val reason: String = "",
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class LeaveRequestViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _ui = MutableStateFlow(LeaveUiState())
    val uiState: StateFlow<LeaveUiState> = _ui.asStateFlow()

    init { load() }

    fun load() {
        val uid = auth.currentUser?.uid ?: return
        _ui.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val snap = firestore.collection("leave_requests")
                    .whereEqualTo("teacherId", uid).get().await()
                val rows = snap.documents.map { d ->
                    LeaveRow(
                        id = d.id,
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

    fun onFrom(millis: Long) = _ui.update { it.copy(fromDate = millis) }
    fun onTo(millis: Long) = _ui.update { it.copy(toDate = millis) }
    fun onReason(v: String) = _ui.update { it.copy(reason = v) }

    fun submit() {
        val s = _ui.value
        if (s.reason.isBlank()) {
            _ui.update { it.copy(error = "Reason is required") }
            return
        }
        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: ""
                val name = auth.currentUser?.email?.substringBefore("@") ?: "Teacher"
                firestore.collection("leave_requests").add(
                    mapOf(
                        "teacherId" to uid,
                        "teacherName" to name,
                        "fromDate" to s.fromDate,
                        "toDate" to s.toDate,
                        "reason" to s.reason.trim(),
                        "status" to "pending",
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()
                _ui.update {
                    it.copy(isSaving = false, reason = "",
                        successMessage = "Leave request submitted")
                }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
