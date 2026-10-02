package com.school.manager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.school.manager.data.prefs.UserPreferences
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

data class PasswordChangeState(
    val current: String = "",
    val newPass: String = "",
    val confirm: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
    private val auth: FirebaseAuth
) : ViewModel() {

    val followSystem: StateFlow<Boolean> = prefs.followSystem
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val darkMode: StateFlow<Boolean> = prefs.darkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val appTheme: StateFlow<String> = prefs.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, "classic")

    private val _pwd = MutableStateFlow(PasswordChangeState())
    val password: StateFlow<PasswordChangeState> = _pwd.asStateFlow()

    fun setFollowSystem(v: Boolean) { viewModelScope.launch { prefs.setFollowSystem(v) } }
    fun setDarkMode(v: Boolean) { viewModelScope.launch { prefs.setDarkMode(v) } }
    fun setAppTheme(v: String) { viewModelScope.launch { prefs.setAppTheme(v) } }

    fun onCurrent(v: String) = _pwd.update { it.copy(current = v, error = null) }
    fun onNew(v: String) = _pwd.update { it.copy(newPass = v, error = null) }
    fun onConfirm(v: String) = _pwd.update { it.copy(confirm = v, error = null) }

    fun changePassword() {
        val s = _pwd.value
        when {
            s.current.isBlank() -> _pwd.update { it.copy(error = "Enter current password") }
            s.newPass.length < 6 -> _pwd.update { it.copy(error = "New password must be 6+ chars") }
            s.newPass != s.confirm -> _pwd.update { it.copy(error = "Passwords don't match") }
            else -> {
                _pwd.update { it.copy(isSaving = true, error = null, success = false) }
                viewModelScope.launch {
                    try {
                        val user = auth.currentUser ?: throw Exception("Not signed in")
                        val email = user.email ?: throw Exception("No email")
                        val cred = EmailAuthProvider.getCredential(email, s.current)
                        user.reauthenticate(cred).await()
                        user.updatePassword(s.newPass).await()
                        _pwd.value = PasswordChangeState(success = true)
                    } catch (e: Exception) {
                        _pwd.update {
                            it.copy(isSaving = false,
                                error = e.message?.take(120) ?: "Failed to change password")
                        }
                    }
                }
            }
        }
    }

    fun resetPasswordState() { _pwd.value = PasswordChangeState() }
}
