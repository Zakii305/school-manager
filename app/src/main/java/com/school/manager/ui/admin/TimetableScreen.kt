package com.school.manager.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
fun TimetableScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }
    var clsExp by remember { mutableStateOf(false) }

    LaunchedEffect(s.infoMessage) { s.infoMessage?.let { snackbar.showSnackbar(it); viewModel.consumeInfo() } }
    LaunchedEffect(s.error) { s.error?.let { snackbar.showSnackbar(it); viewModel.consumeError() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Timetable", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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

            Text("Timetable", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Text("Set your own bell times once, then build each class's weekly schedule.",
                fontSize = 11.sp, color = TextMuted)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    Toast.makeText(context, "Edit Period Times — ${s.periods.size} slots", Toast.LENGTH_SHORT).show()
                }, shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp)); Text("Edit Period Times", fontSize = 11.sp)
                }
                OutlinedButton(onClick = {
                    Toast.makeText(context, "Print timetable for selected class", Toast.LENGTH_SHORT).show()
                }, shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.Print, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp)); Text("Print Timetable", fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(10.dp))

            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Class", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
                    Spacer(Modifier.height(4.dp))
                    ExposedDropdownMenuBox(expanded = clsExp, onExpandedChange = { clsExp = !clsExp }) {
                        OutlinedTextField(
                            value = s.classes.firstOrNull { it.first == s.selectedClassId }?.second ?: "— Select —",
                            onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(clsExp) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(8.dp), singleLine = true
                        )
                        ExposedDropdownMenu(clsExp, onDismissRequest = { clsExp = false }) {
                            s.classes.forEach { (id, label) ->
                                DropdownMenuItem(text = { Text(label, fontSize = 12.sp) },
                                    onClick = { viewModel.selectClass(id); clsExp = false })
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ═══ Timetable grid (both scrolls) ═══
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f)) {
                Box(Modifier.fillMaxSize().horizontalScroll(rememberScrollState())) {
                    Column(Modifier.width(900.dp).fillMaxHeight()) {
                        // Header
                        Row(Modifier.fillMaxWidth().background(Color(0xFF0B1730))) {
                            Box(Modifier.width(110.dp).padding(10.dp)) {
                                Text("Period", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            s.days.forEach { d ->
                                Box(Modifier.weight(1f).padding(10.dp)) {
                                    Text(d, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        // Rows — vertical scroll
                        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                            s.periods.forEachIndexed { pIdx, p ->
                                Row(Modifier.fillMaxWidth().border(0.5.dp, LineGrey)) {
                                    Box(Modifier.width(110.dp).padding(10.dp)) {
                                        Text(p.label, fontSize = 11.sp, color = TextDark, maxLines = 1)
                                    }
                                    s.days.forEachIndexed { dIdx, _ ->
                                        val slot = s.grid.getOrNull(pIdx)?.getOrNull(dIdx) ?: Slot()
                                        Box(Modifier.weight(1f).padding(4.dp)) {
                                            Column {
                                                SubjectDropdown(s.subjects, slot.subject) { v ->
                                                    viewModel.updateSlot(pIdx, dIdx, subject = v)
                                                }
                                                Spacer(Modifier.height(2.dp))
                                                TeacherDropdown(s.teachers, slot.teacher) { v ->
                                                    viewModel.updateSlot(pIdx, dIdx, teacher = v)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.save() }, enabled = !s.isSaving,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                    if (s.isSaving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save Timetable", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                OutlinedButton(onClick = { viewModel.clear() }, shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.Delete, null, tint = RedDelete, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear This Class's Timetable", fontSize = 11.sp, color = RedDelete)
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectDropdown(options: List<String>, current: String, onSelect: (String) -> Unit) {
    var exp by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
        OutlinedTextField(value = current.ifBlank { "Subject" }, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exp) },
            modifier = Modifier.fillMaxWidth().height(52.dp).menuAnchor(),
            shape = RoundedCornerShape(6.dp), singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp))
        ExposedDropdownMenu(exp, onDismissRequest = { exp = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt, fontSize = 11.sp) },
                    onClick = { onSelect(opt); exp = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeacherDropdown(options: List<String>, current: String, onSelect: (String) -> Unit) {
    var exp by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
        OutlinedTextField(value = current.ifBlank { "Teacher..." }, onValueChange = {}, readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exp) },
            modifier = Modifier.fillMaxWidth().height(52.dp).menuAnchor(),
            shape = RoundedCornerShape(6.dp), singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp))
        ExposedDropdownMenu(exp, onDismissRequest = { exp = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt, fontSize = 11.sp) },
                    onClick = { onSelect(opt); exp = false })
            }
        }
    }
}
