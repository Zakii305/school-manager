package com.school.manager.ui.admin

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

data class AdminStats(
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val totalParents: Int = 0,
    val feesCollectedToday: Double = 0.0,
    val attendancePercent: Float = 0f,
    val totalNotices: Int = 0
)

data class AdminUiState(
    val isLoading: Boolean = true,
    val stats: AdminStats = AdminStats(),
    val error: String? = null,
    val adminName: String = "Admin"
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val usersSnap = firestore.collection(FirestoreCollections.USERS).get().await()
                var students = 0; var teachers = 0; var parents = 0
                for (doc in usersSnap.documents) {
                    when (doc.getString("role")) {
                        UserRoles.STUDENT -> students++
                        UserRoles.TEACHER -> teachers++
                        UserRoles.PARENT -> parents++
                    }
                }

                val noticesSnap = firestore.collection(FirestoreCollections.NOTICES).get().await()

                val attSnap = firestore.collection(FirestoreCollections.ATTENDANCE).get().await()
                var present = 0; var total = 0
                for (doc in attSnap.documents) {
                    total++
                    if (doc.getString("status") == "present") present++
                }
                val attPct = if (total > 0) present.toFloat() / total else 0f

                val feesSnap = firestore.collection(FirestoreCollections.FEES).get().await()
                var feesToday = 0.0
                val dayAgo = System.currentTimeMillis() - 86400000L
                for (doc in feesSnap.documents) {
                    val paidAt = doc.getLong("paidAt") ?: 0L
                    val amt = doc.getDouble("amount") ?: 0.0
                    if (paidAt >= dayAgo && doc.getBoolean("paid") == true) feesToday += amt
                }

                val adminName = auth.currentUser?.email?.substringBefore("@") ?: "Admin"

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    adminName = adminName,
                    stats = AdminStats(students, teachers, parents, feesToday, attPct, noticesSnap.size())
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }
}
