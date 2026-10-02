package com.school.manager.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class FamilyFormState(
    val id: String = "",
    val isEdit: Boolean = false,
    val name: String = "",
    val fatherName: String = "",
    val fatherCnic: String = "",
    val phone: String = "",
    val address: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FamilyFormViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(FamilyFormState())
    val state: StateFlow<FamilyFormState> = _state.asStateFlow()

    fun load(id: String) {
        if (id == "new" || id.isBlank()) return
        viewModelScope.launch {
            try {
                val doc = firestore.collection("families").document(id).get().await()
                if (!doc.exists()) return@launch
                fun g(k: String) = doc.getString(k) ?: ""
                _state.value = FamilyFormState(
                    id = id, isEdit = true,
                    name = g("name"),
                    fatherName = g("father_name"),
                    fatherCnic = g("father_cnic"),
                    phone = g("phone"),
                    address = g("address"),
                    notes = g("notes")
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun onName(v: String) = _state.value.let { _state.value = it.copy(name = v) }
    fun onFather(v: String) = _state.value.let { _state.value = it.copy(fatherName = v) }
    fun onCnic(v: String) = _state.value.let { _state.value = it.copy(fatherCnic = v) }
    fun onPhone(v: String) = _state.value.let { _state.value = it.copy(phone = v) }
    fun onAddress(v: String) = _state.value.let { _state.value = it.copy(address = v) }
    fun onNotes(v: String) = _state.value.let { _state.value = it.copy(notes = v) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(error = "Family name is required"); return
        }
        _state.value = s.copy(isSaving = true, error = null)
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
        val data = mapOf(
            "name" to s.name.trim(),
            "father_name" to s.fatherName.trim(),
            "father_cnic" to s.fatherCnic.trim(),
            "phone" to s.phone.trim(),
            "address" to s.address.trim(),
            "notes" to s.notes.trim(),
            "updated_at" to now
        )
        viewModelScope.launch {
            try {
                val coll = firestore.collection("families")
                if (s.isEdit && s.id.isNotBlank()) {
                    coll.document(s.id).update(data).await()
                } else {
                    coll.add(data + ("created_at" to now)).await()
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false, error = e.message ?: "Save failed")
            }
        }
    }
}
