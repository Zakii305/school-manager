package com.school.manager.ui.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class Subject(
    val id: String,
    val name: String,
    val classId: String,
    val teacherId: String = "",
    val teacherName: String = "-"
)

data class ClassDetailUiState(
    val isLoading: Boolean = true,
    val className: String = "",
    val sections: List<String> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ClassDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassDetailUiState())
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    fun load(classId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val cdoc = firestore.collection(FirestoreCollections.CLASSES).document(classId).get().await()
                val cname = cdoc.getString("name") ?: "-"
                @Suppress("UNCHECKED_CAST")
                val sections = (cdoc.get("sections") as? List<String>) ?: emptyList()

                val subjSnap = firestore.collection(FirestoreCollections.SUBJECTS)
                    .whereEqualTo("classId", classId).get().await()

                val teacherIds = subjSnap.documents.mapNotNull { it.getString("teacherId") }.distinct()
                val teacherNames = mutableMapOf<String, String>()
                for (tid in teacherIds) {
                    if (tid.isBlank()) continue
                    val tdoc = firestore.collection(FirestoreCollections.USERS).document(tid).get().await()
                    teacherNames[tid] = tdoc.getString("name") ?: "Unknown"
                }

                val subjects = subjSnap.documents.map { d ->
                    val tid = d.getString("teacherId") ?: ""
                    Subject(
                        id = d.id,
                        name = d.getString("name") ?: "-",
                        classId = classId,
                        teacherId = tid,
                        teacherName = teacherNames[tid] ?: "-"
                    )
                }.sortedBy { it.name }

                _uiState.value = ClassDetailUiState(
                    isLoading = false, className = cname,
                    sections = sections, subjects = subjects
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.SUBJECTS).document(id).delete().await()
                _uiState.value = _uiState.value.copy(successMessage = "Subject deleted")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() = _uiState.value.let {
        _uiState.value = it.copy(error = null, successMessage = null)
    }
}
