package com.school.manager.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SignupUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val role: String = "student",
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onName(v: String) = _uiState.update { it.copy(name = v) }
    fun onEmail(v: String) = _uiState.update { it.copy(email = v) }
    fun onPhone(v: String) = _uiState.update { it.copy(phone = v) }
    fun onPassword(v: String) = _uiState.update { it.copy(password = v) }
    fun onRole(v: String) = _uiState.update { it.copy(role = v) }

    fun clearSubmitted() = _uiState.update { it.copy(submitted = false) }

    fun submit() {
        val s = _uiState.value
        if (s.name.isBlank() || s.email.isBlank() || s.password.length < 6) {
            _uiState.update {
                it.copy(error = "Name, email and a 6+ char password are required")
            }
            return
        }
        _uiState.update { it.copy(isSubmitting = true, error = null) }

        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(
                    s.email.trim(), s.password
                ).await()
                val uid = result.user?.uid ?: throw Exception("Signup failed")

                // Write user document with status = "pending"
                val data = mapOf(
                    "name" to s.name.trim(),
                    "email" to s.email.trim(),
                    "phone" to s.phone.trim(),
                    "role" to s.role,
                    "status" to "pending",
                    "createdAt" to System.currentTimeMillis(),
                    "avatarUrl" to "",
                    "classId" to "",
                    "rollNo" to ""
                )
                firestore.collection(FirestoreCollections.USERS)
                    .document(uid).set(data).await()

                // Sign out immediately — pending users can't enter
                auth.signOut()

                _uiState.update { it.copy(isSubmitting = false, submitted = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message?.take(140) ?: "Signup failed"
                    )
                }
            }
        }
    }
}
