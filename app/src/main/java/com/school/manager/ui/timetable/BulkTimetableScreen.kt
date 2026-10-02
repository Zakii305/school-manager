package com.school.manager.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.AmberAccent
import com.school.manager.ui.theme.ErrorRed
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.ui.theme.SuccessGreen
import com.school.manager.ui.theme.TealSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkTimetableScreen(
    classId: String,
    onNavigateBack: () -> Unit,
    viewModel: BulkTimetableViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showApplyDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(classId) { viewModel.load(classId) }
    LaunchedEffect(s.success, s.error) {
        s.success?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        s.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(s.className.ifBlank { "Bulk Timetable" })
                        Text(
                            if (s.editMode) "${s.selected.size} selected"
                            else "Tap Edit to enable multi-select",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            if (s.editMode) Icons.Default.Done else Icons.Default.Edit,
                            contentDescription = if (s.editMode) "Done" else "Edit"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (s.editMode) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.selectAll() },
                    containerColor = TealSecondary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.SelectAll, contentDescription = null) },
                    text = { Text("All") }
                )
            }
        }
    ) { padding ->
        if (s.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IndigoPrimary)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                Box(Modifier.size(width = 110.dp, height = 42.dp), contentAlignment = Alignment.Center) {
                    Text("Day / Period",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold)
                }
                for (p in 1..MAX_PERIODS) {
                    Box(Modifier.size(width = 88.dp, height = 42.dp), contentAlignment = Alignment.Center) {
                        Text("P$p",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary)
                    }
                }
            }

            for (day in WEEK_DAYS) {
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Box(
                        Modifier
                            .size(width = 110.dp, height = 78.dp)
                            .clickable { viewModel.selectDay(day) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day.take(3),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold)
                            if (s.editMode) {
                                Text("tap to select",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TealSecondary)
                            }
                        }
                    }
                    for (p in 1..MAX_PERIODS) {
                        val slot = s.slots.firstOrNull { it.day == day && it.period == p }
                        val k = viewModel.key(day, p)
                        val isSel = s.selected.contains(k)
                        BulkCell(
                            slot = slot,
                            selected = isSel,
                            editMode = s.editMode,
                            onClick = { viewModel.toggleCell(day, p) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }

    if (s.editMode && s.selected.isNotEmpty()) {
        BulkActionBar(
            onApply = { showApplyDialog = true },
            onCopy = { showCopyDialog = true },
            onDelete = { showClearDialog = true },
            onClear = { viewModel.clearSelection() }
        )
    }

    if (showApplyDialog) {
        ApplySubjectDialog(
            subjects = s.subjects,
            onDismiss = { showApplyDialog = false },
            onPick = { subj ->
                viewModel.applySubjectToSelected(subj)
                showApplyDialog = false
            }
        )
    }

    if (showCopyDialog) {
        CopyDayDialog(
            onDismiss = { showCopyDialog = false },
            onConfirm = { src, tgts ->
                viewModel.copyDay(src, tgts)
                showCopyDialog = false
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear ${s.selected.size} slots?") },
            text = { Text("This removes all selected subject assignments.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected()
                    showClearDialog = false
                }) { Text("Clear", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (s.isSaving) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            title = { Text("Saving…") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = IndigoPrimary)
                    Spacer(Modifier.width(12.dp))
                    Text("Applying changes")
                }
            }
        )
    }
}

@Composable
private fun BulkCell(
    slot: TimetableSlot?,
    selected: Boolean,
    editMode: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        selected -> IndigoPrimary.copy(alpha = 0.20f)
        slot != null -> TealSecondary.copy(alpha = 0.15f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    Card(
        modifier = Modifier
            .size(width = 88.dp, height = 78.dp)
            .padding(3.dp)
            .then(if (selected) Modifier.border(2.dp, IndigoPrimary, RoundedCornerShape(10.dp)) else Modifier)
            .clickable { if (editMode) onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Box(Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
            if (editMode) {
                Checkbox(checked = selected, onCheckedChange = { onClick() })
            } else if (slot != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(slot.subjectName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2)
                    Text(slot.teacherName.take(10),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun BulkActionBar(
    onApply: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(onClick = onApply, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)) {
                Text("Apply", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(onClick = onCopy, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Copy", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null,
                    tint = ErrorRed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Clear", color = ErrorRed, style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = onClear, modifier = Modifier.weight(0.6f)) {
                Text("X", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplySubjectDialog(
    subjects: List<SubjectOption>,
    onDismiss: () -> Unit,
    onPick: (SubjectOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var chosen by remember { mutableStateOf<SubjectOption?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply Subject") },
        text = {
            Column {
                Text("Pick a subject to apply to all selected cells.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = chosen?.name ?: "Select subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                        if (subjects.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No subjects — add them first") },
                                onClick = { expanded = false }
                            )
                        } else {
                            subjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text("${subj.name}  •  ${subj.teacherName}") },
                                    onClick = { chosen = subj; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { chosen?.let { onPick(it) } },
                enabled = chosen != null
            ) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CopyDayDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<String>) -> Unit
) {
    var source by remember { mutableStateOf("Monday") }
    var targets by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Copy Day") },
        text = {
            Column {
                Text("Copy the schedule of one day to other days.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Text("From:", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WEEK_DAYS.forEach { d ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (source == d) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { source = d }
                        ) {
                            Text(d.take(3),
                                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = if (source == d) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("To:", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WEEK_DAYS.filter { it != source }.forEach { d ->
                        val on = targets.contains(d)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (on) SuccessGreen else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                targets = if (on) targets - d else targets + d
                            }
                        ) {
                            Text(d.take(3),
                                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = if (on) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(source, targets.toList()) },
                enabled = targets.isNotEmpty()
            ) { Text("Copy") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
