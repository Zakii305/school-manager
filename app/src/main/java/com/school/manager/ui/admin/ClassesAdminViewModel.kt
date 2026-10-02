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

data class ClassRow(
    val id: String, val name: String, val sections: String,
    val students: Int, val teacher: String
)

data class ClassesUiState(
    val rows: List<ClassRow> = emptyList(),
    val query: String = "",
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<ClassRow>
        get() {
            val q = query.lowercase().trim()
            return rows.filter { r -> q.isBlank() || r.name.lowercase().contains(q) }
        }
}

@HiltViewModel
class ClassesAdminViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _state = MutableStateFlow(ClassesUiState())
    val state: StateFlow<ClassesUiState> = _state.asStateFlow()

    init { load() }

    private fun s(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val cls = firestore.collection("classes").get().await()
                val stu = firestore.collection("students").get().await()
                val staff = firestore.collection("staff").get().await()
                val staffNames = staff.documents.associate { it.id to s(it, "name") }

                val counts = mutableMapOf<String, Int>()
                stu.documents.forEach { d ->
                    val cid = s(d, "class_id")
                    if (cid.isNotBlank()) counts[cid] = (counts[cid] ?: 0) + 1
                }

                val rows = cls.documents.map { d ->
                    ClassRow(
                        id = d.id,
                        name = s(d, "name").ifBlank { "Unnamed" },
                        sections = s(d, "section").ifBlank { "A" },
                        students = counts[d.id] ?: 0,
                        teacher = staffNames[s(d, "class_teacher_id")] ?: "—"
                    )
                }.sortedBy { it.name.lowercase() }

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
                firestore.collection("classes").document(id).delete().await()
                _state.value = _state.value.copy(
                    rows = _state.value.rows.filterNot { it.id == id },
                    infoMessage = "Class deleted")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
