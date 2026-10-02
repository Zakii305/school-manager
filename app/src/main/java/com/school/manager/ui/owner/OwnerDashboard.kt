package com.school.manager.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Blue = Color(0xFF2563EB)
private val Green = Color(0xFF10B981)
private val Amber = Color(0xFFF59E0B)
private val Red = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboard(
    onLogout: () -> Unit = {},
    viewModel: OwnerViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(s.successMessage, s.error) {
        s.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        s.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Owner Console", color = Color.White,
                            fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Manage all schools on your platform",
                            color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.School, contentDescription = "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Blue,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "New School") }
        }
    ) { padding ->
        if (s.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding).background(PageBg),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue)
            }
            return@Scaffold
        }

        Column(Modifier.fillMaxSize().padding(padding).background(PageBg).padding(12.dp)) {
            // Summary stats
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OwnerStat("${s.schools.size}", "Total Schools", Blue, Modifier.weight(1f))
                OwnerStat("${s.activeSchools}", "Active", Green, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OwnerStat("${s.totalStudents}", "Students", Amber, Modifier.weight(1f))
                OwnerStat("${s.totalTeachers}", "Teachers", Red, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))

            Text("Schools", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            if (s.schools.isEmpty()) {
                Card(shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏫", fontSize = 40.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No schools yet", color = TextDark,
                                fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text("Tap + to add your first school",
                                color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(s.schools, key = { it.id }) { school ->
                        SchoolCard(
                            school = school,
                            onSuspend = { viewModel.suspendSchool(school.id) },
                            onActivate = { viewModel.activateSchool(school.id) },
                            onDelete = { confirmDelete = school.id }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSchoolDialog(
            onDismiss = { showAddDialog = false },
            onCreate = { name, email, phone ->
                viewModel.createSchool(name, email, phone)
                showAddDialog = false
            }
        )
    }

    confirmDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text("Delete school?") },
            text = { Text("This removes the school record. Users and data remain orphaned.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSchool(id)
                    confirmDelete = null
                }) { Text("Delete", color = Red) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun OwnerStat(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(value, color = accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextMuted, fontSize = 10.sp, maxLines = 2)
        }
    }
}

@Composable
private fun SchoolCard(
    school: School,
    onSuspend: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(Blue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.School, contentDescription = null,
                        tint = Blue, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(school.name, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(school.email.ifBlank { "No email" }, color = TextMuted, fontSize = 11.sp)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (school.status) {
                        "active" -> Color(0xFFDCFCE7)
                        "suspended" -> Color(0xFFFEE2E2)
                        else -> Color(0xFFFEF3C7)
                    }
                ) {
                    Text(
                        school.status.replaceFirstChar { it.uppercase() },
                        color = when (school.status) {
                            "active" -> Green
                            "suspended" -> Red
                            else -> Amber
                        },
                        fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Text("${school.studentCount} students", color = TextMuted, fontSize = 10.sp)
                Spacer(Modifier.width(12.dp))
                Text("${school.teacherCount} teachers", color = TextMuted, fontSize = 10.sp)
                Spacer(Modifier.width(12.dp))
                Text("Plan: ${school.plan}", color = TextMuted, fontSize = 10.sp)
            }
            Spacer(Modifier.height(4.dp))
            Text("Expires: ${school.expiryText()}", color = TextMuted, fontSize = 10.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (school.status == "active") {
                    TextButton(onClick = onSuspend, modifier = Modifier.weight(1f)) {
                        Text("Suspend", color = Amber, fontSize = 11.sp)
                    }
                } else {
                    TextButton(onClick = onActivate, modifier = Modifier.weight(1f)) {
                        Text("Activate", color = Green, fontSize = 11.sp)
                    }
                }
                TextButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Delete", color = Red, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AddSchoolDialog(onDismiss: () -> Unit, onCreate: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New School") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("School Name") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(name, email, phone) },
                enabled = name.isNotBlank()) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
