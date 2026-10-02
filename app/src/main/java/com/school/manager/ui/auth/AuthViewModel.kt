package com.school.manager.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.data.prefs.UserPreferences
import com.school.manager.util.AnalyticsHelper
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.SessionManager
import com.school.manager.util.UserRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false,
    val userRole: String? = null,
    val resetEmailSent: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val analytics: AnalyticsHelper,
    private val prefs: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val rememberMe: StateFlow<Boolean> = prefs.rememberMe
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val savedEmail: StateFlow<String> = prefs.savedEmail
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val savedPassword: StateFlow<String> = prefs.savedPassword
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun login(email: String, password: String, remember: Boolean) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Please enter email and password") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
                val uid = result.user?.uid ?: throw Exception("Authentication failed")

                val doc = firestore.collection(FirestoreCollections.USERS)
                    .document(uid).get().await()

                val status = doc.getString("status") ?: "approved"
                if (status == "pending") {
                    auth.signOut()
                    _uiState.update {
                        it.copy(isLoading = false, error = "Your account is awaiting admin approval")
                    }
                    return@launch
                }
                if (status == "rejected") {
                    auth.signOut()
                    _uiState.update {
                        it.copy(isLoading = false, error = "Your account request was declined")
                    }
                    return@launch
                }

                val role = doc.getString("role") ?: UserRoles.STUDENT

                val schoolId = doc.getString("schoolId") ?: ""
                val name = doc.getString("name") ?: ""
                SessionManager.set(
                    SessionManager.Session(
                        uid = uid,
                        name = name,
                        email = email,
                        role = role,
                        schoolId = schoolId,
                        isOwner = role == "owner"
                    )
                )
                analytics.logLogin(role)

                if (remember) prefs.saveCredentials(email.trim(), password)
                else prefs.clearCredentials()

                _uiState.update {
                    it.copy(isLoading = false, loginSuccess = true, userRole = role)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = friendly(e.message)) }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Enter your email first") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null, resetEmailSent = false) }
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email.trim()).await()
                _uiState.update { it.copy(isLoading = false, resetEmailSent = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = friendly(e.message)) }
            }
        }
    }

    fun resetLoginState() {
        _uiState.update { it.copy(loginSuccess = false, userRole = null, resetEmailSent = false) }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun friendly(msg: String?): String {
        val m = msg ?: return "Something went wrong. Try again."
        return when {
            m.contains("password is invalid", true) ||
            m.contains("credential is incorrect", true) ||
            m.contains("INVALID_LOGIN_CREDENTIALS", true) ||
            m.contains("no user record", true) ||
            m.contains("user not found", true) -> "Invalid Email or Password"
            m.contains("badly formatted", true) -> "Please enter a valid email address"
            m.contains("network", true) -> "No internet connection. Check your network."
            m.contains("too many requests", true) -> "Too many attempts. Try again later."
            m.contains("blocked", true) -> "Account temporarily blocked. Try later."
            else -> "Invalid Email or Password"
        }
    }
}
