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

data class AuditRow(
    val id: String, val when_: String, val actor: String,
    val action: String, val module: String, val description: String
)

data class AuditState(val isLoading: Boolean = true, val rows: List<AuditRow> = emptyList())

@HiltViewModel
class AuditLogViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(AuditState())
    val uiState = _ui.asStateFlow()
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("audit_log").get().await()
                val list = snap.documents.map { d ->
                    AuditRow(
                        id = d.id,
                        when_ = d.getLong("timestamp")?.let { fmt.format(Date(it)) } ?: "-",
                        actor = d.getString("actor") ?: "-",
                        action = d.getString("action") ?: "-",
                        module = d.getString("module") ?: "-",
                        description = d.getString("description") ?: "-"
                    )
                }.sortedByDescending { it.when_ }
                _ui.update { it.copy(isLoading = false, rows = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AuditLogViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Log", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                    Text("System Activity", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp))
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("WHEN" to 1.2f, "ACTOR" to 1f, "ACTION" to 1f, "MODULE" to 0.8f, "DESCRIPTION" to 2f).forEach {
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
                            Text("No activity recorded yet", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 500.dp)) {
                            items(s.rows.take(30), key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.when_, color = TextMuted, fontSize = 9.sp, modifier = Modifier.weight(1.2f))
                                    Text(r.actor, color = TextDark, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Box(Modifier.weight(1f)) {
                                        Surface(shape = RoundedCornerShape(6.dp),
                                            color = when {
                                                r.action.contains("login") -> Color(0xFFDCFCE7)
                                                r.action.contains("failed") -> Color(0xFFFEE2E2)
                                                else -> Color(0xFFDBEAFE)
                                            }) {
                                            Text(r.action, fontSize = 9.sp,
                                                color = when {
                                                    r.action.contains("login") && !r.action.contains("failed") -> Color(0xFF16A34A)
                                                    r.action.contains("failed") -> Color(0xFFDC2626)
                                                    else -> Color(0xFF2563EB)
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                        }
                                    }
                                    Text(r.module, color = TextMuted, fontSize = 9.sp, modifier = Modifier.weight(0.8f))
                                    Text(r.description, color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(2f), maxLines = 1)
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
