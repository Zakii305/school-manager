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
fun GradeSettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: GradeSettingsViewModel = hiltViewModel()
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
                title = { Text("Grade Settings", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg).verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("Grade Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Text("Define your grade bands once — every marks entry across Exams & Results automatically calculates the grade from these, based on percentage obtained.",
                fontSize = 12.sp, color = TextMuted)

            // Two-column: left is table (weight), right is form
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // ═══ LEFT: Grade bands table ═══
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.weight(2f)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Grade Bands", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        Spacer(Modifier.height(10.dp))
                        Box(Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 500.dp).horizontalScroll(rememberScrollState())) {
                            Column(Modifier.width(560.dp)) {
                                Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(horizontal = 8.dp, vertical = 8.dp)) {
                                    Text("GRADE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(80.dp))
                                    Text("MIN %", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(90.dp))
                                    Text("MAX %", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(90.dp))
                                    Text("REMARKS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(200.dp))
                                    Text("ACTIONS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(90.dp))
                                }
                                HorizontalDivider(color = LineGrey)
                                if (s.grades.isEmpty() && !s.isLoading) {
                                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                        Text("No grade bands yet", color = TextMuted, fontSize = 13.sp)
                                    }
                                } else {
                                    Column(Modifier.verticalScroll(rememberScrollState())) {
                                        s.grades.forEach { g ->
                                            if (s.editingId == g.id) {
                                                // Edit mode row
                                                Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    OutlinedTextField(value = s.editGrade, onValueChange = viewModel::onEditGrade,
                                                        modifier = Modifier.width(80.dp), singleLine = true, shape = RoundedCornerShape(6.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    OutlinedTextField(value = s.editMin, onValueChange = viewModel::onEditMin,
                                                        modifier = Modifier.width(90.dp), singleLine = true, shape = RoundedCornerShape(6.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    OutlinedTextField(value = s.editMax, onValueChange = viewModel::onEditMax,
                                                        modifier = Modifier.width(90.dp), singleLine = true, shape = RoundedCornerShape(6.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    OutlinedTextField(value = s.editRemarks, onValueChange = viewModel::onEditRemarks,
                                                        modifier = Modifier.width(200.dp), singleLine = true, shape = RoundedCornerShape(6.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    IconButton(onClick = { viewModel.saveEdit() }, modifier = Modifier.size(30.dp)) {
                                                        Icon(Icons.Default.Check, null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                                    }
                                                    IconButton(onClick = { viewModel.cancelEdit() }, modifier = Modifier.size(30.dp)) {
                                                        Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            } else {
                                                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(g.grade, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.width(80.dp))
                                                    Text(g.minPct, color = TextMuted, fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(90.dp))
                                                    Text(g.maxPct, color = TextMuted, fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(90.dp))
                                                    Text(g.remarks, color = TextMuted, fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(200.dp))
                                                    Row(Modifier.width(90.dp), horizontalArrangement = Arrangement.End) {
                                                        IconButton(onClick = { viewModel.startEdit(g.id) }, modifier = Modifier.size(28.dp)) {
                                                            Icon(Icons.Default.Edit, null, tint = Color(0xFF16A34A), modifier = Modifier.size(15.dp))
                                                        }
                                                        IconButton(onClick = { viewModel.askDelete(g.id) }, modifier = Modifier.size(28.dp)) {
                                                            Icon(Icons.Default.Delete, null, tint = RedDelete, modifier = Modifier.size(15.dp))
                                                        }
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

                // ═══ RIGHT: Add Grade Band form ═══
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Add a Grade Band", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        GradeField("Grade Label *", s.newGrade, viewModel::onNewGrade, "e.g. A")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Column(Modifier.weight(1f)) {
                                GradeField("Min % *", s.newMin, viewModel::onNewMin, "90")
                            }
                            Column(Modifier.weight(1f)) {
                                GradeField("Max % *", s.newMax, viewModel::onNewMax, "100")
                            }
                        }
                        GradeField("Remarks", s.newRemarks, viewModel::onNewRemarks, "e.g. Outstanding")
                        Button(onClick = { viewModel.addBand() }, enabled = !s.isSaving,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.fillMaxWidth()) {
                            if (s.isSaving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            else {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Grade Band", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))) {
                            Column(Modifier.padding(10.dp)) {
                                Text("How it works:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1E3A8A))
                                Spacer(Modifier.height(4.dp))
                                Text("When marks are entered for any student, the system calculates their percentage (obtained ÷ total × 100) and finds the first band whose Min–Max range includes it — that becomes their grade automatically. No manual grade typing needed.",
                                    fontSize = 10.sp, color = Color(0xFF1E3A8A))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (s.confirmDeleteId != null) {
        AlertDialog(onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Grade Band") },
            text = { Text("Permanently remove this grade band?") },
            confirmButton = { TextButton(onClick = { viewModel.doDelete() }) { Text("Delete", color = RedDelete, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") } })
    }
}

@Composable
private fun GradeField(label: String, value: String, onChange: (String) -> Unit, hint: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark,
            modifier = Modifier.padding(bottom = 3.dp))
        OutlinedTextField(value = value, onValueChange = onChange,
            placeholder = { Text(hint, fontSize = 11.sp, color = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp), singleLine = true)
    }
}
