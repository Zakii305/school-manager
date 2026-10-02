package com.school.manager.ui.assignments

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

data class SubmissionState(
    val isLoading: Boolean = false,
    val existingUrl: String = "",
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val uploaded: Boolean = false,
    val error: String? = null,
    val savedMessage: String? = null
)

@HiltViewModel
class StudentSubmissionViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(SubmissionState())
    val state: StateFlow<SubmissionState> = _state.asStateFlow()

    fun loadExisting(assignmentId: String) {
        val uid = auth.currentUser?.uid ?: return
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val doc = firestore.collection(FirestoreCollections.SUBMISSIONS)
                    .document("${assignmentId}_$uid").get().await()
                val url = doc.getString("fileUrl") ?: ""
                _state.value = SubmissionState(existingUrl = url)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /** Upload submission to Cloudinary. */
    fun submit(assignmentId: String, uri: Uri, fileName: String) {
        val uid = auth.currentUser?.uid ?: return
        _state.value = _state.value.copy(
            isUploading = true, error = null, savedMessage = null, uploadProgress = 0
        )

        CloudinaryHelper.uploadFile(
            uri = uri,
            folder = "submissions/$assignmentId",
            onSuccess = { url ->
                viewModelScope.launch {
                    try {
                        val docId = "${assignmentId}_$uid"
                        val data = mapOf(
                            "assignmentId" to assignmentId,
                            "studentId" to uid,
                            "fileUrl" to url,
                            "fileName" to fileName,
                            "submittedAt" to System.currentTimeMillis()
                        )
                        firestore.collection(FirestoreCollections.SUBMISSIONS).document(docId).set(data).await()

                        _state.value = _state.value.copy(
                            isUploading = false,
                            uploaded = true,
                            existingUrl = url,
                            uploadProgress = 100,
                            savedMessage = "✅ Submitted!"
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(isUploading = false, error = e.message)
                    }
                }
            },
            onError = { msg ->
                _state.value = _state.value.copy(isUploading = false, error = msg)
            },
            onProgress = { pct ->
                _state.value = _state.value.copy(uploadProgress = pct)
            }
        )
    }
}
