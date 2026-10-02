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

data class GradeRow(
    val id: String, val grade: String,
    val minPct: String, val maxPct: String, val remarks: String
)

data class GradeSettingsUiState(
    val grades: List<GradeRow> = emptyList(),
    // New band form
    val newGrade: String = "",
    val newMin: String = "",
    val newMax: String = "",
    val newRemarks: String = "",
    // Editing row id (null = none)
    val editingId: String? = null,
    val editGrade: String = "",
    val editMin: String = "",
    val editMax: String = "",
    val editRemarks: String = "",
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class GradeSettingsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(GradeSettingsUiState())
    val state: StateFlow<GradeSettingsUiState> = _state.asStateFlow()

    init { load() }

    private fun str(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val snap = firestore.collection("grade_settings").get().await()
                val rows = snap.documents.map { d ->
                    GradeRow(
                        id = d.id,
                        grade = str(d, "grade").ifBlank { str(d, "label") },
                        minPct = str(d, "min_percent").ifBlank { str(d, "min_pct") },
                        maxPct = str(d, "max_percent").ifBlank { str(d, "max_pct") },
                        remarks = str(d, "remarks")
                    )
                }.sortedByDescending { it.minPct.toDoubleOrNull() ?: 0.0 }
                _state.value = _state.value.copy(grades = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onNewGrade(v: String) = _state.value.let { _state.value = it.copy(newGrade = v) }
    fun onNewMin(v: String) = _state.value.let { _state.value = it.copy(newMin = v) }
    fun onNewMax(v: String) = _state.value.let { _state.value = it.copy(newMax = v) }
    fun onNewRemarks(v: String) = _state.value.let { _state.value = it.copy(newRemarks = v) }

    fun startEdit(id: String) {
        val g = _state.value.grades.firstOrNull { it.id == id } ?: return
        _state.value = _state.value.copy(
            editingId = id, editGrade = g.grade, editMin = g.minPct,
            editMax = g.maxPct, editRemarks = g.remarks)
    }
    fun cancelEdit() = _state.value.let { _state.value = it.copy(editingId = null) }
    fun onEditGrade(v: String) = _state.value.let { _state.value = it.copy(editGrade = v) }
    fun onEditMin(v: String) = _state.value.let { _state.value = it.copy(editMin = v) }
    fun onEditMax(v: String) = _state.value.let { _state.value = it.copy(editMax = v) }
    fun onEditRemarks(v: String) = _state.value.let { _state.value = it.copy(editRemarks = v) }

    fun saveEdit() {
        val id = _state.value.editingId ?: return
        val s = _state.value
        viewModelScope.launch {
            try {
                firestore.collection("grade_settings").document(id).update(mapOf(
                    "grade" to s.editGrade.trim(),
                    "min_percent" to (s.editMin.toDoubleOrNull() ?: 0.0),
                    "max_percent" to (s.editMax.toDoubleOrNull() ?: 0.0),
                    "remarks" to s.editRemarks.trim()
                )).await()
                _state.value = _state.value.copy(editingId = null, infoMessage = "Grade updated")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun addBand() {
        val s = _state.value
        if (s.newGrade.isBlank()) {
            _state.value = s.copy(error = "Grade label required"); return
        }
        _state.value = s.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                firestore.collection("grade_settings").add(mapOf(
                    "grade" to s.newGrade.trim(),
                    "min_percent" to (s.newMin.toDoubleOrNull() ?: 0.0),
                    "max_percent" to (s.newMax.toDoubleOrNull() ?: 0.0),
                    "remarks" to s.newRemarks.trim()
                )).await()
                _state.value = _state.value.copy(
                    isSaving = false, newGrade = "", newMin = "", newMax = "", newRemarks = "",
                    infoMessage = "Grade added")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun askDelete(id: String) = _state.value.let { _state.value = it.copy(confirmDeleteId = id) }
    fun cancelDelete() = _state.value.let { _state.value = it.copy(confirmDeleteId = null) }
    fun doDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.value = _state.value.copy(confirmDeleteId = null)
        viewModelScope.launch {
            try {
                firestore.collection("grade_settings").document(id).delete().await()
                _state.value = _state.value.copy(
                    grades = _state.value.grades.filterNot { it.id == id },
                    infoMessage = "Grade deleted")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
