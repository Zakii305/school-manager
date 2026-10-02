package com.school.manager.ui.gradebook

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
import javax.inject.Inject

data class SubjectOption(val id: String, val name: String)

data class MarkRow(
    val studentId: String,
    val studentName: String,
    var marksText: String = ""
) {
    val marks: Int? get() = marksText.toIntOrNull()
}

data class GradebookUiState(
    val isLoading: Boolean = true,
    val examName: String = "",
    val className: String = "",
    val classId: String = "",
    val maxMarks: Int = 100,
    val subjects: List<SubjectOption> = emptyList(),
    val selectedSubjectId: String = "",
    val rows: List<MarkRow> = emptyList(),
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class GradebookViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(GradebookUiState())
    val uiState: StateFlow<GradebookUiState> = _uiState.asStateFlow()

    fun load(examId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val examDoc = firestore.collection(FirestoreCollections.EXAMS).document(examId).get().await()
                val examName = examDoc.getString("name") ?: "-"
                val classId = examDoc.getString("classId") ?: ""
                val maxMarks = (examDoc.getLong("maxMarks") ?: 100L).toInt()

                val classDoc = firestore.collection(FirestoreCollections.CLASSES).document(classId).get().await()
                val className = classDoc.getString("name") ?: "-"

                val subjSnap = firestore.collection(FirestoreCollections.SUBJECTS)
                    .whereEqualTo("classId", classId).get().await()
                val subjects = subjSnap.documents.map {
                    SubjectOption(it.id, it.getString("name") ?: "-")
                }.sortedBy { it.name }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    examName = examName,
                    classId = classId,
                    className = className,
                    maxMarks = maxMarks,
                    subjects = subjects,
                    selectedSubjectId = subjects.firstOrNull()?.id ?: ""
                )
                if (subjects.isNotEmpty()) loadMarks(examId, subjects.first().id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectSubject(examId: String, subjectId: String) {
        _uiState.value = _uiState.value.copy(selectedSubjectId = subjectId)
        loadMarks(examId, subjectId)
    }

    private fun loadMarks(examId: String, subjectId: String) {
        val classId = _uiState.value.classId
        viewModelScope.launch {
            try {
                val studentsSnap = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo("role", UserRoles.STUDENT)
                    .whereEqualTo("classId", classId)
                    .get().await()

                val existing = firestore.collection(FirestoreCollections.GRADES)
                    .whereEqualTo("examId", examId)
                    .whereEqualTo("subjectId", subjectId)
                    .get().await()

                val existingMap = mutableMapOf<String, Int>()
                for (d in existing.documents) {
                    val sid = d.getString("studentId") ?: continue
                    existingMap[sid] = (d.getLong("marks") ?: 0L).toInt()
                }

                val rows = studentsSnap.documents.map { d ->
                    val sid = d.id
                    MarkRow(
                        studentId = sid,
                        studentName = d.getString("name") ?: "-",
                        marksText = existingMap[sid]?.toString() ?: ""
                    )
                }.sortedBy { it.studentName }

                _uiState.value = _uiState.value.copy(rows = rows)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateMark(studentId: String, text: String) {
        val cleaned = text.filter { it.isDigit() }.take(3)
        _uiState.value = _uiState.value.copy(
            rows = _uiState.value.rows.map {
                if (it.studentId == studentId) it.copy(marksText = cleaned) else it
            }
        )
    }

    fun save(examId: String) {
        val s = _uiState.value
        if (s.selectedSubjectId.isBlank()) {
            _uiState.value = s.copy(error = "Select a subject first")
            return
        }
        _uiState.value = s.copy(isSaving = true, error = null, savedMessage = null)
        viewModelScope.launch {
            try {
                val teacherId = auth.currentUser?.uid ?: ""
                val subjectName = s.subjects.firstOrNull { it.id == s.selectedSubjectId }?.name ?: ""
                val batch = firestore.batch()
                for (r in s.rows) {
                    if (r.marks == null) continue
                    val docId = "${examId}_${s.selectedSubjectId}_${r.studentId}"
                    val ref = firestore.collection(FirestoreCollections.GRADES).document(docId)
                    batch.set(ref, mapOf(
                        "examId" to examId,
                        "examName" to s.examName,
                        "subjectId" to s.selectedSubjectId,
                        "subject" to subjectName,
                        "studentId" to r.studentId,
                        "studentName" to r.studentName,
                        "marks" to r.marks,
                        "total" to s.maxMarks,
                        "classId" to s.classId,
                        "teacherId" to teacherId,
                        "updatedAt" to System.currentTimeMillis()
                    ))
                }
                batch.commit().await()
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    savedMessage = "✅ Saved ${s.rows.count { it.marks != null }} marks"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
