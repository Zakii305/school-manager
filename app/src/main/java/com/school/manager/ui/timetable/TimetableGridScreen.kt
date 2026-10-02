package com.school.manager.ui.timetable

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.school.manager.ui.theme.ErrorRed
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.ui.theme.TealSecondary
import com.school.manager.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableGridScreen(
    classId: String = "",
    isAdmin: Boolean,
    onNavigateBack: () -> Unit,
    viewModel: TimetableViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    // Shared edit dialog state
    var editing by remember { mutableStateOf<TimetableSlot?>(null) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(classId, isAdmin) {
        if (isAdmin && classId.isNotBlank()) viewModel.load(classId)
        else viewModel.loadForCurrentStudent()
    }

    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        state.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    fun slotAt(day: String, period: Int): TimetableSlot? =
        state.slots.firstOrNull { it.day == day && it.period == period }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(state.className.ifBlank { "Timetable" })
                        Text(
                            if (isAdmin) "Tap cell to edit" else "Read-only",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
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
                .padding(12.dp)
        ) {
            // Period header row (scrolls horizontally with body)
            Row(Modifier.horizontalScroll(rememberScrollState())) {
                // Corner cell
                Box(
                    Modifier.size(width = 90.dp, height = 44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Day / Period",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold)
                }
                for (p in 1..MAX_PERIODS) {
                    Box(
                        Modifier.size(width = 90.dp, height = 44.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("P$p",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary)
                    }
                }
            }

            // Day rows
            for (day in WEEK_DAYS) {
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Box(
                        Modifier.size(width = 90.dp, height = 76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day.take(3),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold)
                    }
                    for (p in 1..MAX_PERIODS) {
                        val slot = slotAt(day, p)
                        Box(
                            Modifier
                                .size(width = 90.dp, height = 76.dp)
                                .padding(3.dp)
                        ) {
                            TimetableCell(
                                slot = slot,
                                isAdmin = isAdmin,
                                onClick = {
                                    if (isAdmin) {
                                        editing = slot ?: TimetableSlot(
                                            classId = classId,
                                            day = day,
                                            period = p
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (isAdmin && slot != null) confirmDeleteId = slot.id
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // ----- Edit dialog -----
    editing?.let { slot ->
        EditSlotDialog(
            initial = slot,
            subjects = state.subjects,
            onDismiss = { editing = null },
            onSave = { updated ->
                viewModel.upsert(updated)
                editing = null
            }
        )
    }

    // ----- Delete confirmation -----
    confirmDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("Remove this slot?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(id, classId)
                    confirmDeleteId = null
                }) { Text("Remove", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableCell(
    slot: TimetableSlot?,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bg = when {
        slot == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else -> TealSecondary.copy(alpha = 0.15f)
    }
    Card(
        modifier = Modifier.fillMaxSize().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Box(Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
            if (slot == null) {
                if (isAdmin) {
                    Icon(Icons.Default.Add,
                        contentDescription = "Add slot",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(slot.subjectName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2)
                    Text(slot.teacherName.take(12),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditSlotDialog(
    initial: TimetableSlot,
    subjects: List<SubjectOption>,
    onDismiss: () -> Unit,
    onSave: (TimetableSlot) -> Unit
) {
    var subjectId by remember { mutableStateOf(initial.subjectId) }
    var startTime by remember { mutableStateOf(initial.startTime) }
    var endTime by remember { mutableStateOf(initial.endTime) }
    var subjectExpanded by remember { mutableStateOf(false) }

    val chosen = subjects.firstOrNull { it.id == subjectId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${initial.day} • Period ${initial.period}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    OutlinedTextField(
                        value = chosen?.name ?: "Select subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(subjectExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        if (subjects.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No subjects for this class") },
                                onClick = { subjectExpanded = false }
                            )
                        } else {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.name}  •  ${s.teacherName}") },
                                    onClick = {
                                        subjectId = s.id
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (chosen != null) {
                        onSave(
                            initial.copy(
                                subjectId = chosen.id,
                                subjectName = chosen.name,
                                teacherId = chosen.teacherId,
                                teacherName = chosen.teacherName,
                                startTime = startTime,
                                endTime = endTime
                            )
                        )
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
