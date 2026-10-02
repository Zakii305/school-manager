package com.school.manager.ui.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
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

data class School(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val status: String,
    val plan: String,
    val studentCount: Int,
    val teacherCount: Int,
    val createdAt: Long,
    val expiresAt: Long
) {
    fun createdText() = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(createdAt))
    fun expiryText() = if (expiresAt > 0)
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(expiresAt))
    else "—"
}

data class OwnerUiState(
    val isLoading: Boolean = true,
    val schools: List<School> = emptyList(),
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val activeSchools: Int = 0,
    val trialSchools: Int = 0,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class OwnerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _ui = MutableStateFlow(OwnerUiState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        _ui.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val schoolsSnap = firestore.collection("schools").get().await()
                val usersSnap = firestore.collection(FirestoreCollections.USERS).get().await()

                val studentsBySchool = mutableMapOf<String, Int>()
                val teachersBySchool = mutableMapOf<String, Int>()
                var totalStudents = 0
                var totalTeachers = 0
                for (u in usersSnap.documents) {
                    val sid = u.getString("schoolId") ?: continue
                    when (u.getString("role")) {
                        "student" -> {
                            studentsBySchool[sid] = (studentsBySchool[sid] ?: 0) + 1
                            totalStudents++
                        }
                        "teacher" -> {
                            teachersBySchool[sid] = (teachersBySchool[sid] ?: 0) + 1
                            totalTeachers++
                        }
                    }
                }

                val schools = schoolsSnap.documents.map { d ->
                    School(
                        id = d.id,
                        name = d.getString("name") ?: "-",
                        email = d.getString("email") ?: "",
                        phone = d.getString("phone") ?: "",
                        status = d.getString("status") ?: "active",
                        plan = d.getString("plan") ?: "trial",
                        studentCount = studentsBySchool[d.id] ?: 0,
                        teacherCount = teachersBySchool[d.id] ?: 0,
                        createdAt = d.getLong("createdAt") ?: 0L,
                        expiresAt = d.getLong("expiresAt") ?: 0L
                    )
                }.sortedByDescending { it.createdAt }

                _ui.update {
                    it.copy(
                        isLoading = false,
                        schools = schools,
                        totalStudents = totalStudents,
                        totalTeachers = totalTeachers,
                        activeSchools = schools.count { s -> s.status == "active" },
                        trialSchools = schools.count { s -> s.plan == "trial" }
                    )
                }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createSchool(name: String, email: String, phone: String) {
        if (name.isBlank()) {
            _ui.update { it.copy(error = "School name required") }
            return
        }
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val expires = now + 30L * 24 * 3600 * 1000
                firestore.collection("schools").add(
                    mapOf(
                        "name" to name.trim(),
                        "email" to email.trim(),
                        "phone" to phone.trim(),
                        "status" to "active",
                        "plan" to "trial",
                        "studentLimit" to 100,
                        "createdAt" to now,
                        "expiresAt" to expires
                    )
                ).await()
                _ui.update { it.copy(successMessage = "School created ✅") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun suspendSchool(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("schools").document(id)
                    .update("status", "suspended").await()
                _ui.update { it.copy(successMessage = "School suspended") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun activateSchool(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("schools").document(id)
                    .update("status", "active").await()
                _ui.update { it.copy(successMessage = "School activated") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteSchool(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("schools").document(id).delete().await()
                _ui.update { it.copy(successMessage = "School deleted") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
