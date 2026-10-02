package com.school.manager.ui.admissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AdmissionEnquiry(
    val id: String, val applicant: String, val father: String,
    val desiredClass: String, val phone: String, val status: String
)

data class AdmissionsState(
    val isLoading: Boolean = true,
    val enquiries: List<AdmissionEnquiry> = emptyList(),
    val filter: String = "All"
)

@HiltViewModel
class AdmissionsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(AdmissionsState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("admission_enquiries").get().await()
                val list = snap.documents.map { d ->
                    AdmissionEnquiry(
                        id = d.id,
                        applicant = d.getString("applicant") ?: "-",
                        father = d.getString("father") ?: "-",
                        desiredClass = d.getString("desiredClass") ?: "-",
                        phone = d.getString("phone") ?: "-",
                        status = d.getString("status") ?: "New"
                    )
                }
                _ui.update { it.copy(isLoading = false, enquiries = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }

    fun setFilter(f: String) = _ui.update { it.copy(filter = f) }
}
