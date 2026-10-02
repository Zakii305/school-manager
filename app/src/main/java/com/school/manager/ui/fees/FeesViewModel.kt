package com.school.manager.ui.fees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.UserRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class FeeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val amount: Double,
    val paid: Boolean,
    val description: String
)

data class FeesUiState(
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val fees: List<FeeRecord> = emptyList(),
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class FeesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeesUiState())
    val uiState: StateFlow<FeesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val meDoc = firestore.collection(FirestoreCollections.USERS)
                    .document(uid).get().await()
                val isAdmin = meDoc.getString("role") == UserRoles.ADMIN

                val snap = firestore.collection(FirestoreCollections.FEES).get().await()
                val all = snap.documents.mapNotNull { d ->
                    val sid = d.getString("studentId") ?: return@mapNotNull null
                    FeeRecord(
                        id = d.id,
                        studentId = sid,
                        studentName = d.getString("studentName") ?: "Unknown",
                        amount = d.getDouble("amount") ?: 0.0,
                        paid = d.getBoolean("paid") ?: false,
                        description = d.getString("description") ?: "Tuition Fee"
                    )
                }

                val visible = if (isAdmin) all else all.filter { it.studentId == uid }
                val collected = visible.filter { it.paid }.sumOf { it.amount }
                val pending = visible.filter { !it.paid }.sumOf { it.amount }

                _uiState.value = FeesUiState(
                    isLoading = false,
                    isAdmin = isAdmin,
                    fees = visible.sortedByDescending { !it.paid },
                    totalCollected = collected,
                    totalPending = pending
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Load failed"
                )
            }
        }
    }

    fun markPaid(feeId: String) {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.FEES)
                    .document(feeId)
                    .update("paid", true, "paidAt", System.currentTimeMillis())
                    .await()
                _uiState.value = _uiState.value.copy(successMessage = "Marked as paid ✅")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Update failed")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
