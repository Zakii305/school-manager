package com.school.manager.ui.exams

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

data class HallTicketUiState(
    val isLoading: Boolean = true,
    val ticketNo: String = "",
    val studentName: String = "",
    val studentEmail: String = "",
    val className: String = "",
    val rollNo: String = "",
    val examName: String = "",
    val examDate: Long = 0L,
    val examDateText: String = "",
    val subjects: List<String> = emptyList(),
    val venue: String = "Main Examination Hall",
    val startTime: String = "09:00",
    val endTime: String = "12:00",
    val error: String? = null
) {
    fun toPdfData(): HallTicketData = HallTicketData(
        ticketNo = ticketNo,
        studentName = studentName,
        studentEmail = studentEmail,
        className = className,
        rollNo = rollNo,
        examName = examName,
        examDate = examDate,
        subjects = subjects,
        venue = venue,
        startTime = startTime,
        endTime = endTime
    )
}

@HiltViewModel
class HallTicketViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HallTicketUiState())
    val uiState: StateFlow<HallTicketUiState> = _uiState.asStateFlow()

    fun load(examId: String) {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Exam
                val examDoc = firestore.collection(FirestoreCollections.EXAMS)
                    .document(examId).get().await()
                val examName = examDoc.getString("name") ?: "-"
                val examDate = examDoc.getLong("date") ?: 0L
                val classId = examDoc.getString("classId") ?: ""

                // Class
                var className = "-"
                if (classId.isNotBlank()) {
                    val cd = firestore.collection(FirestoreCollections.CLASSES)
                        .document(classId).get().await()
                    className = cd.getString("name") ?: "-"
                }

                // Student
                val userDoc = firestore.collection(FirestoreCollections.USERS)
                    .document(uid).get().await()
                val studentName = userDoc.getString("name") ?: "-"
                val studentEmail = userDoc.getString("email") ?: ""
                val rollNo = userDoc.getString("rollNo") ?: "-"

                // Subjects in class
                val subSnap = firestore.collection(FirestoreCollections.SUBJECTS)
                    .whereEqualTo("classId", classId).get().await()
                val subjects = subSnap.documents.mapNotNull { it.getString("name") }
                    .sorted()

                val dateText = if (examDate > 0)
                    SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US).format(Date(examDate))
                else "-"

                _uiState.value = HallTicketUiState(
                    isLoading = false,
                    ticketNo = "HT-${uid.takeLast(6)}-${examId.takeLast(4)}",
                    studentName = studentName,
                    studentEmail = studentEmail,
                    className = className,
                    rollNo = rollNo,
                    examName = examName,
                    examDate = examDate,
                    examDateText = dateText,
                    subjects = subjects
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
