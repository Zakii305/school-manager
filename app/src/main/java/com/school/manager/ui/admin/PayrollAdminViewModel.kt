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

data class PayrollRow(val id: String, val staff: String, val month: String, val gross: String, val net: String, val status: String)

data class PayrollUiState(
    val rows: List<PayrollRow> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filtered: List<PayrollRow>
        get() {
            val q = query.lowercase().trim()
            return rows.filter { q.isBlank() || it.staff.lowercase().contains(q) }
        }
}

@HiltViewModel
class PayrollAdminViewModel @Inject constructor(private val firestore: FirebaseFirestore) : ViewModel() {
    private val _state = MutableStateFlow(PayrollUiState())
    val state: StateFlow<PayrollUiState> = _state.asStateFlow()

    init { load() }
    private fun s(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val staff = firestore.collection("staff").get().await()
                val names = staff.documents.associate { it.id to s(it, "name") }
                val snap = firestore.collection("payroll").get().await()
                val rows = snap.documents.map { d ->
                    val sid = s(d, "staff_id")
                    val gross = d.getDouble("gross_salary") ?: 0.0
                    val net = d.getDouble("net_salary") ?: gross
                    PayrollRow(
                        id = d.id,
                        staff = names[sid] ?: s(d, "staff_name").ifBlank { "—" },
                        month = s(d, "month").ifBlank { s(d, "created_at").take(7) },
                        gross = "Rs ${String.format("%,.0f", gross)}",
                        net = "Rs ${String.format("%,.0f", net)}",
                        status = s(d, "status").ifBlank { "pending" }
                    )
                }.sortedByDescending { it.month }
                _state.value = _state.value.copy(rows = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
}
