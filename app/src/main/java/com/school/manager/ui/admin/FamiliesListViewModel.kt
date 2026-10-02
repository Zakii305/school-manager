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

data class FamiliesListItem(
    val id: String,
    val name: String,
    val fatherName: String,
    val phone: String,
    val cnic: String,
    val childrenCount: Int,
    val pendingFee: Double
)

data class FamiliesUiState(
    val families: List<FamiliesListItem> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filtered: List<FamiliesListItem>
        get() {
            val q = query.lowercase().trim()
            if (q.isBlank()) return families
            return families.filter {
                it.name.lowercase().contains(q) ||
                it.fatherName.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.cnic.contains(q)
            }
        }
}

@HiltViewModel
class FamiliesListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(FamiliesUiState())
    val state: StateFlow<FamiliesUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val famSnap = firestore.collection("families").get().await()
                val stuSnap = firestore.collection("students").get().await()
                val invSnap = firestore.collection("invoices").get().await()

                // Map familyId -> list of student ids
                val familyStudents = mutableMapOf<String, MutableList<String>>()
                stuSnap.documents.forEach { d ->
                    val fid = d.getString("family_id") ?: return@forEach
                    if (fid.isBlank()) return@forEach
                    familyStudents.getOrPut(fid) { mutableListOf() }.add(d.id)
                }

                // Map studentId -> pending fee
                val studentPending = mutableMapOf<String, Double>()
                invSnap.documents.forEach { d ->
                    val sid = d.getString("student_id") ?: return@forEach
                    val status = d.getString("status") ?: "pending"
                    if (status.equals("paid", true)) return@forEach
                    val amt = d.getDouble("total_amount")
                        ?: d.getDouble("amount") ?: 0.0
                    studentPending[sid] = (studentPending[sid] ?: 0.0) + amt
                }

                val rows = famSnap.documents.map { d ->
                    val id = d.id
                    val kids = familyStudents[id]?.size ?: 0
                    val pending = familyStudents[id]
                        ?.sumOf { studentPending[it] ?: 0.0 } ?: 0.0
                    FamiliesListItem(
                        id = id,
                        name = d.getString("name") ?: "Unnamed Family",
                        fatherName = d.getString("father_name") ?: "-",
                        phone = d.getString("phone") ?: "-",
                        cnic = d.getString("father_cnic") ?: "",
                        childrenCount = kids,
                        pendingFee = pending
                    )
                }.sortedBy { it.name.lowercase() }

                _state.value = _state.value.copy(
                    families = rows,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load families"
                )
            }
        }
    }

    fun onQuery(v: String) = _state.value.let { _state.value = it.copy(query = v) }
}
