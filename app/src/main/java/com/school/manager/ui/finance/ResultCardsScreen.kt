package com.school.manager.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
private val Blue = Color(0xFF2563EB)

data class ResultCardRow(
    val id: String, val student: String, val className: String,
    val exam: String, val grade: String, val status: String, val printDate: String
)

data class ResultCardsState(
    val isLoading: Boolean = true,
    val rows: List<ResultCardRow> = emptyList()
)

@HiltViewModel
class ResultCardsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(ResultCardsState())
    val uiState = _ui.asStateFlow()
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val users = firestore.collection(FirestoreCollections.USERS).get().await()
                val nameMap = users.documents.associate { it.id to (it.getString("name") ?: "-") }
                val classMap = users.documents.associate { it.id to (it.getString("classId") ?: "-") }

                val grades = firestore.collection(FirestoreCollections.GRADES).get().await()
                val rows = grades.documents.map { d ->
                    val sid = d.getString("studentId") ?: ""
                    val pct = d.getLong("marks") ?: 0L
                    val tot = d.getLong("total") ?: 100L
                    val p = if (tot > 0) pct * 100 / tot else 0
                    ResultCardRow(
                        id = d.id,
                        student = nameMap[sid] ?: "-",
                        className = classMap[sid] ?: "-",
                        exam = d.getString("examName") ?: "Annual",
                        grade = when {
                            p >= 90 -> "A+"; p >= 80 -> "A"; p >= 70 -> "B"
                            p >= 60 -> "C"; p >= 50 -> "D"; else -> "F"
                        },
                        status = if (p >= 40) "Pass" else "Fail",
                        printDate = fmt.format(Date())
                    )
                }
                _ui.update { it.copy(isLoading = false, rows = rows) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultCardsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ResultCardsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result Cards", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = "", onValueChange = {},
                            placeholder = { Text("Search by student name...", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), singleLine = true)
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = Blue) {
                            Text("+ Generate Result Card", color = Color.White,
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp))
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("STUDENT" to 1.4f, "CLASS" to 1f, "EXAM" to 1.2f,
                            "GRADE" to 0.8f, "STATUS" to 0.8f, "PRINTED" to 1f).forEach {
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
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No result cards generated",
                                    color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text("Enter marks first, then generate result cards",
                                    color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 600.dp)) {
                            items(s.rows.take(20), key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.student, color = TextDark, fontSize = 11.sp,
                                        modifier = Modifier.weight(1.4f), maxLines = 1)
                                    Text(r.className, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text(r.exam, color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(1.2f), maxLines = 1)
                                    Text(r.grade, color = TextDark, fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                    Box(Modifier.weight(0.8f)) {
                                        val (bg, fg) = if (r.status == "Pass")
                                            Color(0xFFDCFCE7) to Color(0xFF16A34A)
                                        else Color(0xFFFEE2E2) to Color(0xFFDC2626)
                                        Surface(shape = RoundedCornerShape(6.dp), color = bg) {
                                            Text(r.status, color = fg, fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                        }
                                    }
                                    Text(r.printDate, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
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
