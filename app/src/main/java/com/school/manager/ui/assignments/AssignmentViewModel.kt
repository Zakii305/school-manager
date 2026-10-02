package com.school.manager.ui.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.UserRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class Assignment(
    val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val classId: String,
    val className: String = "",
    val dueDate: Long = 0L,
    val attachmentUrl: String = "",
    val teacherName: String = "",
    val submissionCount: Int = 0
) {
    fun dueText(): String =
        if (dueDate == 0L) "No due date"
        else SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(dueDate))
}

data class AssignmentsUiState(
    val isLoading: Boolean = true,
    val assignments: List<Assignment> = emptyList(),
    val isTeacher: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssignmentsUiState())
    val uiState: StateFlow<AssignmentsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val meDoc = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
                val role = meDoc.getString("role") ?: ""
                val myClassId = meDoc.getString("classId") ?: ""
                val isTeacher = role == UserRoles.TEACHER

                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classSnap.documents.associate { it.id to (it.getString("name") ?: "-") }

                val query = if (isTeacher) {
                    firestore.collection(FirestoreCollections.ASSIGNMENTS)
                        .whereEqualTo("teacherId", uid)
                } else {
                    firestore.collection(FirestoreCollections.ASSIGNMENTS)
                        .whereEqualTo("classId", myClassId)
                }
                val snap = query.get().await()

                val subSnap = firestore.collection(FirestoreCollections.SUBMISSIONS).get().await()
                val subCountByAssignment = subSnap.documents.groupingBy {
                    it.getString("assignmentId") ?: ""
                }.eachCount()

                val list = snap.documents.map { d ->
                    Assignment(
                        id = d.id,
                        title = d.getString("title") ?: "-",
                        description = d.getString("description") ?: "",
                        subject = d.getString("subject") ?: "",
                        classId = d.getString("classId") ?: "",
                        className = classMap[d.getString("classId")] ?: "-",
                        dueDate = d.getLong("dueDate") ?: 0L,
                        attachmentUrl = d.getString("attachmentUrl") ?: "",
                        teacherName = d.getString("teacherName") ?: "",
                        submissionCount = subCountByAssignment[d.id] ?: 0
                    )
                }.sortedByDescending { it.dueDate }

                _uiState.value = _uiState.value.copy(
                    isLoading = false, assignments = list, isTeacher = isTeacher
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteAssignment(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.ASSIGNMENTS).document(id).delete().await()
                _uiState.value = _uiState.value.copy(successMessage = "Deleted")
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
