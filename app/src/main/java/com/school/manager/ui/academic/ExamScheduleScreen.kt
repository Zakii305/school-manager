package com.school.manager.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.school.manager.util.FirestoreCollections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Violet = Color(0xFF4A148C)

data class DateSheet(
    val id: String, val name: String, val term: String,
    val dateRange: String, val papers: Int, val classes: Int
)

data class ExamScheduleState(
    val isLoading: Boolean = true,
    val sheets: List<DateSheet> = emptyList()
)

@HiltViewModel
class ExamScheduleViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(ExamScheduleState())
    val uiState = _ui.asStateFlow()
    private val fmt = SimpleDateFormat("dd MMM", Locale.US)

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.EXAMS).get().await()
                val list = snap.documents.map { d ->
                    val from = d.getLong("date") ?: 0L
                    DateSheet(
                        id = d.id,
                        name = d.getString("name") ?: "-",
                        term = d.getString("term") ?: "2026",
                        dateRange = if (from > 0) fmt.format(Date(from)) else "—",
                        papers = (d.getLong("paperCount") ?: 0L).toInt(),
                        classes = if (d.getString("classId").isNullOrBlank()) 0 else 1
                    )
                }
                _ui.update { it.copy(isLoading = false, sheets = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScheduleScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ExamScheduleViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Schedule", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg)) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Date Sheets", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF1F5F9)) {
                                Text("Manage Individual Entries", color = TextDark,
                                    fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                            }
                            Spacer(Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF2563EB)) {
                                Text("+ Create Date Sheet", color = Color.White,
                                    fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(horizontal = 14.dp, vertical = 10.dp)) {
                        listOf("EXAM" to 1.4f, "TERM" to 1f, "DATES" to 1.2f, "PAPERS" to 0.7f, "CLASSES" to 0.7f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Violet)
                        }
                    } else if (s.sheets.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No exams yet — create from Exams screen", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn {
                            items(s.sheets, key = { it.id }) { sh ->
                                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(sh.name, color = TextDark, fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.4f), maxLines = 1)
                                    Text(sh.term, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                    Text(sh.dateRange, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                                    Text("${sh.papers}", color = TextDark, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
                                    Text("${sh.classes}", color = TextDark, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
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
