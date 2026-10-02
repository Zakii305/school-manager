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

data class SyllabusRow(
    val id: String, val title: String, val className: String,
    val subject: String, val term: String
)

data class SyllabusUiState(
    val rows: List<SyllabusRow> = emptyList(),
    val query: String = "",
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<SyllabusRow>
        get() {
            val q = query.lowercase().trim()
            return rows.filter { r ->
                q.isBlank() || r.title.lowercase().contains(q) || r.subject.lowercase().contains(q)
            }
        }
}

@HiltViewModel
class SyllabusAdminViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _state = MutableStateFlow(SyllabusUiState())
    val state: StateFlow<SyllabusUiState> = _state.asStateFlow()

    init { load() }

    private fun s(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val cls = firestore.collection("classes").get().await()
                val classNames = cls.documents.associate { it.id to s(it, "name") }
                val subj = firestore.collection("subjects").get().await()
                val subjNames = subj.documents.associate { it.id to s(it, "name") }
                val snap = firestore.collection("syllabus").get().await()
                val rows = snap.documents.map { d ->
                    SyllabusRow(
                        id = d.id,
                        title = s(d, "title").ifBlank { s(d, "name").ifBlank { "Untitled" } },
                        className = classNames[s(d, "class_id")] ?: s(d, "class").ifBlank { "—" },
                        subject = subjNames[s(d, "subject_id")] ?: s(d, "subject").ifBlank { "—" },
                        term = s(d, "term").ifBlank { "—" }
                    )
                }
                _state.value = _state.value.copy(rows = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
    fun askDelete(id: String) = _state.value.let { _state.value = it.copy(confirmDeleteId = id) }
    fun cancelDelete() = _state.value.let { _state.value = it.copy(confirmDeleteId = null) }
    fun doDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.value = _state.value.copy(confirmDeleteId = null)
        viewModelScope.launch {
            try {
                firestore.collection("syllabus").document(id).delete().await()
                _state.value = _state.value.copy(
                    rows = _state.value.rows.filterNot { it.id == id },
                    infoMessage = "Syllabus deleted")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
