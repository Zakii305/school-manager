package com.school.manager.ui.exams

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class ClassOption(val id: String, val name: String)

data class ExamFormState(
    val id: String = "",
    val name: String = "",
    val classId: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val maxMarks: String = "100",
    val classes: List<ClassOption> = emptyList(),
    val isEdit: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
) {
    fun dateText(): String =
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(java.util.Date(dateMillis))
}

@HiltViewModel
class ExamFormViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(ExamFormState())
    val state: StateFlow<ExamFormState> = _state.asStateFlow()

    fun load(examId: String) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val cSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classes = cSnap.documents.map {
                    ClassOption(it.id, it.getString("name") ?: "-")
                }.sortedBy { it.name }

                if (examId == "new") {
                    _state.value = _state.value.copy(classes = classes, isLoading = false)
                } else {
                    val doc = firestore.collection(FirestoreCollections.EXAMS).document(examId).get().await()
                    if (doc.exists()) {
                        _state.value = ExamFormState(
                            id = examId,
                            name = doc.getString("name") ?: "",
                            classId = doc.getString("classId") ?: "",
                            dateMillis = doc.getLong("date") ?: System.currentTimeMillis(),
                            maxMarks = (doc.getLong("maxMarks") ?: 100L).toString(),
                            classes = classes,
                            isEdit = true,
                            isLoading = false
                        )
                    } else {
                        _state.value = _state.value.copy(classes = classes, isLoading = false, error = "Not found")
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onName(v: String) = _state.value.let { _state.value = it.copy(name = v) }
    fun onClass(id: String) = _state.value.let { _state.value = it.copy(classId = id) }
    fun onDate(millis: Long) = _state.value.let { _state.value = it.copy(dateMillis = millis) }
    fun onMaxMarks(v: String) = _state.value.let { _state.value = it.copy(maxMarks = v) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank() || s.classId.isBlank()) {
            _state.value = s.copy(error = "Name and class required")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                val data = mapOf(
                    "name" to s.name.trim(),
                    "classId" to s.classId,
                    "date" to s.dateMillis,
                    "maxMarks" to (s.maxMarks.toIntOrNull() ?: 100),
                    "updatedAt" to System.currentTimeMillis()
                )
                if (s.isEdit && s.id.isNotBlank()) {
                    firestore.collection(FirestoreCollections.EXAMS).document(s.id).update(data).await()
                } else {
                    firestore.collection(FirestoreCollections.EXAMS)
                        .add(data + ("createdAt" to System.currentTimeMillis())).await()
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
