package com.school.manager.ui.submissions

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SubmissionRow(
    val studentId: String,
    val studentName: String,
    val fileUrl: String,
    val fileName: String,
    val submittedAt: Long,
    val isLate: Boolean
)

data class SubmissionViewerUiState(
    val isLoading: Boolean = true,
    val assignmentTitle: String = "",
    val dueDate: Long = 0L,
    val submissions: List<SubmissionRow> = emptyList(),
    val submittedCount: Int = 0,
    val totalStudents: Int = 0,
    val error: String? = null
)

@HiltViewModel
class SubmissionViewerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubmissionViewerUiState())
    val uiState: StateFlow<SubmissionViewerUiState> = _uiState.asStateFlow()

    fun load(assignmentId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val aDoc = firestore.collection(FirestoreCollections.ASSIGNMENTS)
                    .document(assignmentId).get().await()
                val title = aDoc.getString("title") ?: "-"
                val due = aDoc.getLong("dueDate") ?: 0L
                val classId = aDoc.getString("classId") ?: ""

                val studentsSnap = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo("role", "student")
                    .whereEqualTo("classId", classId)
                    .get().await()
                val studentNames = studentsSnap.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }

                val subSnap = firestore.collection(FirestoreCollections.SUBMISSIONS)
                    .whereEqualTo("assignmentId", assignmentId).get().await()

                val subs = subSnap.documents.mapNotNull { d ->
                    val sid = d.getString("studentId") ?: return@mapNotNull null
                    val at = d.getLong("submittedAt") ?: 0L
                    SubmissionRow(
                        studentId = sid,
                        studentName = studentNames[sid] ?: "-",
                        fileUrl = d.getString("fileUrl") ?: "",
                        fileName = d.getString("fileName") ?: "submission",
                        submittedAt = at,
                        isLate = due > 0 && at > due
                    )
                }.sortedBy { it.studentName }

                _uiState.value = SubmissionViewerUiState(
                    isLoading = false,
                    assignmentTitle = title,
                    dueDate = due,
                    submissions = subs,
                    submittedCount = subs.size,
                    totalStudents = studentNames.size
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
