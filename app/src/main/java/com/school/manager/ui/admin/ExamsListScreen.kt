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
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
fun ExamsListScreen(
    onNavigateBack: () -> Unit = {},
    onAddExam: () -> Unit = {},
    onEditExam: (String) -> Unit = {},
    viewModel: ExamsViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }
    var pageExp by remember { mutableStateOf(false) }

    LaunchedEffect(s.infoMessage) { s.infoMessage?.let { snackbar.showSnackbar(it); viewModel.consumeInfo() } }
    LaunchedEffect(s.error) { s.error?.let { snackbar.showSnackbar(it); viewModel.consumeError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Exam", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg)) {

            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = s.query, onValueChange = viewModel::onQuery,
                        placeholder = { Text("Search...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp), singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        ExposedDropdownMenuBox(expanded = pageExp, onExpandedChange = { pageExp = !pageExp }, modifier = Modifier.width(110.dp)) {
                            OutlinedTextField(
                                value = "${s.pageSize} / page", onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(pageExp) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp), singleLine = true
                            )
                            ExposedDropdownMenu(pageExp, onDismissRequest = { pageExp = false }) {
                                listOf(10, 25, 50).forEach { n ->
                                    DropdownMenuItem(text = { Text("$n / page", fontSize = 12.sp) },
                                        onClick = { viewModel.onPageSize(n); pageExp = false })
                                }
                            }
                        }
                        OutlinedButton(onClick = {
                            Toast.makeText(context, "Print: ${s.total} exams", Toast.LENGTH_SHORT).show()
                        }, shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Print, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp)); Text("Print List", fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = { /* PDF */ }, shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp)); Text("Download PDF", fontSize = 11.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Button(onClick = onAddExam, shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Exam", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }

            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 10.dp)) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f).horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(720.dp).fillMaxHeight()) {
                            Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(34.dp))
                                Text("EXAM NAME", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(180.dp))
                                Text("TERM", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(140.dp))
                                Text("START DATE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(130.dp))
                                Text("END DATE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(130.dp))
                                Text("ACTIONS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(90.dp))
                            }
                            HorizontalDivider(color = LineGrey)
                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = IndigoPrimary)
                                }
                                s.paged.isEmpty() -> Box(Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text("No exams found", color = TextMuted, fontSize = 13.sp)
                                }
                                else -> Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                                    s.paged.forEach { e ->
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = e.id in s.selectedIds,
                                                onCheckedChange = { viewModel.toggleSelect(e.id) },
                                                modifier = Modifier.size(24.dp))
                                            Spacer(Modifier.width(10.dp))
                                            Text(e.name, color = TextDark, fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium, maxLines = 1,
                                                modifier = Modifier.width(180.dp))
                                            Text(e.term.ifBlank { "—" }, color = TextMuted,
                                                fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(140.dp))
                                            Text(e.startDate.ifBlank { "—" }, color = TextMuted,
                                                fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(130.dp))
                                            Text(e.endDate.ifBlank { "—" }, color = TextMuted,
                                                fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(130.dp))
                                            Row(Modifier.width(90.dp), horizontalArrangement = Arrangement.End) {
                                                IconButton(onClick = { onEditExam(e.id) }, modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Edit, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = { viewModel.askDelete(e.id) }, modifier = Modifier.size(28.dp)) {
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
            Spacer(Modifier.height(10.dp))
        }
    }
    if (s.confirmDeleteId != null) {
        AlertDialog(onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Exam") },
            text = { Text("Permanently remove this exam?") },
            confirmButton = { TextButton(onClick = { viewModel.doDelete() }) { Text("Delete", color = RedDelete, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") } })
    }
}
