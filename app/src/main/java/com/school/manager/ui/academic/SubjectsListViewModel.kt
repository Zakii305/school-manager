package com.school.manager.ui.academic

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
import javax.inject.Inject

data class SubjectRow(val id: String, val name: String, val className: String)
data class ClassOpt(val id: String, val name: String)

data class SubjectsState(
    val isLoading: Boolean = true,
    val rows: List<SubjectRow> = emptyList(),
    val classes: List<ClassOpt> = emptyList(),
    val query: String = "",
    val filtered: List<SubjectRow> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SubjectsListViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(SubjectsState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val subs = firestore.collection(FirestoreCollections.SUBJECTS).get().await()
                val classes = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classes.documents.associate { it.id to (it.getString("name") ?: "-") }
                val rows = subs.documents.map { d ->
                    SubjectRow(
                        id = d.id,
                        name = d.getString("name") ?: "-",
                        className = classMap[d.getString("classId")] ?: "-"
                    )
                }.sortedBy { it.name }
                val classList = classes.documents.map { ClassOpt(it.id, it.getString("name") ?: "-") }
                    .sortedBy { it.name }
                _ui.update { it.copy(isLoading = false, rows = rows, filtered = rows, classes = classList) }
            } catch (e: Exception) { _ui.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onQuery(q: String) {
        val s = _ui.value
        val f = if (q.isBlank()) s.rows else s.rows.filter {
            it.name.contains(q, true) || it.className.contains(q, true)
        }
        _ui.update { it.copy(query = q, filtered = f) }
    }

    fun addSubject(name: String, classId: String) {
        if (name.isBlank() || classId.isBlank()) {
            _ui.update { it.copy(error = "Name and class required") }
            return
        }
        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val sid = SessionManager.current.schoolId
                val data = mutableMapOf<String, Any>(
                    "name" to name.trim(),
                    "classId" to classId,
                    "createdAt" to System.currentTimeMillis()
                )
                if (sid.isNotBlank()) data["schoolId"] = sid
                firestore.collection(FirestoreCollections.SUBJECTS).add(data).await()
                _ui.update { it.copy(isSaving = false, successMessage = "Subject added ✅") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.SUBJECTS).document(id).delete().await()
                _ui.update { it.copy(successMessage = "Deleted") }
                load()
            } catch (e: Exception) { _ui.update { it.copy(error = e.message) } }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}
