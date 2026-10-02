package com.school.manager.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.CloudinaryHelper
import com.school.manager.util.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val classId: String = "",
    val rollNo: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val doc = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: (auth.currentUser?.email ?: ""),
                    role = doc.getString("role") ?: "",
                    classId = doc.getString("classId") ?: "",
                    rollNo = doc.getString("rollNo") ?: "",
                    phone = doc.getString("phone") ?: "",
                    avatarUrl = doc.getString("avatarUrl") ?: ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleEdit() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun onName(v: String) = _uiState.value.let { _uiState.value = it.copy(name = v) }
    fun onPhone(v: String) = _uiState.value.let { _uiState.value = it.copy(phone = v) }
    fun onRollNo(v: String) = _uiState.value.let { _uiState.value = it.copy(rollNo = v) }

    fun save() {
        val uid = auth.currentUser?.uid ?: return
        val s = _uiState.value
        if (s.name.isBlank()) {
            _uiState.value = s.copy(error = "Name required"); return
        }
        _uiState.value = s.copy(isSaving = true, error = null, successMessage = null)
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.USERS).document(uid).update(
                    mapOf(
                        "name" to s.name.trim(),
                        "phone" to s.phone.trim(),
                        "rollNo" to s.rollNo.trim()
                    )
                ).await()
                _uiState.value = _uiState.value.copy(
                    isSaving = false, isEditing = false, successMessage = "Profile updated"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    /** Upload avatar via Cloudinary (replaces Firebase Storage). */
    fun uploadAvatar(uri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isUploadingAvatar = true, error = null)

        CloudinaryHelper.uploadFile(
            uri = uri,
            folder = "avatars/$uid",
            onSuccess = { url ->
                viewModelScope.launch {
                    try {
                        firestore.collection(FirestoreCollections.USERS)
                            .document(uid).update("avatarUrl", url).await()
                        _uiState.value = _uiState.value.copy(
                            isUploadingAvatar = false, avatarUrl = url,
                            successMessage = "Avatar updated"
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isUploadingAvatar = false, error = e.message
                        )
                    }
                }
            },
            onError = { msg ->
                _uiState.value = _uiState.value.copy(isUploadingAvatar = false, error = msg)
            }
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
