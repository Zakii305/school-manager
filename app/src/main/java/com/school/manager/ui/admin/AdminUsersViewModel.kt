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

data class ManagedUser(
    val uid: String,
    val name: String,
    val email: String,
    val role: String,
    val classId: String = "",
    val fcmToken: String = ""
)

data class AdminUsersUiState(
    val isLoading: Boolean = true,
    val users: List<ManagedUser> = emptyList(),
    val filtered: List<ManagedUser> = emptyList(),
    val filterRole: String = "all",
    val query: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.USERS)
                    .limit(200)
                    .get().await()
                val list = snap.documents.map { d ->
                    ManagedUser(
                        uid = d.id,
                        name = d.getString("name") ?: "-",
                        email = d.getString("email") ?: "-",
                        role = d.getString("role") ?: "student",
                        classId = d.getString("classId") ?: "",
                        fcmToken = d.getString("fcmToken") ?: ""
                    )
                }.sortedBy { it.name.lowercase() }
                _uiState.value = _uiState.value.copy(isLoading = false, users = list)
                applyFilter()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setFilter(role: String) {
        _uiState.value = _uiState.value.copy(filterRole = role)
        applyFilter()
    }

    fun setQuery(q: String) {
        _uiState.value = _uiState.value.copy(query = q)
        applyFilter()
    }

    private fun applyFilter() {
        val s = _uiState.value
        val filtered = s.users.filter { u ->
            (s.filterRole == "all" || u.role == s.filterRole) &&
            (s.query.isBlank() ||
                u.name.contains(s.query, true) ||
                u.email.contains(s.query, true))
        }
        _uiState.value = s.copy(filtered = filtered)
    }

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.USERS).document(uid).delete().await()
                _uiState.value = _uiState.value.copy(successMessage = "User deleted")
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
