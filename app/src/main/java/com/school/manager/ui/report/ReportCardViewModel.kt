package com.school.manager.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ReportSubject(
    val name: String,
    val marks: Int,
    val total: Int,
    val examName: String
) {
    val percent: Int get() = if (total > 0) marks * 100 / total else 0
    val grade: String get() = when {
        percent >= 90 -> "A+"
        percent >= 80 -> "A"
        percent >= 70 -> "B"
        percent >= 60 -> "C"
        percent >= 50 -> "D"
        else -> "F"
    }
}

data class ReportCardUiState(
    val isLoading: Boolean = true,
    val studentName: String = "",
    val studentEmail: String = "",
    val className: String = "",
    val rollNo: String = "",
    val subjects: List<ReportSubject> = emptyList(),
    val totalPresent: Int = 0,
    val totalAbsent: Int = 0,
    val attendancePercent: Int = 0,
    val overallPercent: Int = 0,
    val overallGrade: String = "",
    val generatedOn: String = "",
    val error: String? = null
)

@HiltViewModel
class ReportCardViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportCardUiState())
    val uiState: StateFlow<ReportCardUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Not signed in")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val userDoc = firestore.collection(FirestoreCollections.USERS)
                    .document(uid).get().await()
                val name = userDoc.getString("name") ?: "Student"
                val email = userDoc.getString("email") ?: (auth.currentUser?.email ?: "")
                val classId = userDoc.getString("classId") ?: ""
                val rollNo = userDoc.getString("rollNo") ?: "-"

                var className = "-"
                if (classId.isNotBlank()) {
                    val cd = firestore.collection(FirestoreCollections.CLASSES)
                        .document(classId).get().await()
                    className = cd.getString("name") ?: "-"
                }

                val gradeSnap = firestore.collection(FirestoreCollections.GRADES)
                    .whereEqualTo("studentId", uid).get().await()

                val subjects = gradeSnap.documents.mapNotNull { d ->
                    val subj = d.getString("subject") ?: return@mapNotNull null
                    ReportSubject(
                        name = subj,
                        marks = (d.getLong("marks") ?: 0L).toInt(),
                        total = (d.getLong("total") ?: 100L).toInt(),
                        examName = d.getString("examName") ?: "-"
                    )
                }.sortedBy { it.name }

                val overall = if (subjects.isNotEmpty())
                    subjects.map { it.percent }.average().toInt() else 0
                val overallGrade = when {
                    overall >= 90 -> "A+"
                    overall >= 80 -> "A"
                    overall >= 70 -> "B"
                    overall >= 60 -> "C"
                    overall >= 50 -> "D"
                    else -> "F"
                }

                val attSnap = firestore.collection(FirestoreCollections.ATTENDANCE)
                    .whereEqualTo("studentId", uid).get().await()
                val present = attSnap.count { it.getString("status") == "present" }
                val absent = attSnap.count { it.getString("status") == "absent" }
                val total = present + absent
                val attPct = if (total > 0) present * 100 / total else 0

                val date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US)
                    .format(java.util.Date())

                _uiState.value = ReportCardUiState(
                    isLoading = false,
                    studentName = name,
                    studentEmail = email,
                    className = className,
                    rollNo = rollNo,
                    subjects = subjects,
                    totalPresent = present,
                    totalAbsent = absent,
                    attendancePercent = attPct,
                    overallPercent = overall,
                    overallGrade = overallGrade,
                    generatedOn = date
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun reload() = load()
}
