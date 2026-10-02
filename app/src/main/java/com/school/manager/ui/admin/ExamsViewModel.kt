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

data class ExamRow(
    val id: String, val name: String, val term: String,
    val startDate: String, val endDate: String
)

data class ExamsUiState(
    val exams: List<ExamRow> = emptyList(),
    val query: String = "",
    val pageSize: Int = 10,
    val selectedIds: Set<String> = emptySet(),
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<ExamRow>
        get() {
            val q = query.lowercase().trim()
            return exams.filter { e ->
                q.isBlank() || e.name.lowercase().contains(q) || e.term.lowercase().contains(q)
            }
        }
    val paged: List<ExamRow> get() = filtered.take(pageSize)
    val total: Int get() = filtered.size
}

@HiltViewModel
class ExamsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(ExamsUiState())
    val state: StateFlow<ExamsUiState> = _state.asStateFlow()

    init { load() }

    private fun str(d: DocumentSnapshot, k: String): String {
        val v = d.get(k) ?: return ""
        if (v is com.google.firebase.Timestamp) {
            return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(v.toDate())
        }
        return v.toString()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val snap = firestore.collection("exams").get().await()
                val rows = snap.documents.map { d ->
                    ExamRow(
                        id = d.id,
                        name = str(d, "name").ifBlank { "—" },
                        term = str(d, "term"),
                        startDate = str(d, "start_date"),
                        endDate = str(d, "end_date")
                    )
                }
                _state.value = _state.value.copy(exams = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
    fun onPageSize(n: Int) = _state.value.let { _state.value = it.copy(pageSize = n) }
    fun toggleSelect(id: String) {
        val c = _state.value.selectedIds
        _state.value = _state.value.copy(selectedIds = if (id in c) c - id else c + id)
    }
    fun askDelete(id: String) = _state.value.let { _state.value = it.copy(confirmDeleteId = id) }
    fun cancelDelete() = _state.value.let { _state.value = it.copy(confirmDeleteId = null) }
    fun doDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.value = _state.value.copy(confirmDeleteId = null)
        viewModelScope.launch {
            try {
                firestore.collection("exams").document(id).delete().await()
                _state.value = _state.value.copy(
                    exams = _state.value.exams.filterNot { it.id == id },
                    infoMessage = "Exam deleted")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Delete failed: ${e.message}")
            }
        }
    }
    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
