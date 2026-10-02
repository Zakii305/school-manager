package com.school.manager.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.school.manager.util.FirestoreCollections
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

data class SyllabusRow(val id: String, val className: String, val session: String, val subjects: Int)

data class SyllabusState(val isLoading: Boolean = true, val rows: List<SyllabusRow> = emptyList())

@HiltViewModel
class SyllabusViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(SyllabusState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val classes = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val subs = firestore.collection(FirestoreCollections.SUBJECTS).get().await()
                val countByClass = subs.documents.groupingBy { it.getString("classId") ?: "" }.eachCount()
                val list = classes.documents.map { d ->
                    SyllabusRow(
                        id = d.id,
                        className = d.getString("name") ?: "-",
                        session = "2026-2027",
                        subjects = countByClass[d.id] ?: 0
                    )
                }.sortedBy { it.className }
                _ui.update { it.copy(isLoading = false, rows = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyllabusScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SyllabusViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Syllabus", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                        Text("Class Syllabi", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = Blue) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null,
                                    tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Create Syllabus", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("CLASS" to 1.6f, "SESSION" to 1.2f, "SUBJECTS" to 1f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue)
                        }
                    } else if (s.rows.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No classes yet", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 600.dp)) {
                            items(s.rows, key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.className, color = TextDark, fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.6f))
                                    Text(r.session, color = TextMuted, fontSize = 11.sp,
                                        modifier = Modifier.weight(1.2f))
                                    Text("${r.subjects} subject(s)", color = TextMuted, fontSize = 11.sp,
                                        modifier = Modifier.weight(1f))
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
}
