package com.school.manager.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Blue = Color(0xFF2563EB)

data class UserRow(val id: String, val name: String, val email: String, val role: String)

data class UAState(val isLoading: Boolean = true, val users: List<UserRow> = emptyList())

@HiltViewModel
class UsersAccessViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(UAState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("users").get().await()
                val list = snap.documents.map { d ->
                    UserRow(
                        id = d.id,
                        name = d.getString("name") ?: "-",
                        email = d.getString("email") ?: "-",
                        role = d.getString("role") ?: "student"
                    )
                }
                _ui.update { it.copy(isLoading = false, users = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersAccessScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: UsersAccessViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Users", "Branch", "Modules")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Users & Access", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg)) {
            Row(Modifier.fillMaxWidth().background(CardWhite)
                .horizontalScroll(rememberScrollState()).padding(8.dp)) {
                tabs.forEachIndexed { i, t ->
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = if (i == tab) Blue else Color.Transparent,
                        modifier = Modifier.padding(end = 4.dp)) {
                        Text(t, color = if (i == tab) Color.White else TextMuted,
                            fontSize = 12.sp, fontWeight = if (i == tab) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.clickable { tab = i }
                                .padding(horizontal = 14.dp, vertical = 10.dp))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))

            Column(Modifier.fillMaxSize().padding(12.dp)) {
                when (tab) {
                    0 -> UsersTab(s)
                    1 -> BranchTab()
                    2 -> ModulesTab()
                }
            }
        }
    }
}

@Composable
private fun UsersTab(s: UAState) {
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                listOf("USER" to 1.4f, "ROLE" to 1f, "STATUS" to 0.8f).forEach {
                    Text(it.first, color = TextMuted, fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))
            if (s.isLoading) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Blue)
                }
            } else {
                s.users.take(20).forEach { u ->
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1.4f)) {
                            Text(u.name, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(u.email, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                        }
                        Text(u.role.replaceFirstChar { it.uppercase() }, color = TextMuted,
                            fontSize = 11.sp, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                            Text("Active", color = Color(0xFF16A34A), fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                }
            }
        }
    }
}

@Composable
private fun BranchTab() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite)) {
            Column(Modifier.padding(14.dp)) {
                Text("School Manager", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Housing Colony Sheikhupura, Pakistan", color = TextMuted, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("190 Students", color = TextMuted, fontSize = 10.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("23 Staff", color = TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun ModulesTab() {
    val modules = listOf(
        "Certificates & ID Cards", "Library", "Result Cards", "School Assets",
        "Transport", "Attendance", "Classes & Sections", "Exams & Results",
        "Expenses", "Fee Collection", "Payroll", "Staff Attendance",
        "Students", "Teachers & Staff", "Timetable", "Branch Management",
        "Notifications / SMTP", "Roles & Permissions"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        modules.forEach { m ->
            Card(shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(m, color = TextDark, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedTrackColor = Blue))
                }
            }
        }
    }
}
