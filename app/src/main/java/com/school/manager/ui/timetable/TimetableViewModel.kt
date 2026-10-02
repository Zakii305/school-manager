package com.school.manager.ui.timetable

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

data class TimetableSlot(
    val id: String = "",
    val classId: String = "",
    val day: String = "Monday",
    val period: Int = 1,
    val subjectId: String = "",
    val subjectName: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val startTime: String = "09:00",
    val endTime: String = "09:45"
)

data class SubjectOption(val id: String, val name: String, val teacherId: String, val teacherName: String)

data class TimetableUiState(
    val isLoading: Boolean = true,
    val slots: List<TimetableSlot> = emptyList(),
    val subjects: List<SubjectOption> = emptyList(),
    val className: String = "",
    val classId: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

val WEEK_DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
const val MAX_PERIODS = 8

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState: StateFlow<TimetableUiState> = _uiState.asStateFlow()

    /** Loads class name + all slots + subjects for a class. */
    fun load(classId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, classId = classId)
        viewModelScope.launch {
            try {
                val classDoc = firestore.collection(FirestoreCollections.CLASSES)
                    .document(classId).get().await()
                val className = classDoc.getString("name") ?: "-"

                val slotSnap = firestore.collection(FirestoreCollections.TIMETABLE)
                    .whereEqualTo("classId", classId).get().await()
                val slots = slotSnap.documents.map { d ->
                    TimetableSlot(
                        id = d.id,
                        classId = classId,
                        day = d.getString("day") ?: "Monday",
                        period = (d.getLong("period") ?: 1L).toInt(),
                        subjectId = d.getString("subjectId") ?: "",
                        subjectName = d.getString("subjectName") ?: "",
                        teacherId = d.getString("teacherId") ?: "",
                        teacherName = d.getString("teacherName") ?: "",
                        startTime = d.getString("startTime") ?: "09:00",
                        endTime = d.getString("endTime") ?: "09:45"
                    )
                }

                val subSnap = firestore.collection(FirestoreCollections.SUBJECTS)
                    .whereEqualTo("classId", classId).get().await()
                val subjects = subSnap.documents.map {
                    SubjectOption(
                        id = it.id,
                        name = it.getString("name") ?: "-",
                        teacherId = it.getString("teacherId") ?: "",
                        teacherName = it.getString("teacherName") ?: "-"
                    )
                }.sortedBy { it.name }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    slots = slots,
                    subjects = subjects,
                    className = className
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /** For the student view: uses the currently signed-in user's classId. */
    fun loadForCurrentStudent() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
                val classId = userDoc.getString("classId") ?: ""
                if (classId.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false, error = "No class assigned to your account"
                    )
                    return@launch
                }
                load(classId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun upsert(slot: TimetableSlot) {
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "classId" to slot.classId,
                    "day" to slot.day,
                    "period" to slot.period,
                    "subjectId" to slot.subjectId,
                    "subjectName" to slot.subjectName,
                    "teacherId" to slot.teacherId,
                    "teacherName" to slot.teacherName,
                    "startTime" to slot.startTime,
                    "endTime" to slot.endTime,
                    "updatedAt" to System.currentTimeMillis()
                )
                val classId = slot.classId
                if (slot.id.isBlank()) {
                    firestore.collection(FirestoreCollections.TIMETABLE).add(data).await()
                } else {
                    firestore.collection(FirestoreCollections.TIMETABLE)
                        .document(slot.id).set(data).await()
                }
                _uiState.value = _uiState.value.copy(successMessage = "Saved")
                load(classId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun delete(slotId: String, classId: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.TIMETABLE)
                    .document(slotId).delete().await()
                _uiState.value = _uiState.value.copy(successMessage = "Removed")
                load(classId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() = _uiState.value.let {
        _uiState.value = it.copy(error = null, successMessage = null)
    }
}
