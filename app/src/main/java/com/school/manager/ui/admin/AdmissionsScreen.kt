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
fun AdmissionsScreen(
    onNavigateBack: () -> Unit = {},
    onAddEnquiry: () -> Unit = {},
    onEditEnquiry: (String) -> Unit = {},
    viewModel: AdmissionsAdminViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }
    var statusExp by remember { mutableStateOf(false) }

    LaunchedEffect(s.infoMessage) { s.infoMessage?.let { snackbar.showSnackbar(it); viewModel.consumeInfo() } }
    LaunchedEffect(s.error) { s.error?.let { snackbar.showSnackbar(it); viewModel.consumeError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Admission Enquiries", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = s.query, onValueChange = viewModel::onQuery,
                            placeholder = { Text("Search by name or phone...", fontSize = 12.sp, color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp), singleLine = true
                        )
                        ExposedDropdownMenuBox(expanded = statusExp, onExpandedChange = { statusExp = !statusExp },
                            modifier = Modifier.width(140.dp)) {
                            OutlinedTextField(
                                value = s.statusFilter?.replaceFirstChar { it.uppercase() } ?: "All Status",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExp) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp), singleLine = true
                            )
                            ExposedDropdownMenu(statusExp, onDismissRequest = { statusExp = false }) {
                                listOf("All Status", "new", "approved", "rejected", "converted").forEach { st ->
                                    DropdownMenuItem(text = { Text(st.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.onStatusFilter(if (st == "All Status") null else st)
                                            statusExp = false
                                        })
                                }
                            }
                        }
                        Button(onClick = onAddEnquiry, shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Enquiry", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f).horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(900.dp).fillMaxHeight()) {
                            Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 10.dp)) {
                                Text("APPLICANT", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(160.dp))
                                Text("FATHER", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(150.dp))
                                Text("PHONE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(130.dp))
                                Text("CLASS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(120.dp))
                                Text("SOURCE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(110.dp))
                                Text("STATUS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(90.dp))
                                Text("ACTIONS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(80.dp))
                            }
                            HorizontalDivider(color = LineGrey)
                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = IndigoPrimary)
                                }
                                s.filtered.isEmpty() -> Box(Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text("No enquiries", color = TextMuted, fontSize = 13.sp)
                                }
                                else -> Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                                    s.filtered.forEach { r ->
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(r.applicant, color = TextDark, fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.width(160.dp))
                                            Text(r.father, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(150.dp))
                                            Text(r.phone, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(130.dp))
                                            Text(r.className, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(120.dp))
                                            Text(r.source, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(110.dp))
                                            Box(Modifier.width(90.dp)) {
                                                val (bg, fg) = when (r.status.lowercase()) {
                                                    "approved" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
                                                    "rejected" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
                                                    "converted" -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
                                                    else -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                                }
                                                Surface(shape = RoundedCornerShape(6.dp), color = bg) {
                                                    Text(r.status.replaceFirstChar { it.uppercase() },
                                                        color = fg, fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold, maxLines = 1,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                                }
                                            }
                                            Row(Modifier.width(80.dp), horizontalArrangement = Arrangement.End) {
                                                IconButton(onClick = { onEditEnquiry(r.id) }, modifier = Modifier.size(28.dp)) {
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
            title = { Text("Delete Enquiry") },
            text = { Text("Permanently remove this enquiry?") },
            confirmButton = { TextButton(onClick = { viewModel.doDelete() }) { Text("Delete", color = RedDelete, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") } })
    }
}
