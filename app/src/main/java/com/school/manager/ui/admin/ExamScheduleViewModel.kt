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

data class DateSheetRow(
    val id: String, val examId: String, val examName: String, val term: String,
    val dateRange: String, val papers: Int, val classes: Int
)

data class ExamScheduleUiState(
    val sheets: List<DateSheetRow> = emptyList(),
    val exams: List<Pair<String, String>> = emptyList(),
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
)

@HiltViewModel
class ExamScheduleViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(ExamScheduleUiState())
    val state: StateFlow<ExamScheduleUiState> = _state.asStateFlow()

    init { load() }

    private fun str(d: DocumentSnapshot, k: String): String {
        val v = d.get(k) ?: return ""
        if (v is com.google.firebase.Timestamp)
            return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(v.toDate())
        return v.toString()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val examSnap = firestore.collection("exams").get().await()
                val examNames = examSnap.documents.associate {
                    it.id to (str(it, "name").ifBlank { "Unnamed" })
                }
                val schedSnap = firestore.collection("exam_schedule").get().await()

                val sheets = schedSnap.documents.map { d ->
                    val examId = str(d, "exam_id")
                    val start = str(d, "start_date").ifBlank { str(d, "exam_date") }
                    val end = str(d, "end_date")
                    val range = if (end.isNotBlank() && end != start) "$start – $end" else start
                    DateSheetRow(
                        id = d.id,
                        examId = examId,
                        examName = examNames[examId] ?: str(d, "exam_name").ifBlank { "—" },
                        term = str(d, "term"),
                        dateRange = range.ifBlank { "—" },
                        papers = (d.get("papers") as? Number)?.toInt() ?: 0,
                        classes = (d.get("classes") as? Number)?.toInt() ?: 0
                    )
                }
                _state.value = _state.value.copy(
                    sheets = sheets,
                    exams = examSnap.documents.map { it.id to (examNames[it.id] ?: "") }
                        .filter { it.second.isNotBlank() },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
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
                firestore.collection("exam_schedule").document(id).delete().await()
                _state.value = _state.value.copy(
                    sheets = _state.value.sheets.filterNot { it.id == id },
                    infoMessage = "Date sheet deleted")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Delete failed: ${e.message}")
            }
        }
    }
    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
