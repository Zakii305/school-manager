package com.school.manager.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class StaffRow(
    val id: String,
    val staffId: String,
    val name: String,
    val designation: String,
    val department: String,
    val phone: String,
    val status: String
)

data class StaffListUiState(
    val staff: List<StaffRow> = emptyList(),
    val query: String = "",
    val departmentFilter: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val confirmDeleteId: String? = null
) {
    val departments: List<String>
        get() = staff.map { it.department }.filter { it.isNotBlank() }
            .distinct().sorted()

    val filtered: List<StaffRow>
        get() {
            val q = query.lowercase().trim()
            return staff.filter { s ->
                (q.isBlank()
                    || s.name.lowercase().contains(q)
                    || s.staffId.lowercase().contains(q)
                    || s.designation.lowercase().contains(q)
                    || s.phone.contains(q)) &&
                        (departmentFilter == null || s.department == departmentFilter)
            }
        }
}

@HiltViewModel
class StaffListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(StaffListUiState())
    val state: StateFlow<StaffListUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val snap = firestore.collection("staff").get().await()
                val rows = snap.documents.map { d ->
                    val name = d.getString("name")
                        ?: d.getString("full_name") ?: "Unnamed"
                    StaffRow(
                        id = d.id,
                        staffId = d.getString("staff_id") ?: "",
                        name = name,
                        designation = d.getString("designation") ?: "-",
                        department = d.getString("department") ?: "",
                        phone = d.getString("phone") ?: "-",
                        status = d.getString("status") ?: "active"
                    )
                }.sortedBy { it.staffId.toIntOrNull() ?: Int.MAX_VALUE }
                _state.value = _state.value.copy(staff = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load staff"
                )
            }
        }
    }

    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
    fun onDepartmentFilter(d: String?) = _state.value.let { _state.value = it.copy(departmentFilter = d) }

    fun askDelete(id: String) = _state.value.let { _state.value = it.copy(confirmDeleteId = id) }
    fun cancelDelete() = _state.value.let { _state.value = it.copy(confirmDeleteId = null) }

    fun doDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.value = _state.value.copy(confirmDeleteId = null)
        viewModelScope.launch {
            try {
                firestore.collection("staff").document(id).delete().await()
                _state.value = _state.value.copy(
                    staff = _state.value.staff.filterNot { it.id == id }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
