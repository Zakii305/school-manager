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

data class ExpenseRow(val id: String, val title: String, val category: String, val amount: String, val date: String, val notes: String)

data class ExpensesUiState(
    val rows: List<ExpenseRow> = emptyList(),
    val query: String = "",
    val confirmDeleteId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val infoMessage: String? = null
) {
    val filtered: List<ExpenseRow>
        get() {
            val q = query.lowercase().trim()
            return rows.filter { q.isBlank() || it.title.lowercase().contains(q) || it.category.lowercase().contains(q) }
        }
}

@HiltViewModel
class ExpensesAdminViewModel @Inject constructor(private val firestore: FirebaseFirestore) : ViewModel() {
    private val _state = MutableStateFlow(ExpensesUiState())
    val state: StateFlow<ExpensesUiState> = _state.asStateFlow()

    init { load() }
    private fun s(d: DocumentSnapshot, k: String) = d.get(k)?.toString() ?: ""

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val snap = firestore.collection("expenses").get().await()
                val rows = snap.documents.map { d ->
                    val amt = d.getDouble("amount") ?: 0.0
                    ExpenseRow(
                        id = d.id,
                        title = s(d, "title").ifBlank { s(d, "description").ifBlank { "Untitled" } },
                        category = s(d, "category").ifBlank { "General" },
                        amount = "Rs ${String.format("%,.0f", amt)}",
                        date = s(d, "expense_date").ifBlank { s(d, "created_at").take(10) },
                        notes = s(d, "notes").take(50)
                    )
                }.sortedByDescending { it.date }
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
                firestore.collection("expenses").document(id).delete().await()
                _state.value = _state.value.copy(rows = _state.value.rows.filterNot { it.id == id }, infoMessage = "Deleted")
            } catch (e: Exception) { _state.value = _state.value.copy(error = e.message) }
        }
    }
    fun consumeInfo() = _state.value.let { _state.value = it.copy(infoMessage = null) }
    fun consumeError() = _state.value.let { _state.value = it.copy(error = null) }
}
