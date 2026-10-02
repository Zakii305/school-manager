package com.school.manager.ui.teacher

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeworkClassOption(val id: String, val name: String)

data class HomeworkPost(
    val id: String,
    val title: String,
    val className: String,
    val subject: String,
    val dueDate: Long,
    val photoCount: Int
) {
    fun dueText(): String =
        if (dueDate == 0L) "—"
        else SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(dueDate))
}

data class HomeworkUiState(
    val isLoading: Boolean = true,
    val classes: List<HomeworkClassOption> = emptyList(),
    val posts: List<HomeworkPost> = emptyList(),
    val selectedClassId: String = "",
    val subject: String = "",
    val dueDate: Long = System.currentTimeMillis(),
    val title: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class HomeworkViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeworkUiState())
    val uiState: StateFlow<HomeworkUiState> = _ui.asStateFlow()

    init {
        loadClasses()
        loadPosts()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val list = snap.documents.map {
                    HomeworkClassOption(it.id, it.getString("name") ?: "-")
                }.sortedBy { it.name }
                _ui.update { it.copy(classes = list, isLoading = false) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadPosts() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.ASSIGNMENTS)
                    .whereEqualTo("teacherId", uid).get().await()
                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classSnap.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }
                val posts = snap.documents.map { d ->
                    @Suppress("UNCHECKED_CAST")
                    val photos = (d.get("photoUrls") as? List<String>) ?: emptyList()
                    HomeworkPost(
                        id = d.id,
                        title = d.getString("title") ?: "-",
                        className = classMap[d.getString("classId")] ?: "-",
                        subject = d.getString("subject") ?: "-",
                        dueDate = d.getLong("dueDate") ?: 0L,
                        photoCount = photos.size
                    )
                }.sortedByDescending { it.dueDate }
                _ui.update { it.copy(posts = posts) }
            } catch (_: Exception) { }
        }
    }

    fun onClass(id: String) = _ui.update { it.copy(selectedClassId = id) }
    fun onSubject(v: String) = _ui.update { it.copy(subject = v) }
    fun onDueDate(millis: Long) = _ui.update { it.copy(dueDate = millis) }
    fun onTitle(v: String) = _ui.update { it.copy(title = v) }
    fun onDescription(v: String) = _ui.update { it.copy(description = v) }

    fun postHomework() {
        val s = _ui.value
        if (s.title.isBlank() || s.selectedClassId.isBlank()) {
            _ui.update { it.copy(error = "Title and class are required") }
            return
        }
        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: ""
                val tName = auth.currentUser?.email?.substringBefore("@") ?: "Teacher"
                firestore.collection(FirestoreCollections.ASSIGNMENTS).add(
                    mapOf(
                        "title" to s.title.trim(),
                        "description" to s.description.trim(),
                        "subject" to s.subject.trim(),
                        "classId" to s.selectedClassId,
                        "dueDate" to s.dueDate,
                        "teacherId" to uid,
                        "teacherName" to tName,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()
                _ui.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Homework posted",
                        title = "",
                        description = "",
                        subject = ""
                    )
                }
                loadPosts()
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deletePost(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.ASSIGNMENTS).document(id).delete().await()
                loadPosts()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
