package com.school.manager.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class SiblingFeeRow(
    val feeId: String,
    val studentName: String,
    val className: String,
    val description: String,
    val amount: Double
)

data class FamilyGroup(
    val familyId: String,
    val parentName: String,
    val parentEmail: String,
    val childrenUids: List<String>,
    val childCount: Int,
    val pendingTotal: Double
)

data class FamilyFeeUiState(
    val isLoading: Boolean = true,
    val families: List<FamilyGroup> = emptyList(),
    val selectedFamily: FamilyGroup? = null,
    val siblingFees: List<SiblingFeeRow> = emptyList(),
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
) {
    val siblingTotal: Double get() = siblingFees.sumOf { it.amount }
}

@HiltViewModel
class FamilyFeeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _ui = MutableStateFlow(FamilyFeeUiState())
    val uiState: StateFlow<FamilyFeeUiState> = _ui.asStateFlow()

    init { loadFamilies() }

    fun loadFamilies() {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val users = firestore.collection(FirestoreCollections.USERS).get().await()
                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classSnap.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }
                val feesSnap = firestore.collection(FirestoreCollections.FEES).get().await()

                val pendingByStudent = mutableMapOf<String, Double>()
                for (d in feesSnap.documents) {
                    val sid = d.getString("studentId") ?: continue
                    val paid = d.getBoolean("paid") ?: false
                    val amt = d.getDouble("amount") ?: 0.0
                    if (!paid) pendingByStudent[sid] = (pendingByStudent[sid] ?: 0.0) + amt
                }

                val list = users.documents.mapNotNull { d ->
                    if (d.getString("role") != UserRoles.PARENT) return@mapNotNull null
                    @Suppress("UNCHECKED_CAST")
                    val children = (d.get("children") as? List<String>) ?: emptyList()
                    val pending = children.sumOf { pendingByStudent[it] ?: 0.0 }
                    FamilyGroup(
                        familyId = d.id,
                        parentName = d.getString("name") ?: "Parent",
                        parentEmail = d.getString("email") ?: "",
                        childrenUids = children,
                        childCount = children.size,
                        pendingTotal = pending
                    )
                }.sortedByDescending { it.pendingTotal }

                _ui.update { it.copy(isLoading = false, families = list) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectFamily(family: FamilyGroup) {
        _ui.update { it.copy(selectedFamily = family, siblingFees = emptyList()) }
        viewModelScope.launch {
            try {
                val userSnap = firestore.collection(FirestoreCollections.USERS).get().await()
                val nameMap = userSnap.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }
                val classMap = userSnap.documents.associate {
                    it.id to (it.getString("classId") ?: "-")
                }
                val classNames = firestore.collection(FirestoreCollections.CLASSES).get().await()
                    .documents.associate { it.id to (it.getString("name") ?: "-") }

                val rows = mutableListOf<SiblingFeeRow>()
                for (childUid in family.childrenUids) {
                    val snap = firestore.collection(FirestoreCollections.FEES)
                        .whereEqualTo("studentId", childUid)
                        .whereEqualTo("paid", false)
                        .get().await()
                    for (d in snap.documents) {
                        rows.add(
                            SiblingFeeRow(
                                feeId = d.id,
                                studentName = nameMap[childUid] ?: "-",
                                className = classNames[classMap[childUid] ?: ""] ?: "-",
                                description = d.getString("description") ?: "Fee",
                                amount = d.getDouble("amount") ?: 0.0
                            )
                        )
                    }
                }
                _ui.update { it.copy(siblingFees = rows) }
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun payAll() {
        val s = _ui.value
        val family = s.selectedFamily
        if (family == null) {
            _ui.update { it.copy(error = "Select a family first") }
            return
        }
        if (s.siblingFees.isEmpty()) {
            _ui.update { it.copy(error = "No pending fees to pay"); return }
        }
        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val receipt = "RCPT-FAM-${System.currentTimeMillis().toString().takeLast(8)}"
                val batch = firestore.batch()
                for (row in s.siblingFees) {
                    val ref = firestore.collection(FirestoreCollections.FEES).document(row.feeId)
                    batch.update(ref, mapOf(
                        "paid" to true,
                        "paidAt" to now,
                        "receiptNo" to receipt,
                        "paymentMethod" to "Family",
                        "familyId" to family.familyId
                    ))
                }
                batch.commit().await()
                _ui.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "${s.siblingFees.size} fees cleared — Receipt: $receipt",
                        siblingFees = emptyList(),
                        selectedFamily = null
                    )
                }
                loadFamilies()
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
