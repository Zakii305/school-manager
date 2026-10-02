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

data class AnnRow(
    val id: String, val title: String, val audience: String,
    val channels: String, val recipients: Int, val sentAt: String
)

data class AnnState(val isLoading: Boolean = true, val rows: List<AnnRow> = emptyList())

@HiltViewModel
class AnnouncementsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(AnnState())
    val uiState = _ui.asStateFlow()
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.NOTICES).get().await()
                val list = snap.documents.map { d ->
                    AnnRow(
                        id = d.id,
                        title = d.getString("title") ?: "-",
                        audience = d.getString("audience") ?: "All",
                        channels = d.getString("channels") ?: "in_app",
                        recipients = (d.getLong("recipients") ?: 0L).toInt(),
                        sentAt = d.getLong("createdAt")?.let { fmt.format(Date(it)) } ?: "-"
                    )
                }.sortedByDescending { it.sentAt }
                _ui.update { it.copy(isLoading = false, rows = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AnnouncementsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                        Text("Announcements", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = Blue) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("New Announcement", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("TITLE" to 1.6f, "AUDIENCE" to 1f, "CHANNELS" to 1.2f, "RECIPIENTS" to 1f, "SENT AT" to 1.2f).forEach {
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
                            Text("No announcements yet", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn {
                            items(s.rows.take(20), key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.title, color = TextDark, fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.6f), maxLines = 1)
                                    Text(r.audience, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text(r.channels, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1.2f))
                                    Text("${r.recipients}", color = TextDark, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text(r.sentAt, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1.2f))
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
