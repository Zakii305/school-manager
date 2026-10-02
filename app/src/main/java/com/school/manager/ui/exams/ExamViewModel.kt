package com.school.manager.ui.exams

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

data class Exam(
    val id: String,
    val name: String,
    val classId: String,
    val className: String = "",
    val date: Long = 0L,
    val maxMarks: Int = 100
)

data class ExamUiState(
    val isLoading: Boolean = true,
    val exams: List<Exam> = emptyList(),
    val filtered: List<Exam> = emptyList(),
    val query: String = "",
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classSnap.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }
                val examSnap = firestore.collection(FirestoreCollections.EXAMS).get().await()
                val list = examSnap.documents.map { d ->
                    Exam(
                        id = d.id,
                        name = d.getString("name") ?: "-",
                        classId = d.getString("classId") ?: "",
                        className = classMap[d.getString("classId")] ?: "-",
                        date = d.getLong("date") ?: 0L,
                        maxMarks = (d.getLong("maxMarks") ?: 100L).toInt()
                    )
                }.sortedByDescending { it.date }
                _uiState.update {
                    it.copy(isLoading = false, exams = list, filtered = list)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onQuery(q: String) {
        val s = _uiState.value
        val f = if (q.isBlank()) s.exams else s.exams.filter {
            it.name.contains(q, true) || it.className.contains(q, true)
        }
        _uiState.update { it.copy(query = q, filtered = f) }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.EXAMS).document(id).delete().await()
                _uiState.update { it.copy(successMessage = "Exam deleted") }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() = _uiState.update {
        it.copy(error = null, successMessage = null)
    }
}
