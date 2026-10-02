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

data class TeacherCell(
    val day: String,
    val period: Int,
    val subjectName: String,
    val className: String
)

data class TeacherTimetableUiState(
    val isLoading: Boolean = true,
    val teacherName: String = "",
    val cells: List<TeacherCell> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TeacherTimetableViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherTimetableUiState())
    val uiState: StateFlow<TeacherTimetableUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        val uid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val me = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
                val name = me.getString("name") ?: "Teacher"

                val slotSnap = firestore.collection(FirestoreCollections.TIMETABLE)
                    .whereEqualTo("teacherId", uid).get().await()

                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classSnap.documents.associate { it.id to (it.getString("name") ?: "-") }

                val cells = slotSnap.documents.mapNotNull { d ->
                    val day = d.getString("day") ?: return@mapNotNull null
                    val period = (d.getLong("period") ?: 0L).toInt()
                    val subj = d.getString("subjectName") ?: ""
                    val cid = d.getString("classId") ?: ""
                    if (subj.isBlank()) null
                    else TeacherCell(day, period, subj, classMap[cid] ?: "-")
                }

                _uiState.value = TeacherTimetableUiState(
                    isLoading = false, teacherName = name, cells = cells
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
