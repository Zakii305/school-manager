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

data class FeeVoucherRow(
    val id: String, val student: String, val amount: String,
    val month: String, val status: String, val dueDate: String
)

data class FeeCollectionUiState(
    val rows: List<FeeVoucherRow> = emptyList(),
    val query: String = "",
    val statusFilter: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<FeeVoucherRow>
        get() {
            val q = query.lowercase().trim()
            return rows.filter { r ->
                (q.isBlank() || r.student.lowercase().contains(q) || r.month.contains(q)) &&
                        (statusFilter == null || r.status.equals(statusFilter, true))
            }
        }
}

@HiltViewModel
class FeeCollectionAdminViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _state = MutableStateFlow(FeeCollectionUiState())
    val state: StateFlow<FeeCollectionUiState> = _state.asStateFlow()

    init { load() }

    private fun s(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val stu = firestore.collection("students").get().await()
                val names = stu.documents.associate { it.id to s(it, "name") }
                val snap = firestore.collection("invoices").get().await()
                val rows = snap.documents.map { d ->
                    val sid = s(d, "student_id")
                    val amount = (d.getDouble("total_amount") ?: d.getDouble("amount") ?: 0.0)
                    FeeVoucherRow(
                        id = d.id,
                        student = names[sid] ?: s(d, "student_name").ifBlank { "—" },
                        amount = "Rs ${String.format("%,.0f", amount)}",
                        month = s(d, "month").ifBlank { s(d, "invoice_date").take(7) },
                        status = s(d, "status").ifBlank { "pending" },
                        dueDate = s(d, "due_date").take(10)
                    )
                }.sortedByDescending { it.id }
                _state.value = _state.value.copy(rows = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
    fun onStatusFilter(s: String?) = _state.value.let { _state.value = it.copy(statusFilter = s) }
}
