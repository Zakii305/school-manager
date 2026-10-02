package com.school.manager.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Blue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationLogsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NotificationLogsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Logs", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
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
                    Text("Recent Notifications",
                        color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp))

                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(10.dp)) {
                        Text("EVENT", color = TextMuted, fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.4f))
                        Text("RECIPIENT", color = TextMuted, fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                        Text("EMAIL", color = TextMuted, fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f))
                        Text("WA", color = TextMuted, fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.7f))
                        Text("SENT AT", color = TextMuted, fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue)
                        }
                    } else if (s.logs.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📬", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("No notifications sent yet",
                                    color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 550.dp)) {
                            items(s.logs.take(50), key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.event, color = TextDark, fontSize = 10.sp,
                                        modifier = Modifier.weight(1.4f), maxLines = 1)
                                    Text(r.recipient, color = TextMuted, fontSize = 9.sp,
                                        modifier = Modifier.weight(1.2f), maxLines = 1)
                                    StatusMini(r.emailStatus, Modifier.weight(0.8f))
                                    StatusMini(r.whatsappStatus, Modifier.weight(0.7f))
                                    Text(r.timeText(), color = TextMuted, fontSize = 9.sp,
                                        modifier = Modifier.weight(1.2f))
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

@Composable
private fun StatusMini(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status.lowercase()) {
        "sent", "delivered" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "failed" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        else -> Color(0xFFF1F5F9) to TextMuted
    }
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        Surface(shape = RoundedCornerShape(4.dp), color = bg) {
            Text(status.take(6), color = fg, fontSize = 8.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
        }
    }
}
