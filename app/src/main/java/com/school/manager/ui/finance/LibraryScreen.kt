package com.school.manager.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Blue = Color(0xFF2563EB)

data class Book(
    val id: String, val title: String, val author: String,
    val category: String, val copies: Int, val available: Int
)

data class LibraryState(
    val isLoading: Boolean = true,
    val books: List<Book> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(LibraryState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("library_books").get().await()
                val list = snap.documents.map { d ->
                    Book(
                        id = d.id,
                        title = d.getString("title") ?: "-",
                        author = d.getString("author") ?: "Various",
                        category = d.getString("category") ?: "Textbook",
                        copies = (d.getLong("copies") ?: 0L).toInt(),
                        available = (d.getLong("available") ?: 0L).toInt()
                    )
                }
                _ui.update { it.copy(isLoading = false, books = list) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addBook(title: String, author: String, category: String, copies: Int) {
        if (title.isBlank()) {
            _ui.update { it.copy(error = "Title required"); return
            }
        }
        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val sid = SessionManager.current.schoolId
                val data = mutableMapOf<String, Any>(
                    "title" to title.trim(),
                    "author" to author.trim().ifBlank { "Various" },
                    "category" to category.trim().ifBlank { "Textbook" },
                    "copies" to copies,
                    "available" to copies,
                    "createdAt" to System.currentTimeMillis()
                )
                if (sid.isNotBlank()) data["schoolId"] = sid
                firestore.collection("library_books").add(data).await()
                _ui.update { it.copy(isSaving = false, successMessage = "Book added ✅") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(s.successMessage, s.error) {
        s.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        s.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Library", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg).padding(12.dp)) {
            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Books", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Book", fontSize = 11.sp)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue)
                        }
                    } else if (s.books.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No books yet — tap Add", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 550.dp)) {
                            items(s.books, key = { it.id }) { b ->
                                Row(Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(b.title, color = TextDark, fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text("${b.author} • ${b.category}",
                                            color = TextMuted, fontSize = 10.sp)
                                    }
                                    Text("${b.copies}", color = TextDark, fontSize = 11.sp)
                                }
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var author by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var copies by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Book") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it },
                        label = { Text("Title") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = author, onValueChange = { author = it },
                        label = { Text("Author") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it },
                        label = { Text("Category") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = copies, onValueChange = { copies = it },
                        label = { Text("Copies") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addBook(title, author, category, copies.toIntOrNull() ?: 1)
                    showDialog = false
                }, enabled = title.isNotBlank()) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
