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

data class SubjectRow(
    val id: String,
    val name: String,
    val classId: String,
    val className: String,
    val code: String
)

data class SubjectsUiState(
    val subjects: List<SubjectRow> = emptyList(),
    val classes: List<Pair<String, String>> = emptyList(),
    val query: String = "",
    val classFilter: String? = null,
    val pageSize: Int = 10,
    val selectedIds: Set<String> = emptySet(),
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<SubjectRow>
        get() {
            val q = query.lowercase().trim()
            return subjects.filter { s ->
                (q.isBlank() || s.name.lowercase().contains(q) || s.code.lowercase().contains(q)) &&
                        (classFilter == null || s.classId == classFilter)
            }
        }
    val paged: List<SubjectRow> get() = filtered.take(pageSize)
    val total: Int get() = filtered.size
}

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectsUiState())
    val state: StateFlow<SubjectsUiState> = _state.asStateFlow()

    init { load() }

    private fun str(d: DocumentSnapshot, key: String) = d.get(key)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val cls = firestore.collection("classes").get().await()
                val classNames = cls.documents.associate { d ->
                    val nm = str(d, "name")
                    val sec = str(d, "section")
                    d.id to if (sec.isBlank()) nm else "$nm - $sec"
                }
                val snap = firestore.collection("subjects").get().await()
                val rows = snap.documents.map { d ->
                    val cid = str(d, "class_id")
                    SubjectRow(
                        id = d.id,
                        name = str(d, "name").ifBlank { "Unnamed" },
                        classId = cid,
                        className = classNames[cid] ?: "-",
                        code = str(d, "code")
                    )
                }.sortedBy { it.name.lowercase() }
                _state.value = _state.value.copy(
                    subjects = rows,
                    classes = classNames.entries.map { it.key to it.value }.sortedBy { it.second },
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
    fun onClassFilter(id: String?) = _state.value.let { _state.value = it.copy(classFilter = id) }
    fun onPageSize(n: Int) = _state.value.let { _state.value = it.copy(pageSize = n) }
    fun toggleSelect(id: String) {
        val cur = _state.value.selectedIds
        _state.value = _state.value.copy(
            selectedIds = if (id in cur) cur - id else cur + id)
    }
    fun askDelete(id: String) = _state.value.let { _state.value = it.copy(confirmDeleteId = id) }
    fun cancelDelete() = _state.value.let { _state.value = it.copy(confirmDeleteId = null) }
    fun doDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.value = _state.value.copy(confirmDeleteId = null)
        viewModelScope.launch {
            try {
                firestore.collection("subjects").document(id).delete().await()
                _state.value = _state.value.copy(
                    subjects = _state.value.subjects.filterNot { it.id == id },
                    infoMessage = "Subject deleted")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Delete failed: ${e.message}")
            }
        }
    }
    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
