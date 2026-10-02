package com.school.manager.ui.teacher

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

data class StudentRow(
    val id: String,
    val name: String,
    val email: String,
    var status: String = "present"
)

data class TeacherUiState(
    val isLoading: Boolean = true,
    val teacherName: String = "Teacher",
    val totalStudents: Int = 0,
    val presentToday: Int = 0,
    val absentToday: Int = 0,
    val error: String? = null
)

data class AttendanceUiState(
    val isLoading: Boolean = true,
    val date: String = todayKey(),
    val students: List<StudentRow> = emptyList(),
    val isSaving: Boolean = false,
    val savedMessage: String? = null,
    val error: String? = null
)

private fun todayKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

@HiltViewModel
class TeacherViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherUiState())
    val uiState: StateFlow<TeacherUiState> = _uiState.asStateFlow()

    private val _attendanceState = MutableStateFlow(AttendanceUiState())
    val attendanceState: StateFlow<AttendanceUiState> = _attendanceState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val usersSnap = firestore.collection(FirestoreCollections.USERS).get().await()
                var studentCount = 0
                for (doc in usersSnap.documents) {
                    if (doc.getString("role") == UserRoles.STUDENT) studentCount++
                }

                val today = todayKey()
                val attSnap = firestore.collection(FirestoreCollections.ATTENDANCE)
                    .whereEqualTo("date", today).get().await()

                var present = 0; var absent = 0
                for (doc in attSnap.documents) {
                    when (doc.getString("status")) {
                        "present" -> present++
                        "absent" -> absent++
                    }
                }

                val name = auth.currentUser?.email?.substringBefore("@") ?: "Teacher"
                _uiState.value = TeacherUiState(
                    isLoading = false,
                    teacherName = name,
                    totalStudents = studentCount,
                    presentToday = present,
                    absentToday = absent
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Load failed"
                )
            }
        }
    }

    fun loadAttendance(classId: String = "default") {
        _attendanceState.value = _attendanceState.value.copy(
            isLoading = true, error = null, savedMessage = null
        )
        viewModelScope.launch {
            try {
                val usersSnap = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo("role", UserRoles.STUDENT).get().await()

                val today = todayKey()
                val attSnap = firestore.collection(FirestoreCollections.ATTENDANCE)
                    .whereEqualTo("date", today).get().await()

                val existing = mutableMapOf<String, String>()
                for (doc in attSnap.documents) {
                    val sid = doc.getString("studentId") ?: continue
                    existing[sid] = doc.getString("status") ?: "present"
                }

                val students = usersSnap.documents.map { d ->
                    StudentRow(
                        id = d.id,
                        name = d.getString("name") ?: "Unknown",
                        email = d.getString("email") ?: "",
                        status = existing[d.id] ?: "present"
                    )
                }

                _attendanceState.value = _attendanceState.value.copy(
                    isLoading = false, students = students, date = today
                )
            } catch (e: Exception) {
                _attendanceState.value = _attendanceState.value.copy(
                    isLoading = false, error = e.message ?: "Load failed"
                )
            }
        }
    }

    fun toggleStatus(studentId: String) {
        val cycle = listOf("present", "absent", "late", "leave")
        val current = _attendanceState.value.students
        val updated = current.map { s ->
            if (s.id == studentId) {
                val idx = cycle.indexOf(s.status)
                val next = cycle[(idx + 1) % cycle.size]
                s.copy(status = next)
            } else s
        }
        _attendanceState.value = _attendanceState.value.copy(students = updated)
    }

    fun saveAttendance() {
        val state = _attendanceState.value
        if (state.students.isEmpty()) {
            _attendanceState.value = state.copy(error = "No students to save")
            return
        }
        _attendanceState.value = state.copy(isSaving = true, error = null, savedMessage = null)

        viewModelScope.launch {
            try {
                val teacherId = auth.currentUser?.uid ?: "unknown"
                val batch = firestore.batch()
                for (s in state.students) {
                    val docId = "${state.date}_${s.id}"
                    val ref = firestore.collection(FirestoreCollections.ATTENDANCE).document(docId)
                    val data = mapOf(
                        "studentId" to s.id,
                        "studentName" to s.name,
                        "date" to state.date,
                        "status" to s.status,
                        "markedBy" to teacherId,
                        "markedAt" to System.currentTimeMillis()
                    )
                    batch.set(ref, data)
                }
                batch.commit().await()
                _attendanceState.value = _attendanceState.value.copy(
                    isSaving = false,
                    savedMessage = "✅ Attendance saved for ${state.students.size} students"
                )
                loadDashboard()
            } catch (e: Exception) {
                _attendanceState.value = _attendanceState.value.copy(
                    isSaving = false, error = e.message ?: "Save failed"
                )
            }
        }
    }
}
