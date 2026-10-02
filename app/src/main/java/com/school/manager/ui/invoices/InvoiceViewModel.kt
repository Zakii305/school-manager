package com.school.manager.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class InvoiceRow(
    val id: String,
    val studentName: String,
    val month: String,
    val total: Double,
    val paid: Double,
    val status: String
) {
    val pending: Double get() = total - paid
}

data class InvoiceState(
    val isLoading: Boolean = true,
    val rows: List<InvoiceRow> = emptyList(),
    val filtered: List<InvoiceRow> = emptyList(),
    val query: String = "",
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _ui = MutableStateFlow(InvoiceState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val users = firestore.collection(FirestoreCollections.USERS).get().await()
                val nameMap = users.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }

                val snap = firestore.collection("invoices").get().await()
                val fmt = SimpleDateFormat("MMM yyyy", Locale.US)

                val rows = snap.documents.map { d ->
                    val sid = d.getString("studentId") ?: ""
                    val total = d.getDouble("total") ?: 0.0
                    val paid = d.getDouble("paidAmount") ?: 0.0
                    val st = when {
                        paid >= total && total > 0 -> "Paid"
                        paid > 0 -> "Partial"
                        else -> "Pending"
                    }
                    InvoiceRow(
                        id = d.id,
                        studentName = nameMap[sid] ?: "-",
                        month = d.getLong("month")?.let { fmt.format(Date(it)) } ?: "-",
                        total = total,
                        paid = paid,
                        status = st
                    )
                }.sortedByDescending { it.id }

                val collected = rows.sumOf { it.paid }
                val pending = rows.sumOf { it.pending }

                _ui.update {
                    it.copy(
                        isLoading = false,
                        rows = rows,
                        filtered = rows,
                        totalCollected = collected,
                        totalPending = pending
                    )
                }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onQuery(q: String) {
        val s = _ui.value
        val f = if (q.isBlank()) s.rows else s.rows.filter {
            it.studentName.contains(q, true) || it.month.contains(q, true)
        }
        _ui.update { it.copy(query = q, filtered = f) }
    }

    fun generateBulkVouchers(studentIds: List<String>, month: Long, amount: Double) {
        if (studentIds.isEmpty() || amount <= 0) {
            _ui.update { it.copy(error = "Select students and enter amount") }
            return
        }
        _ui.update { it.copy(isLoading = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val sid = SessionManager.current.schoolId
                val batch = firestore.batch()
                for (uid in studentIds) {
                    val ref = firestore.collection("invoices").document()
                    val data = mutableMapOf<String, Any>(
                        "studentId" to uid,
                        "month" to month,
                        "total" to amount,
                        "paidAmount" to 0.0,
                        "items" to listOf(mapOf("label" to "Monthly Fee", "amount" to amount)),
                        "createdAt" to System.currentTimeMillis(),
                        "dueDate" to (month + 10L * 24 * 3600 * 1000)
                    )
                    if (sid.isNotBlank()) data["schoolId"] = sid
                    batch.set(ref, data)
                }
                batch.commit().await()
                _ui.update { it.copy(isLoading = false, successMessage = "${studentIds.size} vouchers generated ✅") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun recordPayment(invoiceId: String, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("invoices").document(invoiceId)
                val snap = doc.get().await()
                val current = snap.getDouble("paidAmount") ?: 0.0
                doc.update(
                    "paidAmount", current + amount,
                    "lastPaidAt", System.currentTimeMillis()
                ).await()
                _ui.update { it.copy(successMessage = "Payment recorded ✅") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
