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

data class StudentRow(
    val id: String,
    val admissionNo: String,
    val name: String,
    val classId: String,
    val className: String,
    val fatherName: String,
    val phone: String,
    val status: String
)

data class StudentsUiState(
    val students: List<StudentRow> = emptyList(),
    val classes: List<Pair<String, String>> = emptyList(),
    val query: String = "",
    val classFilter: String? = null,
    val statusFilter: String? = null,
    val pageSize: Int = 10,
    val selectedIds: Set<String> = emptySet(),
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<StudentRow>
        get() {
            val q = query.lowercase().trim()
            return students.filter { s ->
                (q.isBlank()
                    || s.name.lowercase().contains(q)
                    || s.admissionNo.lowercase().contains(q)
                    || s.fatherName.lowercase().contains(q)
                    || s.phone.contains(q)) &&
                        (classFilter == null || s.classId == classFilter) &&
                        (statusFilter == null || s.status.equals(statusFilter, true))
            }
        }
    val paged: List<StudentRow> get() = filtered.take(pageSize)
    val total: Int get() = filtered.size
}

@HiltViewModel
class StudentsListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(StudentsUiState())
    val state: StateFlow<StudentsUiState> = _state.asStateFlow()

    init { load() }

    /** Safely read any field as String, regardless of its Firestore type. */
    private fun str(d: DocumentSnapshot, key: String): String {
        val v = d.get(key) ?: return ""
        return v.toString()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val classesSnap = firestore.collection("classes").get().await()
                val classNames = classesSnap.documents.associate { d ->
                    val nm = str(d, "name")
                    val sec = str(d, "section")
                    d.id to if (sec.isBlank()) nm else "$nm - Section $sec"
                }
                val snap = firestore.collection("students").get().await()
                val rows = snap.documents.map { d ->
                    val cid = str(d, "class_id")
                    StudentRow(
                        id = d.id,
                        admissionNo = str(d, "admission_no"),
                        name = str(d, "name"),
                        classId = cid,
                        className = classNames[cid] ?: "-",
                        fatherName = str(d, "father_name").ifBlank { "-" },
                        phone = str(d, "phone").ifBlank { str(d, "father_contact").ifBlank { "-" } },
                        status = str(d, "status").ifBlank { "active" }
                    )
                }
                val sorted = rows.sortedWith(
                    compareByDescending<StudentRow> {
                        it.admissionNo.toIntOrNull() ?: 0
                    }.thenBy { it.name }
                )
                _state.value = _state.value.copy(
                    students = sorted,
                    classes = classNames.entries.map { it.key to it.value }
                        .sortedBy { it.second },
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load students"
                )
            }
        }
    }

    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
    fun onClassFilter(id: String?) = _state.value.let { _state.value = it.copy(classFilter = id) }
    fun onStatusFilter(s: String?) = _state.value.let { _state.value = it.copy(statusFilter = s) }
    fun onPageSize(n: Int) = _state.value.let { _state.value = it.copy(pageSize = n) }

    fun toggleSelect(id: String) {
        val cur = _state.value.selectedIds
        _state.value = _state.value.copy(
            selectedIds = if (id in cur) cur - id else cur + id
        )
    }
    fun clearSelection() = _state.value.let { _state.value = it.copy(selectedIds = emptySet()) }

    fun askDelete(id: String) = _state.value.let { _state.value = it.copy(confirmDeleteId = id) }
    fun cancelDelete() = _state.value.let { _state.value = it.copy(confirmDeleteId = null) }

    fun doDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.value = _state.value.copy(confirmDeleteId = null)
        viewModelScope.launch {
            try {
                firestore.collection("students").document(id).delete().await()
                _state.value = _state.value.copy(
                    students = _state.value.students.filterNot { it.id == id },
                    infoMessage = "Student deleted"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Delete failed: ${e.message}")
            }
        }
    }

    fun duplicate(id: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("students").document(id).get().await()
                if (!doc.exists()) return@launch
                val data = doc.data?.toMutableMap() ?: return@launch
                data.remove("created_at")
                data["name"] = (data["name"] as? String ?: "") + " (copy)"
                data["admission_no"] = ""
                firestore.collection("students").add(data).await()
                _state.value = _state.value.copy(infoMessage = "Student duplicated")
                load()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Duplicate failed: ${e.message}")
            }
        }
    }

    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
