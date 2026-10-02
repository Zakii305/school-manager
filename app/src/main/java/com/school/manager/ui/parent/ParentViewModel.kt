package com.school.manager.ui.parent

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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ChildInfo(
    val uid: String,
    val name: String,
    val email: String,
    val attendancePercent: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val avgScore: Int = 0,
    val pendingFees: Double = 0.0
)

data class ParentUiState(
    val isLoading: Boolean = true,
    val parentName: String = "Parent",
    val children: List<ChildInfo> = emptyList(),
    val selectedChildIndex: Int = 0,
    val error: String? = null
) {
    val selectedChild: ChildInfo? get() = children.getOrNull(selectedChildIndex)
}

@HiltViewModel
class ParentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParentUiState())
    val uiState: StateFlow<ParentUiState> = _uiState.asStateFlow()

    init { loadParent() }

    fun loadParent() {
        val parentUid = auth.currentUser?.uid ?: return
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // 1. Get parent doc
                val parentDoc = firestore.collection(FirestoreCollections.USERS)
                    .document(parentUid).get().await()
                val parentName = parentDoc.getString("name") ?: "Parent"

                // 2. Get child UIDs from parent's "children" array
                @Suppress("UNCHECKED_CAST")
                val childUids = (parentDoc.get("children") as? List<String>) ?: emptyList()

                val children = mutableListOf<ChildInfo>()

                for (cuid in childUids) {
                    val cDoc = firestore.collection(FirestoreCollections.USERS)
                        .document(cuid).get().await()
                    if (!cDoc.exists()) continue

                    val cName = cDoc.getString("name") ?: "Child"
                    val cEmail = cDoc.getString("email") ?: ""

                    // Attendance
                    val attSnap = firestore.collection(FirestoreCollections.ATTENDANCE)
                        .whereEqualTo("studentId", cuid).get().await()
                    val present = attSnap.count { it.getString("status") == "present" }
                    val absent = attSnap.count { it.getString("status") == "absent" }
                    val total = present + absent
                    val attPct = if (total > 0) (present * 100 / total) else 0

                    // Grades
                    val gradeSnap = firestore.collection(FirestoreCollections.GRADES)
                        .whereEqualTo("studentId", cuid).get().await()
                    val percents = gradeSnap.documents.mapNotNull { d ->
                        val m = d.getLong("marks")?.toInt() ?: return@mapNotNull null
                        val t = d.getLong("total")?.toInt() ?: return@mapNotNull null
                        if (t > 0) m * 100 / t else null
                    }
                    val avg = if (percents.isNotEmpty()) percents.average().toInt() else 0

                    // Fees
                    val feeSnap = firestore.collection(FirestoreCollections.FEES)
                        .whereEqualTo("studentId", cuid).get().await()
                    var pending = 0.0
                    for (f in feeSnap.documents) {
                        if (f.getBoolean("paid") != true) pending += f.getDouble("amount") ?: 0.0
                    }

                    children.add(
                        ChildInfo(cuid, cName, cEmail, attPct, present, absent, avg, pending)
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    parentName = parentName,
                    children = children,
                    selectedChildIndex = 0
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false, error = e.message ?: "Failed to load"
                )
            }
        }
    }

    fun selectChild(index: Int) {
        if (index in _uiState.value.children.indices) {
            _uiState.value = _uiState.value.copy(selectedChildIndex = index)
        }
    }

    fun reload() = loadParent()
}
