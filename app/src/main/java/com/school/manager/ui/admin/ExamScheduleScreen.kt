package com.school.manager.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.school.manager.ui.theme.IndigoPrimary

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val LineGrey = Color(0xFFE2E8F0)
private val BluePrimary = Color(0xFF2563EB)
private val RedDelete = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScheduleScreen(
    onNavigateBack: () -> Unit = {},
    onManageEntries: () -> Unit = {},
    onCreateDateSheet: () -> Unit = {},
    onEditSheet: (String) -> Unit = {},
    viewModel: ExamScheduleViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(s.infoMessage) { s.infoMessage?.let { snackbar.showSnackbar(it); viewModel.consumeInfo() } }
    LaunchedEffect(s.error) { s.error?.let { snackbar.showSnackbar(it); viewModel.consumeError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Exam Schedule", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg).padding(10.dp)) {

            // ═══ Header row with title + actions ═══
            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Date Sheets", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                    Spacer(Modifier.weight(1f))
                    OutlinedButton(onClick = onManageEntries, shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Manage Individual Entries", fontSize = 11.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onCreateDateSheet, shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Create Date Sheet", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ═══ Table ═══
            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f).horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(780.dp).fillMaxHeight()) {
                            Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("EXAM", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(180.dp))
                                Text("TERM", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(140.dp))
                                Text("DATES", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(180.dp))
                                Text("PAPERS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(70.dp))
                                Text("CLASSES", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(80.dp))
                                Text("ACTIONS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(110.dp))
                            }
                            HorizontalDivider(color = LineGrey)

                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = IndigoPrimary)
                                }
                                s.sheets.isEmpty() -> Box(Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text("No date sheets yet. Tap Create Date Sheet.",
                                        color = TextMuted, fontSize = 13.sp)
                                }
                                else -> Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                                    s.sheets.forEach { row ->
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(row.examName, color = TextDark, fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.width(180.dp))
                                            Text(row.term.ifBlank { "—" }, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(140.dp))
                                            Text(row.dateRange, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(180.dp))
                                            Text(row.papers.toString(), color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(70.dp))
                                            Text(row.classes.toString(), color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(80.dp))
                                            Row(Modifier.width(110.dp), horizontalArrangement = Arrangement.End) {
                                                IconButton(onClick = { /* copy */ }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.ContentCopy, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = { /* download */ }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Download, null, tint = Color(0xFF10B981), modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = { onEditSheet(row.id) }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Edit, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = { viewModel.askDelete(row.id) }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Delete, null, tint = RedDelete, modifier = Modifier.size(15.dp))
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = LineGrey)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }

    if (s.confirmDeleteId != null) {
        AlertDialog(onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Date Sheet") },
            text = { Text("Permanently remove this date sheet?") },
            confirmButton = { TextButton(onClick = { viewModel.doDelete() }) { Text("Delete", color = RedDelete, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") } })
    }
}
