package com.school.manager.ui.timetable

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
import javax.inject.Inject

data class BulkUiState(
    val isLoading: Boolean = true,
    val classId: String = "",
    val className: String = "",
    val slots: List<TimetableSlot> = emptyList(),
    val subjects: List<SubjectOption> = emptyList(),
    val selected: Set<String> = emptySet(),
    val editMode: Boolean = false,
    val isSaving: Boolean = false,
    val success: String? = null,
    val error: String? = null
)

@HiltViewModel
class BulkTimetableViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _ui = MutableStateFlow(BulkUiState())
    val uiState: StateFlow<BulkUiState> = _ui.asStateFlow()

    fun key(day: String, period: Int) = "${day}_$period"

    fun load(classId: String) {
        _ui.value = _ui.value.copy(isLoading = true, classId = classId, error = null)
        viewModelScope.launch {
            try {
                val cd = firestore.collection(FirestoreCollections.CLASSES).document(classId).get().await()
                val cname = cd.getString("name") ?: "-"

                val slotSnap = firestore.collection(FirestoreCollections.TIMETABLE)
                    .whereEqualTo("classId", classId).get().await()
                val slots = slotSnap.documents.map { d ->
                    TimetableSlot(
                        id = d.id, classId = classId,
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

                _ui.value = _ui.value.copy(
                    isLoading = false, className = cname, slots = slots, subjects = subjects
                )
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleEditMode() = _ui.value.let {
        _ui.value = it.copy(editMode = !it.editMode, selected = if (!it.editMode) it.selected else emptySet())
    }

    fun toggleCell(day: String, period: Int) {
        if (!_ui.value.editMode) return
        val k = key(day, period)
        val sel = _ui.value.selected.toMutableSet()
        if (sel.contains(k)) sel.remove(k) else sel.add(k)
        _ui.value = _ui.value.copy(selected = sel)
    }

    fun selectDay(day: String) {
        if (!_ui.value.editMode) return
        val sel = _ui.value.selected.toMutableSet()
        (1..MAX_PERIODS).forEach { sel.add(key(day, it)) }
        _ui.value = _ui.value.copy(selected = sel)
    }

    fun selectAll() {
        if (!_ui.value.editMode) return
        val sel = mutableSetOf<String>()
        WEEK_DAYS.forEach { d -> (1..MAX_PERIODS).forEach { sel.add(key(d, it)) } }
        _ui.value = _ui.value.copy(selected = sel)
    }

    fun clearSelection() = _ui.value.let { _ui.value = it.copy(selected = emptySet()) }

    fun applySubjectToSelected(subject: SubjectOption) {
        val s = _ui.value
        if (s.selected.isEmpty()) {
            _ui.value = s.copy(error = "Select at least one cell")
            return
        }
        _ui.value = s.copy(isSaving = true, error = null, success = null)
        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                val collection = firestore.collection(FirestoreCollections.TIMETABLE)

                for (k in s.selected) {
                    val parts = k.split("_")
                    val day = parts.dropLast(1).joinToString("_")
                    val period = parts.last().toIntOrNull() ?: continue

                    val existing = s.slots.firstOrNull { it.day == day && it.period == period }
                    val data = mapOf(
                        "classId" to s.classId,
                        "day" to day,
                        "period" to period,
                        "subjectId" to subject.id,
                        "subjectName" to subject.name,
                        "teacherId" to subject.teacherId,
                        "teacherName" to subject.teacherName,
                        "startTime" to "09:00",
                        "endTime" to "09:45",
                        "updatedAt" to System.currentTimeMillis()
                    )
                    val ref = if (existing != null) collection.document(existing.id)
                              else collection.document()
                    batch.set(ref, data)
                }
                batch.commit().await()
                _ui.value = _ui.value.copy(
                    isSaving = false,
                    selected = emptySet(),
                    success = "Applied ${subject.name} to ${s.selected.size} cells"
                )
                load(s.classId)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun copyDay(source: String, targets: List<String>) {
        val s = _ui.value
        if (targets.isEmpty()) return
        _ui.value = s.copy(isSaving = true, error = null, success = null)
        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                val collection = firestore.collection(FirestoreCollections.TIMETABLE)

                for (tgt in targets) {
                    if (tgt == source) continue
                    for (p in 1..MAX_PERIODS) {
                        val src = s.slots.firstOrNull { it.day == source && it.period == p }
                        val tgtSlot = s.slots.firstOrNull { it.day == tgt && it.period == p }
                        val ref = if (tgtSlot != null) collection.document(tgtSlot.id)
                                  else collection.document()
                        if (src == null) {
                            if (tgtSlot != null) batch.delete(collection.document(tgtSlot.id))
                        } else {
                            batch.set(ref, mapOf(
                                "classId" to s.classId,
                                "day" to tgt,
                                "period" to p,
                                "subjectId" to src.subjectId,
                                "subjectName" to src.subjectName,
                                "teacherId" to src.teacherId,
                                "teacherName" to src.teacherName,
                                "startTime" to src.startTime,
                                "endTime" to src.endTime,
                                "updatedAt" to System.currentTimeMillis()
                            ))
                        }
                    }
                }
                batch.commit().await()
                _ui.value = _ui.value.copy(
                    isSaving = false,
                    success = "Copied $source → ${targets.joinToString(", ")}"
                )
                load(s.classId)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun deleteSelected() {
        val s = _ui.value
        if (s.selected.isEmpty()) return
        _ui.value = s.copy(isSaving = true, error = null, success = null)
        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                val collection = firestore.collection(FirestoreCollections.TIMETABLE)

                var count = 0
                for (k in s.selected) {
                    val parts = k.split("_")
                    val day = parts.dropLast(1).joinToString("_")
                    val period = parts.last().toIntOrNull() ?: continue
                    val existing = s.slots.firstOrNull { it.day == day && it.period == period }
                    if (existing != null) {
                        batch.delete(collection.document(existing.id))
                        count++
                    }
                }
                batch.commit().await()
                _ui.value = _ui.value.copy(
                    isSaving = false, selected = emptySet(),
                    success = "Cleared $count slots"
                )
                load(s.classId)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun clearMessages() = _ui.value.let { _ui.value = it.copy(error = null, success = null) }
}
