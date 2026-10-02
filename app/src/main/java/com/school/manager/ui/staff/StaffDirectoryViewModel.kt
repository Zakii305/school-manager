package com.school.manager.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.SessionManager
import com.school.manager.util.UserRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class StaffMember(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val avatarUrl: String
)

data class StaffDirectoryUiState(
    val isLoading: Boolean = true,
    val all: List<StaffMember> = emptyList(),
    val filtered: List<StaffMember> = emptyList(),
    val query: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class StaffDirectoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffDirectoryUiState())
    val uiState = _uiState.asStateFlow()

    // Legacy aliases so existing screens don't break
    val teachers: List<StaffMember> get() = _uiState.value.all
    val admins: List<StaffMember> get() = emptyList()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.USERS).get().await()
                val all = snap.documents.mapNotNull { d ->
                    val role = d.getString("role") ?: return@mapNotNull null
                    if (role != UserRoles.TEACHER && role != UserRoles.ADMIN &&
                        role != UserRoles.OWNER) return@mapNotNull null
                    StaffMember(
                        uid = d.id,
                        name = d.getString("name") ?: "-",
                        email = d.getString("email") ?: "",
                        phone = d.getString("phone") ?: "",
                        role = role,
                        avatarUrl = d.getString("avatarUrl") ?: ""
                    )
                }.sortedBy { it.name.lowercase() }
                _uiState.value = _uiState.value.copy(
                    isLoading = false, all = all, filtered = all
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onQuery(q: String) {
        val s = _uiState.value
        val f = if (q.isBlank()) s.all else s.all.filter {
            it.name.contains(q, true) || it.email.contains(q, true) || it.phone.contains(q, true)
        }
        _uiState.value = s.copy(query = q, filtered = f)
    }

    fun addStaff(name: String, email: String, phone: String, role: String) {
        if (name.isBlank() || email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Name and email required")
            return
        }
        _uiState.value = _uiState.value.copy(isSaving = true, error = null, successMessage = null)
        viewModelScope.launch {
            try {
                val sid = SessionManager.current.schoolId
                val data = mutableMapOf<String, Any>(
                    "name" to name.trim(),
                    "email" to email.trim(),
                    "phone" to phone.trim(),
                    "role" to role,
                    "status" to "approved",
                    "createdAt" to System.currentTimeMillis()
                )
                if (sid.isNotBlank()) data["schoolId"] = sid
                firestore.collection(FirestoreCollections.USERS).add(data).await()
                _uiState.value = _uiState.value.copy(
                    isSaving = false, successMessage = "Staff added ✅"
                )
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun deleteStaff(uid: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.USERS).document(uid).delete().await()
                _uiState.value = _uiState.value.copy(successMessage = "Deleted")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
