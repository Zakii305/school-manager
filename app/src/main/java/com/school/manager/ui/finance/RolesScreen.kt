package com.school.manager.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

data class RoleRow(val id: String, val name: String, val users: Int, val modified: String)

data class RolesState(val isLoading: Boolean = true, val roles: List<RoleRow> = emptyList())

@HiltViewModel
class RolesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(RolesState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val users = firestore.collection("users").get().await()
                val countByRole = users.documents.groupingBy { it.getString("role") ?: "student" }.eachCount()
                val roles = listOf("Super Admin", "Branch Admin", "Accountant", "Front Desk", "Saba", "Teacher", "Student", "Parent")
                val list = roles.map { r ->
                    RoleRow(
                        id = r.lowercase().replace(" ", "_"),
                        name = r,
                        users = countByRole[r.lowercase()] ?: 0,
                        modified = "-"
                    )
                }
                _ui.update { it.copy(isLoading = false, roles = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolesScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RolesViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Roles & Permissions", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg).padding(12.dp)) {
            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Roles", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = Blue) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Role", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("ROLE" to 1.6f, "USERS" to 0.8f, "LAST MODIFIED" to 1.4f).forEach {
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
                        LazyColumn(Modifier.heightIn(max = 600.dp)) {
                            items(s.roles, key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.name, color = TextDark, fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.6f))
                                    Text("${r.users}", color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                                    Text(r.modified, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
                                }
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
