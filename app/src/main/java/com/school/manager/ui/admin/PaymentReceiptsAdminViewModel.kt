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

data class ReceiptRow(val id: String, val student: String, val amount: String, val method: String, val date: String, val ref: String)

data class ReceiptsUiState(
    val rows: List<ReceiptRow> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filtered: List<ReceiptRow>
        get() {
            val q = query.lowercase().trim()
            return rows.filter { r -> q.isBlank() || r.student.lowercase().contains(q) || r.ref.contains(q) }
        }
}

@HiltViewModel
class PaymentReceiptsAdminViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _state = MutableStateFlow(ReceiptsUiState())
    val state: StateFlow<ReceiptsUiState> = _state.asStateFlow()

    init { load() }
    private fun s(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val stu = firestore.collection("students").get().await()
                val names = stu.documents.associate { it.id to s(it, "name") }
                val snap = firestore.collection("invoice_payments").get().await()
                val rows = snap.documents.map { d ->
                    val sid = s(d, "student_id")
                    val amt = d.getDouble("amount") ?: 0.0
                    ReceiptRow(
                        id = d.id,
                        student = names[sid] ?: "—",
                        amount = "Rs ${String.format("%,.0f", amt)}",
                        method = s(d, "method").ifBlank { "Cash" },
                        date = s(d, "created_at").take(10),
                        ref = s(d, "receipt_no").ifBlank { d.id.takeLast(6) }
                    )
                }.sortedByDescending { it.date }
                _state.value = _state.value.copy(rows = rows, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }
    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
}
