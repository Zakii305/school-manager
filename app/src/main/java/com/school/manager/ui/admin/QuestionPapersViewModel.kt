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

data class QPRow(
    val id: String, val subject: String, val className: String,
    val type: String, val date: String, val maxMarks: String
)

data class QPUiState(
    val rows: List<QPRow> = emptyList(),
    val query: String = "",
    val pageSize: Int = 10,
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<QPRow>
        get() {
            val q = query.lowercase().trim()
            return rows.filter { r ->
                q.isBlank() || r.subject.lowercase().contains(q) ||
                        r.className.lowercase().contains(q)
            }
        }
    val paged: List<QPRow> get() = filtered.take(pageSize)
    val total: Int get() = filtered.size
}

@HiltViewModel
class QuestionPapersViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _state = MutableStateFlow(QPUiState())
    val state: StateFlow<QPUiState> = _state.asStateFlow()

    init { load() }

    private fun s(d: DocumentSnapshot, k: String): String {
        val v = d.get(k) ?: return ""
        if (v is com.google.firebase.Timestamp)
            return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(v.toDate())
        return v.toString()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val cls = firestore.collection("classes").get().await()
                val classNames = cls.documents.associate { d ->
                    val nm = s(d, "name"); val sec = s(d, "section")
                    d.id to if (sec.isBlank()) nm else "$nm - $sec"
                }
                val subj = firestore.collection("subjects").get().await()
                val subjectNames = subj.documents.associate { it.id to s(it, "name") }
                val snap = firestore.collection("question_papers").get().await()
                val rows = snap.documents.map { d ->
                    val sid = s(d, "subject_id")
                    val cid = s(d, "class_id")
                    QPRow(
                        id = d.id,
                        subject = subjectNames[sid] ?: s(d, "subject").ifBlank { "—" },
                        className = classNames[cid] ?: s(d, "class").ifBlank { "—" },
                        type = s(d, "type").ifBlank { "Both" },
                        date = s(d, "exam_date").ifBlank { s(d, "date").ifBlank { "—" } },
                        maxMarks = s(d, "max_marks").ifBlank { "—" }
                    )
                }
                _state.value = _state.value.copy(rows = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
    fun onPageSize(n: Int) = _state.value.let { _state.value = it.copy(pageSize = n) }
    fun askDelete(id: String) = _state.value.let { _state.value = it.copy(confirmDeleteId = id) }
    fun cancelDelete() = _state.value.let { _state.value = it.copy(confirmDeleteId = null) }
    fun doDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.value = _state.value.copy(confirmDeleteId = null)
        viewModelScope.launch {
            try {
                firestore.collection("question_papers").document(id).delete().await()
                _state.value = _state.value.copy(
                    rows = _state.value.rows.filterNot { it.id == id },
                    infoMessage = "Deleted")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
