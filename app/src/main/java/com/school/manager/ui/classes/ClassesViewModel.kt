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

data class SchoolClass(
    val id: String,
    val name: String,
    val sections: List<String> = emptyList(),
    val studentCount: Int = 0
)

data class ClassesUiState(
    val isLoading: Boolean = true,
    val classes: List<SchoolClass> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassesUiState())
    val uiState: StateFlow<ClassesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val userSnap = firestore.collection(FirestoreCollections.USERS).get().await()

                val list = classSnap.documents.map { d ->
                    val cid = d.id
                    val cname = d.getString("name") ?: "-"
                    @Suppress("UNCHECKED_CAST")
                    val sections = (d.get("sections") as? List<String>) ?: emptyList()
                    val studentCount = userSnap.documents.count {
                        it.getString("role") == "student" && it.getString("classId") == cid
                    }
                    SchoolClass(cid, cname, sections, studentCount)
                }.sortedBy { it.name }
                _uiState.value = _uiState.value.copy(isLoading = false, classes = list)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteClass(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.CLASSES).document(id).delete().await()
                _uiState.value = _uiState.value.copy(successMessage = "Class deleted")
                load()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearMessages() = _uiState.value.let {
        _uiState.value = it.copy(error = null, successMessage = null)
    }
}
