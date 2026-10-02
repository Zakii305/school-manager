package com.school.manager.ui.search

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

data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: String // "user" | "class" | "notice"
)

data class SearchUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var allUsers: List<SearchResult> = emptyList()
    private var allClasses: List<SearchResult> = emptyList()
    private var allNotices: List<SearchResult> = emptyList()

    init { preload() }

    private fun preload() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val users = firestore.collection(FirestoreCollections.USERS).get().await()
                allUsers = users.documents.map { d ->
                    SearchResult(
                        id = d.id,
                        title = d.getString("name") ?: "-",
                        subtitle = "${d.getString("role") ?: "-"} • ${d.getString("email") ?: ""}",
                        kind = "user"
                    )
                }

                val classes = firestore.collection(FirestoreCollections.CLASSES).get().await()
                allClasses = classes.documents.map { d ->
                    SearchResult(
                        id = d.id,
                        title = d.getString("name") ?: "-",
                        subtitle = "Class",
                        kind = "class"
                    )
                }

                val notices = firestore.collection(FirestoreCollections.NOTICES).get().await()
                allNotices = notices.documents.map { d ->
                    SearchResult(
                        id = d.id,
                        title = d.getString("title") ?: "-",
                        subtitle = d.getString("body")?.take(60) ?: "",
                        kind = "notice"
                    )
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun onQuery(q: String) {
        _uiState.value = _uiState.value.copy(query = q)
        if (q.isBlank()) {
            _uiState.value = _uiState.value.copy(results = emptyList())
            return
        }
        val lower = q.lowercase()
        val results = (allUsers + allClasses + allNotices)
            .filter {
                it.title.lowercase().contains(lower) ||
                it.subtitle.lowercase().contains(lower)
            }
            .take(50)
        _uiState.value = _uiState.value.copy(results = results)
    }
}
