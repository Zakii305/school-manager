package com.school.manager.ui.fees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.UserRoles
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class FeeClassOption(val id: String, val name: String)

data class FeeStudentOption(
    val uid: String,
    val name: String,
    val className: String,
    val pendingAmount: Double
)

data class FeeHeadOption(val id: String, val label: String, val amount: Double)

data class FeeCollectionUiState(
    val isLoading: Boolean = true,
    val classes: List<FeeClassOption> = emptyList(),
    val students: List<FeeStudentOption> = emptyList(),
    val selectedClassId: String = "",
    val selectedSection: String = "",
    val selectedStudent: FeeStudentOption? = null,
    val pendingFees: List<FeeHeadOption> = emptyList(),
    val selectedFeeHeadId: String = "",
    val receivingNow: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash",
    val receiptNo: String = "",
    val txnRef: String = "",
    val paymentNote: String = "",
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
) {
    val totalPending: Double get() = pendingFees.sumOf { it.amount }
}

@HiltViewModel
class FeeCollectionViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _ui = MutableStateFlow(FeeCollectionUiState())
    val uiState: StateFlow<FeeCollectionUiState> = _ui.asStateFlow()

    init {
        loadClasses()
        loadAllStudents()
    }

    private fun loadClasses() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val list = snap.documents.map { d ->
                    FeeClassOption(d.id, d.getString("name") ?: "-")
                }.sortedBy { it.name }
                _ui.update { it.copy(classes = list, isLoading = false) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadAllStudents() {
        viewModelScope.launch {
            try {
                val users = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo("role", UserRoles.STUDENT).get().await()
                val fees = firestore.collection(FirestoreCollections.FEES).get().await()

                val pendingByStudent = mutableMapOf<String, Double>()
                for (doc in fees.documents) {
                    val sid = doc.getString("studentId") ?: continue
                    val paid = doc.getBoolean("paid") ?: false
                    val amt = doc.getDouble("amount") ?: 0.0
                    if (!paid) pendingByStudent[sid] = (pendingByStudent[sid] ?: 0.0) + amt
                }

                val classMap = _ui.value.classes.associate { it.id to it.name }
                val list = users.documents.map { d ->
                    val cid = d.getString("classId") ?: ""
                    FeeStudentOption(
                        uid = d.id,
                        name = d.getString("name") ?: "-",
                        className = classMap[cid] ?: "-",
                        pendingAmount = pendingByStudent[d.id] ?: 0.0
                    )
                }
                _ui.update { it.copy(students = list) }
            } catch (_: Exception) { }
        }
    }

    fun selectClass(classId: String) {
        _ui.update { it.copy(selectedClassId = classId, selectedStudent = null, pendingFees = emptyList()) }
    }

    fun selectSection(section: String) = _ui.update { it.copy(selectedSection = section) }

    fun selectStudent(student: FeeStudentOption) {
        _ui.update {
            it.copy(
                selectedStudent = student,
                selectedFeeHeadId = "",
                receivingNow = student.pendingAmount.toInt().toString(),
                receiptNo = "RCPT-${System.currentTimeMillis().toString().takeLast(8)}"
            )
        }
        loadPendingForStudent(student.uid)
    }

    private fun loadPendingForStudent(uid: String) {
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.FEES)
                    .whereEqualTo("studentId", uid)
                    .whereEqualTo("paid", false)
                    .get().await()
                val heads = snap.documents.map { d ->
                    FeeHeadOption(
                        id = d.id,
                        label = d.getString("description") ?: "Fee",
                        amount = d.getDouble("amount") ?: 0.0
                    )
                }
                _ui.update {
                    it.copy(pendingFees = heads, selectedFeeHeadId = heads.firstOrNull()?.id ?: "")
                }
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun onReceivingNow(v: String) =
        _ui.update { it.copy(receivingNow = v.filter { c -> c.isDigit() || c == '.' }) }

    fun onPaymentDate(millis: Long) = _ui.update { it.copy(paymentDate = millis) }
    fun onPaymentMethod(v: String) = _ui.update { it.copy(paymentMethod = v) }
    fun onReceiptNo(v: String) = _ui.update { it.copy(receiptNo = v) }
    fun onTxnRef(v: String) = _ui.update { it.copy(txnRef = v) }
    fun onPaymentNote(v: String) = _ui.update { it.copy(paymentNote = v) }
    fun onFeeHead(id: String) = _ui.update { it.copy(selectedFeeHeadId = id) }

    fun recordPayment(generateInvoice: Boolean) {
        val s = _ui.value
        val student = s.selectedStudent ?: run {
            _ui.update { it.copy(error = "Select a student first") }
            return
        }
        val head = s.pendingFees.firstOrNull { it.id == s.selectedFeeHeadId } ?: run {
            _ui.update { it.copy(error = "Select a fee head") }
            return
        }
        val amount = s.receivingNow.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            _ui.update { it.copy(error = "Enter amount received") }
            return
        }

        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val uid = auth.currentUser?.uid ?: ""
                val receipt = s.receiptNo.ifBlank {
                    "RCPT-${System.currentTimeMillis().toString().takeLast(8)}"
                }

                firestore.collection(FirestoreCollections.FEES).document(head.id)
                    .update(mapOf(
                        "paid" to true,
                        "paidAt" to s.paymentDate,
                        "paymentMethod" to s.paymentMethod,
                        "receiptNo" to receipt,
                        "txnRef" to s.txnRef,
                        "paymentNote" to s.paymentNote,
                        "receivedBy" to uid,
                        "receivedAmount" to amount
                    )).await()

                _ui.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Payment recorded — Receipt: $receipt",
                        receivingNow = "",
                        paymentNote = "",
                        txnRef = ""
                    )
                }
                loadAllStudents()
                loadPendingForStudent(student.uid)
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
