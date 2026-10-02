package com.school.manager.ui.student

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AttendanceRecord(
    val date: String,
    val status: String
)

data class HomeworkItem(
    val id: String,
    val title: String,
    val subject: String,
    val dueDate: String,
    val description: String
)

data class ResultItem(
    val subject: String,
    val marks: Int,
    val total: Int
) {
    val percent: Int get() = if (total > 0) (marks * 100 / total) else 0
}

data class StudentUiState(
    val isLoading: Boolean = true,
    val studentName: String = "Student",
    val studentEmail: String = "",
    val attendancePercent: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val pendingFees: Double = 0.0,
    val recentHomework: List<HomeworkItem> = emptyList(),
    val attendanceHistory: List<AttendanceRecord> = emptyList(),
    val results: List<ResultItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "Not signed in")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Profile
                val userDoc = firestore.collection(FirestoreCollections.USERS)
                    .document(uid).get().await()
                val name = userDoc.getString("name") ?: "Student"
                val email = userDoc.getString("email") ?: (auth.currentUser?.email ?: "")

                // Attendance
                val attSnap = firestore.collection(FirestoreCollections.ATTENDANCE)
                    .whereEqualTo("studentId", uid).get().await()

                val history = attSnap.documents.mapNotNull { d ->
                    val date = d.getString("date") ?: return@mapNotNull null
                    val status = d.getString("status") ?: "present"
                    AttendanceRecord(date, status)
                }.sortedByDescending { it.date }

                val present = history.count { it.status == "present" }
                val absent = history.count { it.status == "absent" }
                val total = history.size
                val pct = if (total > 0) (present * 100 / total) else 0

                // Fees
                val feesSnap = firestore.collection(FirestoreCollections.FEES)
                    .whereEqualTo("studentId", uid).get().await()
                var pending = 0.0
                for (doc in feesSnap.documents) {
                    val paid = doc.getBoolean("paid") ?: false
                    val amount = doc.getDouble("amount") ?: 0.0
                    if (!paid) pending += amount
                }

                // Homework / Assignments
                val hwSnap = firestore.collection(FirestoreCollections.ASSIGNMENTS).get().await()
                val hw = hwSnap.documents.mapNotNull { d ->
                    val title = d.getString("title") ?: return@mapNotNull null
                    HomeworkItem(
                        id = d.id,
                        title = title,
                        subject = d.getString("subject") ?: "General",
                        dueDate = d.getString("dueDate") ?: "-",
                        description = d.getString("description") ?: ""
                    )
                }.take(5)

                // Results (grades collection)
                val gradeSnap = firestore.collection(FirestoreCollections.GRADES)
                    .whereEqualTo("studentId", uid).get().await()
                val results = gradeSnap.documents.mapNotNull { d ->
                    val subj = d.getString("subject") ?: return@mapNotNull null
                    val marks = (d.getLong("marks") ?: 0L).toInt()
                    val total = (d.getLong("total") ?: 100L).toInt()
                    ResultItem(subj, marks, total)
                }

                _uiState.value = StudentUiState(
                    isLoading = false,
                    studentName = name,
                    studentEmail = email,
                    attendancePercent = pct,
                    presentCount = present,
                    absentCount = absent,
                    pendingFees = pending,
                    recentHomework = hw,
                    attendanceHistory = history,
                    results = results
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }

    fun reload() = loadAll()
}
