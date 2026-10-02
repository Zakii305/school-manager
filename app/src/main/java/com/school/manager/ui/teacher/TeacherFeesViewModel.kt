package com.school.manager.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.SessionManager
import com.school.manager.util.UserRoles
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

data class VoucherRow(
    val id: String,
    val studentName: String,
    val className: String,
    val month: String,
    val total: Double,
    val status: String
)

data class FeeClassOption(val id: String, val name: String)

data class TeacherFeesUiState(
    val isLoading: Boolean = true,
    val rows: List<VoucherRow> = emptyList(),
    val classes: List<FeeClassOption> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TeacherFeesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _ui = MutableStateFlow(TeacherFeesUiState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val users = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo("role", UserRoles.STUDENT).get().await()
                val nameMap = users.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }
                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classSnap.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }
                val studentClassMap = users.documents.associate {
                    it.id to (classMap[it.getString("classId")] ?: "-")
                }
                val classes = classSnap.documents.map {
                    FeeClassOption(it.id, it.getString("name") ?: "-")
                }.sortedBy { it.name }

                val fees = firestore.collection(FirestoreCollections.FEES).get().await()
                val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.US)
                val rows = fees.documents.map { d ->
                    val sid = d.getString("studentId") ?: ""
                    val paid = d.getBoolean("paid") ?: false
                    val paidAt = d.getLong("paidAt") ?: 0L
                    VoucherRow(
                        id = d.id,
                        studentName = nameMap[sid] ?: "-",
                        className = studentClassMap[sid] ?: "-",
                        month = monthFmt.format(Date(if (paidAt > 0) paidAt else System.currentTimeMillis())),
                        total = d.getDouble("amount") ?: 0.0,
                        status = if (paid) "Paid" else "Pending"
                    )
                }
                _ui.update {
                    it.copy(isLoading = false, rows = rows, classes = classes)
                }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun generateForClass(classId: String, amount: Double, monthLabel: String) {
        if (classId.isBlank() || amount <= 0) {
            _ui.update { it.copy(error = "Select class and enter amount") }
            return
        }
        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val students = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo("role", UserRoles.STUDENT)
                    .whereEqualTo("classId", classId)
                    .get().await()
                if (students.isEmpty) {
                    _ui.update { it.copy(isSaving = false, error = "No students in class") }
                    return@launch
                }
                val sid = SessionManager.current.schoolId
                val batch = firestore.batch()
                val dueDate = System.currentTimeMillis() + 10L * 24 * 3600 * 1000
                for (doc in students.documents) {
                    val ref = firestore.collection(FirestoreCollections.FEES).document()
                    val data = mutableMapOf<String, Any>(
                        "studentId" to doc.id,
                        "studentName" to (doc.getString("name") ?: "-"),
                        "amount" to amount,
                        "paid" to false,
                        "description" to "$monthLabel Fee",
                        "dueDate" to dueDate,
                        "createdAt" to System.currentTimeMillis()
                    )
                    if (sid.isNotBlank()) data["schoolId"] = sid
                    batch.set(ref, data)
                }
                batch.commit().await()
                _ui.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "${students.size()} vouchers generated ✅"
                    )
                }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
