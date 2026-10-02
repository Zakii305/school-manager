package com.school.manager.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class PendingUser(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val createdAt: Long
)

data class PendingUsersUiState(
    val isLoading: Boolean = true,
    val pending: List<PendingUser> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class PendingUsersViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PendingUsersUiState())
    val uiState: StateFlow<PendingUsersUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo("status", "pending").get().await()
                val list = snap.documents.map { d ->
                    PendingUser(
                        uid = d.id,
                        name = d.getString("name") ?: "-",
                        email = d.getString("email") ?: "-",
                        phone = d.getString("phone") ?: "",
                        role = d.getString("role") ?: "-",
                        createdAt = d.getLong("createdAt") ?: 0L
                    )
                }.sortedByDescending { it.createdAt }
                _uiState.value = PendingUsersUiState(isLoading = false, pending = list)
            } catch (e: Exception) {
                _uiState.value = PendingUsersUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun approve(uid: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.USERS).document(uid)
                    .update("status", "approved").await()
                _uiState.value = _uiState.value.copy(successMessage = "User approved ✅")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun reject(uid: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.USERS).document(uid)
                    .update("status", "rejected").await()
                _uiState.value = _uiState.value.copy(successMessage = "User rejected")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() = _uiState.value.let {
        _uiState.value = it.copy(error = null, successMessage = null)
    }
}
