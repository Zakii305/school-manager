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

data class EnquiryRow(
    val id: String, val applicant: String, val father: String,
    val phone: String, val className: String, val source: String, val status: String
)

data class AdmissionsUiState(
    val rows: List<EnquiryRow> = emptyList(),
    val query: String = "",
    val statusFilter: String? = null,
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<EnquiryRow>
        get() {
            val q = query.lowercase().trim()
            return rows.filter { r ->
                (q.isBlank() || r.applicant.lowercase().contains(q) ||
                        r.father.lowercase().contains(q) || r.phone.contains(q)) &&
                        (statusFilter == null || r.status.equals(statusFilter, true))
            }
        }
}

@HiltViewModel
class AdmissionsAdminViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _state = MutableStateFlow(AdmissionsUiState())
    val state: StateFlow<AdmissionsUiState> = _state.asStateFlow()

    init { load() }

    private fun s(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val cls = firestore.collection("classes").get().await()
                val classNames = cls.documents.associate { it.id to s(it, "name") }
                val snap = firestore.collection("admission_enquiries").get().await()
                val rows = snap.documents.map { d ->
                    EnquiryRow(
                        id = d.id,
                        applicant = s(d, "applicant_name").ifBlank { "—" },
                        father = s(d, "father_name").ifBlank { "—" },
                        phone = s(d, "phone").ifBlank { "—" },
                        className = classNames[s(d, "desired_class_id")] ?: "—",
                        source = s(d, "source").ifBlank { "—" },
                        status = s(d, "status").ifBlank { "new" }
                    )
                }.sortedByDescending { it.id.toIntOrNull() ?: 0 }

                _state.value = _state.value.copy(rows = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
    fun onStatusFilter(s: String?) = _state.value.let { _state.value = it.copy(statusFilter = s) }
    fun askDelete(id: String) = _state.value.let { _state.value = it.copy(confirmDeleteId = id) }
    fun cancelDelete() = _state.value.let { _state.value = it.copy(confirmDeleteId = null) }
    fun doDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.value = _state.value.copy(confirmDeleteId = null)
        viewModelScope.launch {
            try {
                firestore.collection("admission_enquiries").document(id).delete().await()
                _state.value = _state.value.copy(
                    rows = _state.value.rows.filterNot { it.id == id },
                    infoMessage = "Enquiry deleted")
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
