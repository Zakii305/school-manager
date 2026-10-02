package com.school.manager.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class Period(val label: String) // e.g. "08:00 - 08:40"
data class Slot(val subject: String = "", val teacher: String = "")

data class TimetableUiState(
    val classes: List<Pair<String, String>> = emptyList(),
    val selectedClassId: String = "",
    val periods: List<Period> = listOf(
        Period("08:00 - 08:40"),
        Period("08:40 - 09:20"),
        Period("09:20 - 10:00"),
        Period("10:20 - 11:00"),
        Period("11:00 - 11:40"),
        Period("11:40 - 12:20")
    ),
    val days: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
    // grid[periodIndex][dayIndex]
    val grid: List<List<Slot>> = emptyList(),
    val subjects: List<String> = emptyList(),
    val teachers: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(TimetableUiState())
    val state: StateFlow<TimetableUiState> = _state.asStateFlow()

    init { init() }

    private fun str(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    private fun blankGrid(rows: Int, cols: Int) = List(rows) { List(cols) { Slot() } }

    private fun init() {
        viewModelScope.launch {
            try {
                val cls = firestore.collection("classes").get().await()
                val classList = cls.documents.map { d ->
                    val nm = str(d, "name"); val sec = str(d, "section")
                    d.id to if (sec.isBlank()) nm else "$nm - $sec"
                }.sortedBy { it.second }
                val subSnap = firestore.collection("subjects").get().await()
                val subjectNames = subSnap.documents.mapNotNull { it.getString("name") }.distinct().sorted()
                val staffSnap = firestore.collection("staff").get().await()
                val teacherNames = staffSnap.documents.mapNotNull {
                    it.getString("name") ?: it.getString("full_name")
                }.distinct().sorted()
                val st = _state.value
                _state.value = st.copy(
                    classes = classList,
                    subjects = subjectNames,
                    teachers = teacherNames,
                    grid = blankGrid(st.periods.size, st.days.size),
                    selectedClassId = classList.firstOrNull()?.first ?: "",
                    isLoading = false
                )
                if (classList.isNotEmpty()) loadTimetable(classList.first().first)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectClass(id: String) {
        _state.value = _state.value.copy(selectedClassId = id)
        loadTimetable(id)
    }

    private fun loadTimetable(classId: String) {
        viewModelScope.launch {
            try {
                val st = _state.value
                val grid = blankGrid(st.periods.size, st.days.size).toMutableList()
                val snap = firestore.collection("timetable")
                    .whereEqualTo("class_id", classId).get().await()
                snap.documents.forEach { d ->
                    val day = str(d, "day_of_week").ifBlank { str(d, "day") }
                    val periodLabel = str(d, "period").ifBlank { str(d, "period_label") }
                    val subj = str(d, "subject").ifBlank { str(d, "subject_id") }
                    val teach = str(d, "teacher").ifBlank { str(d, "teacher_id") }
                    val pIdx = st.periods.indexOfFirst { it.label == periodLabel }
                    val dIdx = st.days.indexOfFirst { it.equals(day, true) }
                    if (pIdx >= 0 && dIdx >= 0) {
                        val row = grid[pIdx].toMutableList()
                        row[dIdx] = Slot(subj, teach)
                        grid[pIdx] = row
                    }
                }
                _state.value = st.copy(grid = grid)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun updateSlot(periodIdx: Int, dayIdx: Int, subject: String = "", teacher: String = "") {
        val st = _state.value
        val grid = st.grid.toMutableList()
        val row = grid[periodIdx].toMutableList()
        val cur = row[dayIdx]
        row[dayIdx] = cur.copy(
            subject = subject.ifBlank { cur.subject },
            teacher = teacher.ifBlank { cur.teacher }
        )
        grid[periodIdx] = row
        _state.value = st.copy(grid = grid)
    }

    fun save() {
        val st = _state.value
        if (st.selectedClassId.isBlank()) {
            _state.value = st.copy(error = "Select a class first"); return
        }
        _state.value = st.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                val coll = firestore.collection("timetable")
                // Wipe existing for this class
                val existing = coll.whereEqualTo("class_id", st.selectedClassId).get().await()
                existing.documents.forEach { coll.document(it.id).delete().await() }
                // Insert all non-empty slots
                st.grid.forEachIndexed { pIdx, row ->
                    row.forEachIndexed { dIdx, slot ->
                        if (slot.subject.isNotBlank() || slot.teacher.isNotBlank()) {
                            coll.add(mapOf(
                                "class_id" to st.selectedClassId,
                                "period" to st.periods[pIdx].label,
                                "day_of_week" to st.days[dIdx],
                                "subject" to slot.subject,
                                "teacher" to slot.teacher
                            )).await()
                        }
                    }
                }
                _state.value = _state.value.copy(isSaving = false, infoMessage = "Timetable saved")
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun clear() {
        val st = _state.value
        val blank = blankGrid(st.periods.size, st.days.size)
        _state.value = st.copy(grid = blank)
    }

    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
