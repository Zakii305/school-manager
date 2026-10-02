package com.school.manager.ui.timetable

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

data class EntriesRow(
    val day: String,
    val period: Int,
    val subjectClass: String,
    val teacherName: String
)

data class AllEntriesUiState(
    val isLoading: Boolean = true,
    val rows: List<EntriesRow> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AllEntriesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AllEntriesUiState())
    val uiState: StateFlow<AllEntriesUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val slotSnap = firestore.collection(FirestoreCollections.TIMETABLE).get().await()
                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classSnap.documents.associate { it.id to (it.getString("name") ?: "-") }

                val rows = slotSnap.documents.mapNotNull { d ->
                    val day = d.getString("day") ?: return@mapNotNull null
                    val period = (d.getLong("period") ?: 0L).toInt()
                    val subj = d.getString("subjectName") ?: ""
                    val cid = d.getString("classId") ?: ""
                    val teacher = d.getString("teacherName") ?: "-"
                    if (subj.isBlank()) null
                    else EntriesRow(day, period, "$subj (${classMap[cid] ?: "-"})", teacher)
                }.sortedWith(compareBy({ WEEK_DAYS.indexOf(it.day) }, { it.period }))

                _uiState.value = AllEntriesUiState(isLoading = false, rows = rows)
            } catch (e: Exception) {
                _uiState.value = AllEntriesUiState(isLoading = false, error = e.message)
            }
        }
    }
}
