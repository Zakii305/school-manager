package com.school.manager.ui.admin

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
fun QuestionPapersScreen(
    onNavigateBack: () -> Unit = {},
    onAddPaper: () -> Unit = {},
    onEditPaper: (String) -> Unit = {},
    viewModel: QuestionPapersViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val ctx = LocalContext.current
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }
    var pageExp by remember { mutableStateOf(false) }

    LaunchedEffect(s.infoMessage) { s.infoMessage?.let { snackbar.showSnackbar(it); viewModel.consumeInfo() } }
    LaunchedEffect(s.error) { s.error?.let { snackbar.showSnackbar(it); viewModel.consumeError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Question Papers", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Question Papers", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                    Spacer(Modifier.weight(1f))
                    Button(onClick = onAddPaper, shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Paper", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = s.query, onValueChange = viewModel::onQuery,
                        placeholder = { Text("Search...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp), singleLine = true
                    )
                    ExposedDropdownMenuBox(expanded = pageExp, onExpandedChange = { pageExp = !pageExp },
                        modifier = Modifier.width(110.dp)) {
                        OutlinedTextField(value = "${s.pageSize} / page", onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(pageExp) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(8.dp), singleLine = true)
                        ExposedDropdownMenu(pageExp, onDismissRequest = { pageExp = false }) {
                            listOf(10, 25, 50).forEach { n ->
                                DropdownMenuItem(text = { Text("$n / page", fontSize = 12.sp) },
                                    onClick = { viewModel.onPageSize(n); pageExp = false })
                            }
                        }
                    }
                    OutlinedButton(onClick = {
                        Toast.makeText(ctx, "Print: ${s.total} question papers", Toast.LENGTH_SHORT).show()
                    }, shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp)); Text("Print", fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = {
                        Toast.makeText(ctx, "PDF export: ${s.total} papers", Toast.LENGTH_SHORT).show()
                    }, shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp)); Text("PDF", fontSize = 11.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f).horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(760.dp).fillMaxHeight()) {
                            Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 10.dp)) {
                                Text("SUBJECT", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(160.dp))
                                Text("CLASS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(140.dp))
                                Text("TYPE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(110.dp))
                                Text("DATE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(120.dp))
                                Text("MAX MARKS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(100.dp))
                                Text("ACTIONS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(120.dp))
                            }
                            HorizontalDivider(color = LineGrey)
                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = IndigoPrimary)
                                }
                                s.paged.isEmpty() -> Box(Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text("No question papers", color = TextMuted, fontSize = 13.sp)
                                }
                                else -> Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                                    s.paged.forEach { r ->
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(r.subject, color = TextDark, fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.width(160.dp))
                                            Text(r.className, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(140.dp))
                                            Text(r.type, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(110.dp))
                                            Text(r.date, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(120.dp))
                                            Text(r.maxMarks, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(100.dp))
                                            Row(Modifier.width(120.dp), horizontalArrangement = Arrangement.End) {
                                                IconButton(onClick = { onEditPaper(r.id) }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Edit, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = {
                                                    Toast.makeText(ctx, "Downloading ${r.subject}...", Toast.LENGTH_SHORT).show()
                                                }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Download, null, tint = Color(0xFF10B981), modifier = Modifier.size(15.dp))
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
                    if (s.paged.isNotEmpty()) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.Center) {
                            Text("Showing 1-${s.paged.size} of ${s.total}", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
    if (s.confirmDeleteId != null) {
        AlertDialog(onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Question Paper") },
            text = { Text("Permanently remove this question paper?") },
            confirmButton = { TextButton(onClick = { viewModel.doDelete() }) { Text("Delete", color = RedDelete, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") } })
    }
}
