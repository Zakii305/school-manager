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

data class AssignmentClassOption(val id: String, val name: String)

data class AssignmentFormState(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val subject: String = "",
    val classId: String = "",
    val dueDate: Long = System.currentTimeMillis() + 7L * 24 * 3600 * 1000,
    val attachmentUrl: String = "",
    val classes: List<AssignmentClassOption> = emptyList(),
    val isEdit: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AssignmentFormViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(AssignmentFormState())
    val state: StateFlow<AssignmentFormState> = _state.asStateFlow()

    fun load(assignmentId: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val cSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classes = cSnap.documents.map {
                    AssignmentClassOption(it.id, it.getString("name") ?: "-")
                }.sortedBy { it.name }

                if (assignmentId == "new") {
                    _state.value = _state.value.copy(classes = classes, isLoading = false)
                } else {
                    val doc = firestore.collection(FirestoreCollections.ASSIGNMENTS)
                        .document(assignmentId).get().await()
                    if (doc.exists()) {
                        _state.value = AssignmentFormState(
                            id = assignmentId,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            subject = doc.getString("subject") ?: "",
                            classId = doc.getString("classId") ?: "",
                            dueDate = doc.getLong("dueDate") ?: System.currentTimeMillis(),
                            attachmentUrl = doc.getString("attachmentUrl") ?: "",
                            classes = classes,
                            isEdit = true,
                            isLoading = false
                        )
                    } else {
                        _state.value = _state.value.copy(classes = classes, isLoading = false, error = "Not found")
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onTitle(v: String) = _state.value.let { _state.value = it.copy(title = v) }
    fun onDescription(v: String) = _state.value.let { _state.value = it.copy(description = v) }
    fun onSubject(v: String) = _state.value.let { _state.value = it.copy(subject = v) }
    fun onClass(id: String) = _state.value.let { _state.value = it.copy(classId = id) }
    fun onDueDate(millis: Long) = _state.value.let { _state.value = it.copy(dueDate = millis) }

    /** Upload attachment to Cloudinary. */
    fun uploadAttachment(uri: Uri, fileName: String) {
        val s = _state.value
        _state.value = s.copy(isUploading = true, error = null, uploadProgress = 0)

        CloudinaryHelper.uploadFile(
            uri = uri,
            folder = "assignments",
            onSuccess = { url ->
                _state.value = _state.value.copy(
                    isUploading = false, attachmentUrl = url, uploadProgress = 100
                )
            },
            onError = { msg ->
                _state.value = _state.value.copy(isUploading = false, error = msg)
            },
            onProgress = { pct ->
                _state.value = _state.value.copy(uploadProgress = pct)
            }
        )
    }

    fun save() {
        val s = _state.value
        if (s.title.isBlank() || s.classId.isBlank()) {
            _state.value = s.copy(error = "Title and class required"); return
        }
        _state.value = s.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val teacherId = auth.currentUser?.uid ?: ""
                val teacherName = auth.currentUser?.email?.substringBefore("@") ?: "Teacher"
                val data = mapOf(
                    "title" to s.title.trim(),
                    "description" to s.description.trim(),
                    "subject" to s.subject.trim(),
                    "classId" to s.classId,
                    "dueDate" to s.dueDate,
                    "attachmentUrl" to s.attachmentUrl,
                    "teacherId" to teacherId,
                    "teacherName" to teacherName,
                    "updatedAt" to System.currentTimeMillis()
                )
                if (s.isEdit && s.id.isNotBlank()) {
                    firestore.collection(FirestoreCollections.ASSIGNMENTS).document(s.id).update(data).await()
                } else {
                    firestore.collection(FirestoreCollections.ASSIGNMENTS)
                        .add(data + ("createdAt" to System.currentTimeMillis())).await()
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
