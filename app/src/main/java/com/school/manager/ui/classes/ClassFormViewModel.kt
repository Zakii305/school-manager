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

data class ClassFormState(
    val id: String = "",
    val name: String = "",
    val sectionsText: String = "A, B, C",
    val isEdit: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ClassFormViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(ClassFormState())
    val state: StateFlow<ClassFormState> = _state.asStateFlow()

    fun load(classId: String) {
        if (classId == "new") return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val doc = firestore.collection(FirestoreCollections.CLASSES).document(classId).get().await()
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val sections = (doc.get("sections") as? List<String>) ?: emptyList()
                    _state.value = ClassFormState(
                        id = classId,
                        name = doc.getString("name") ?: "",
                        sectionsText = sections.joinToString(", "),
                        isEdit = true
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    fun onName(v: String) = _state.value.let { _state.value = it.copy(name = v) }
    fun onSections(v: String) = _state.value.let { _state.value = it.copy(sectionsText = v) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(error = "Class name required")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)

        val sections = s.sectionsText.split(",").map { it.trim() }.filter { it.isNotBlank() }

        viewModelScope.launch {
            try {
                val data = mapOf(
                    "name" to s.name.trim(),
                    "sections" to sections,
                    "updatedAt" to System.currentTimeMillis()
                )
                if (s.isEdit && s.id.isNotBlank()) {
                    firestore.collection(FirestoreCollections.CLASSES).document(s.id).update(data).await()
                } else {
                    firestore.collection(FirestoreCollections.CLASSES)
                        .add(data + ("createdAt" to System.currentTimeMillis())).await()
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
