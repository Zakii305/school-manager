package com.school.manager.ui.academic

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

data class QuestionPaper(
    val id: String, val subject: String, val className: String,
    val type: String, val date: String, val maxMarks: Int
)

data class QPState(val isLoading: Boolean = true, val papers: List<QuestionPaper> = emptyList())

@HiltViewModel
class QuestionPapersViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(QPState())
    val uiState = _ui.asStateFlow()
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("question_papers").get().await()
                val list = snap.documents.map { d ->
                    val ts = d.getLong("date") ?: 0L
                    QuestionPaper(
                        id = d.id,
                        subject = d.getString("subject") ?: "-",
                        className = d.getString("className") ?: "-",
                        type = d.getString("type") ?: "Both",
                        date = if (ts > 0) fmt.format(Date(ts)) else "—",
                        maxMarks = (d.getLong("maxMarks") ?: 100L).toInt()
                    )
                }
                _ui.update { it.copy(isLoading = false, papers = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionPapersScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: QuestionPapersViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question Papers", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Question Papers", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF2563EB)) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("New Paper", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(14.dp)) {
                        listOf("SUBJECT" to 1.2f, "CLASS" to 1f, "TYPE" to 0.8f, "DATE" to 1.2f, "MAX" to 0.7f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Violet)
                        }
                    } else if (s.papers.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No question papers yet", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 600.dp)) {
                            items(s.papers, key = { it.id }) { p ->
                                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.subject, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1.2f), maxLines = 1)
                                    Text(p.className, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f), maxLines = 1)
                                    Text(p.type, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                                    Text(p.date, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1.2f))
                                    Text("${p.maxMarks}", color = TextDark, fontSize = 11.sp, modifier = Modifier.weight(0.7f))
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
