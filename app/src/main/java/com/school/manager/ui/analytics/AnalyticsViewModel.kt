package com.school.manager.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class ClassAnalytics(
    val classId: String,
    val className: String,
    val studentCount: Int,
    val presentToday: Int,
    val absentToday: Int,
    val attendancePercent: Int,
    val avgScore: Int,
    val feesCollected: Double,
    val feesPending: Double
)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val overallAttendancePercent: Int = 0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val averageScore: Int = 0,
    val perClass: List<ClassAnalytics> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(java.util.Date())

                // 1. Users
                val users = firestore.collection(FirestoreCollections.USERS).get().await()
                var totalStudents = 0
                var totalTeachers = 0
                val studentClassMap = mutableMapOf<String, String>()   // uid → classId
                users.forEach { d ->
                    val role = d.getString("role")
                    val classId = d.getString("classId") ?: ""
                    when (role) {
                        UserRoles.STUDENT -> {
                            totalStudents++
                            if (classId.isNotBlank()) studentClassMap[d.id] = classId
                        }
                        UserRoles.TEACHER -> totalTeachers++
                    }
                }

                // 2. Classes
                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classNameMap = classSnap.documents.associate { it.id to (it.getString("name") ?: "-") }

                // 3. Attendance (today + all-time for score calc)
                val attSnap = firestore.collection(FirestoreCollections.ATTENDANCE).get().await()
                val todaySnap = attSnap.documents.filter { it.getString("date") == today }

                var globalPresent = 0
                var globalAbsent = 0
                val perClassPresent = mutableMapOf<String, Int>()
                val perClassAbsent = mutableMapOf<String, Int>()

                todaySnap.forEach { d ->
                    val sid = d.getString("studentId") ?: return@forEach
                    val cid = studentClassMap[sid] ?: return@forEach
                    when (d.getString("status")) {
                        "present" -> {
                            globalPresent++
                            perClassPresent[cid] = (perClassPresent[cid] ?: 0) + 1
                        }
                        "absent" -> {
                            globalAbsent++
                            perClassAbsent[cid] = (perClassAbsent[cid] ?: 0) + 1
                        }
                    }
                }
                val totalAtt = globalPresent + globalAbsent
                val globalPct = if (totalAtt > 0) globalPresent * 100 / totalAtt else 0

                // 4. Grades (average per class)
                val gradeSnap = firestore.collection(FirestoreCollections.GRADES).get().await()
                val perClassScores = mutableMapOf<String, MutableList<Int>>()
                gradeSnap.forEach { d ->
                    val sid = d.getString("studentId") ?: return@forEach
                    val cid = studentClassMap[sid] ?: return@forEach
                    val marks = (d.getLong("marks") ?: 0L).toInt()
                    val total = (d.getLong("total") ?: 100L).toInt()
                    if (total > 0) {
                        val pct = marks * 100 / total
                        perClassScores.getOrPut(cid) { mutableListOf() }.add(pct)
                    }
                }

                // 5. Fees
                val feeSnap = firestore.collection(FirestoreCollections.FEES).get().await()
                var totalCollected = 0.0
                var totalPending = 0.0
                val perClassFeesCollected = mutableMapOf<String, Double>()
                val perClassFeesPending = mutableMapOf<String, Double>()

                feeSnap.forEach { d ->
                    val sid = d.getString("studentId") ?: return@forEach
                    val cid = studentClassMap[sid] ?: ""
                    val amount = d.getDouble("amount") ?: 0.0
                    val paid = d.getBoolean("paid") ?: false
                    if (paid) {
                        totalCollected += amount
                        perClassFeesCollected[cid] = (perClassFeesCollected[cid] ?: 0.0) + amount
                    } else {
                        totalPending += amount
                        perClassFeesPending[cid] = (perClassFeesPending[cid] ?: 0.0) + amount
                    }
                }

                // 6. Build per-class rows
                val perClass = classSnap.documents.map { d ->
                    val cid = d.id
                    val cName = classNameMap[cid] ?: "-"
                    val studentsInClass = studentClassMap.count { it.value == cid }
                    val p = perClassPresent[cid] ?: 0
                    val a = perClassAbsent[cid] ?: 0
                    val attPct = if (p + a > 0) p * 100 / (p + a) else 0
                    val scores = perClassScores[cid] ?: mutableListOf()
                    val avg = if (scores.isNotEmpty()) scores.average().toInt() else 0
                    ClassAnalytics(
                        classId = cid,
                        className = cName,
                        studentCount = studentsInClass,
                        presentToday = p,
                        absentToday = a,
                        attendancePercent = attPct,
                        avgScore = avg,
                        feesCollected = perClassFeesCollected[cid] ?: 0.0,
                        feesPending = perClassFeesPending[cid] ?: 0.0
                    )
                }.sortedBy { it.className }

                // 7. Overall average score
                val allScores = perClassScores.values.flatten()
                val overallAvg = if (allScores.isNotEmpty()) allScores.average().toInt() else 0

                _uiState.value = AnalyticsUiState(
                    isLoading = false,
                    totalStudents = totalStudents,
                    totalTeachers = totalTeachers,
                    overallAttendancePercent = globalPct,
                    totalCollected = totalCollected,
                    totalPending = totalPending,
                    averageScore = overallAvg,
                    perClass = perClass
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
