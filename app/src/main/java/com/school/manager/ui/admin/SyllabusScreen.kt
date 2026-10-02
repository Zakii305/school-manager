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
fun SyllabusScreen(
    onNavigateBack: () -> Unit = {},
    onCreateSyllabus: () -> Unit = {},
    onEditSyllabus: (String) -> Unit = {},
    viewModel: SyllabusAdminViewModel = hiltViewModel()
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
                title = { Text("Syllabus", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = s.query, onValueChange = viewModel::onQuery,
                        placeholder = { Text("Search syllabus...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp), singleLine = true
                    )
                    Button(onClick = onCreateSyllabus, shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Create Syllabus", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f).horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(680.dp).fillMaxHeight()) {
                            Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 10.dp)) {
                                Text("TITLE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(240.dp))
                                Text("CLASS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(150.dp))
                                Text("SUBJECT", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(150.dp))
                                Text("TERM", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(90.dp))
                                Text("ACTIONS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(50.dp))
                            }
                            HorizontalDivider(color = LineGrey)
                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = IndigoPrimary)
                                }
                                s.filtered.isEmpty() -> Box(Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text("No syllabus yet", color = TextMuted, fontSize = 13.sp)
                                }
                                else -> Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                                    s.filtered.forEach { r ->
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(r.title, color = TextDark, fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.width(240.dp))
                                            Text(r.className, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(150.dp))
                                            Text(r.subject, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(150.dp))
                                            Text(r.term, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(90.dp))
                                            Row(Modifier.width(50.dp), horizontalArrangement = Arrangement.End) {
                                                IconButton(onClick = { onEditSyllabus(r.id) }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Edit, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = { viewModel.askDelete(r.id) }, modifier = Modifier.size(28.dp)) {
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
        }
    }
    if (s.confirmDeleteId != null) {
        AlertDialog(onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Syllabus") },
            text = { Text("Permanently remove?") },
            confirmButton = { TextButton(onClick = { viewModel.doDelete() }) { Text("Delete", color = RedDelete, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") } })
    }
}
