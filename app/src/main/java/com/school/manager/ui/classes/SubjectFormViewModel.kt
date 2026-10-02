package com.school.manager.ui.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class TeacherOption(val uid: String, val name: String)

data class SubjectFormState(
    val id: String = "",
    val classId: String = "",
    val name: String = "",
    val teacherId: String = "",
    val teachers: List<TeacherOption> = emptyList(),
    val isEdit: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubjectFormViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(SubjectFormState())
    val state: StateFlow<SubjectFormState> = _state.asStateFlow()

    fun load(classId: String, subjectId: String) {
        _state.value = _state.value.copy(classId = classId, isLoading = true)
        viewModelScope.launch {
            try {
                val tSnap = firestore.collection(FirestoreCollections.USERS)
                    .whereEqualTo("role", UserRoles.TEACHER).get().await()
                val teachers = tSnap.documents.map {
                    TeacherOption(it.id, it.getString("name") ?: it.id)
                }.sortedBy { it.name }

                if (subjectId == "new") {
                    _state.value = _state.value.copy(teachers = teachers, isLoading = false)
                } else {
                    val doc = firestore.collection(FirestoreCollections.SUBJECTS)
                        .document(subjectId).get().await()
                    if (doc.exists()) {
                        _state.value = SubjectFormState(
                            id = subjectId,
                            classId = classId,
                            name = doc.getString("name") ?: "",
                            teacherId = doc.getString("teacherId") ?: "",
                            teachers = teachers,
                            isEdit = true,
                            isLoading = false
                        )
                    } else {
                        _state.value = _state.value.copy(teachers = teachers, isLoading = false, error = "Not found")
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onName(v: String) = _state.value.let { _state.value = it.copy(name = v) }
    fun onTeacher(uid: String) = _state.value.let { _state.value = it.copy(teacherId = uid) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(error = "Subject name required")
            return
        }
        _state.value = s.copy(isSaving = true, error = null)

        val teacherName = s.teachers.firstOrNull { it.uid == s.teacherId }?.name ?: ""

        viewModelScope.launch {
            try {
                val data = mapOf(
                    "name" to s.name.trim(),
                    "classId" to s.classId,
                    "teacherId" to s.teacherId,
                    "teacherName" to teacherName,
                    "updatedAt" to System.currentTimeMillis()
                )
                if (s.isEdit && s.id.isNotBlank()) {
                    firestore.collection(FirestoreCollections.SUBJECTS).document(s.id).update(data).await()
                } else {
                    firestore.collection(FirestoreCollections.SUBJECTS)
                        .add(data + ("createdAt" to System.currentTimeMillis())).await()
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
