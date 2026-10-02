package com.school.manager.ui.gradebook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.ui.theme.SuccessGreen
import com.school.manager.ui.theme.TealSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradebookScreen(
    examId: String,
    onNavigateBack: () -> Unit,
    viewModel: GradebookViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var subjectExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(examId) { viewModel.load(examId) }
    LaunchedEffect(state.savedMessage, state.error) {
        state.savedMessage?.let { snackbar.showSnackbar(it) }
        state.error?.let { snackbar.showSnackbar("Error: $it") }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(state.examName.ifBlank { "Gradebook" }) },
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

        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(
                Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TealSecondary.copy(alpha = 0.10f))
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("${state.className} • Max ${state.maxMarks} marks",
                        style = MaterialTheme.typography.bodySmall,
                        color = TealSecondary, fontWeight = FontWeight.SemiBold)

                    Spacer(Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = !subjectExpanded }
                    ) {
                        val current = state.subjects.firstOrNull { it.id == state.selectedSubjectId }
                        OutlinedTextField(
                            value = current?.name ?: "Select subject",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(subjectExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                            if (state.subjects.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No subjects — add some first") },
                                    onClick = { subjectExpanded = false }
                                )
                            } else {
                                state.subjects.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.name) },
                                        onClick = {
                                            viewModel.selectSubject(examId, s.id)
                                            subjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.rows.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students in this class yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.rows, key = { it.studentId }) { row ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(row.studentName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f))
                                OutlinedTextField(
                                    value = row.marksText,
                                    onValueChange = { viewModel.updateMark(row.studentId, it) },
                                    modifier = Modifier.width(90.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    placeholder = { Text("/${state.maxMarks}") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.save(examId) },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(Modifier.size(20.dp),
                                color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Marks", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
