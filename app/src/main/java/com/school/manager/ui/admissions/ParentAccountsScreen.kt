package com.school.manager.ui.admissions

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

data class ParentAcc(
    val id: String, val name: String, val email: String,
    val childCount: Int, val children: String
)

data class PAccState(val isLoading: Boolean = true, val rows: List<ParentAcc> = emptyList())

@HiltViewModel
class ParentAccountsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(PAccState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val usersSnap = firestore.collection("users").get().await()
                val nameMap = usersSnap.documents.associate { it.id to (it.getString("name") ?: "-") }

                val parents = usersSnap.documents.filter { it.getString("role") == "parent" }
                val list = parents.map { d ->
                    @Suppress("UNCHECKED_CAST")
                    val children = (d.get("children") as? List<String>) ?: emptyList()
                    ParentAcc(
                        id = d.id,
                        name = d.getString("name") ?: "-",
                        email = d.getString("email") ?: "-",
                        childCount = children.size,
                        children = children.mapNotNull { nameMap[it] }.joinToString(", ")
                    )
                }
                _ui.update { it.copy(isLoading = false, rows = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentAccountsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ParentAccountsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Accounts", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                        Column(Modifier.weight(1f)) {
                            Text("Parent Accounts", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Link children to parents to enable parent portal",
                                color = TextMuted, fontSize = 11.sp)
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = Blue) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White,
                                    modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Create", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("PARENT" to 1.2f, "EMAIL" to 1.4f, "CHILDREN" to 2f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue)
                        }
                    } else if (s.rows.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No parent accounts yet", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn {
                            items(s.rows, key = { it.id }) { p ->
                                Row(Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.name, color = TextDark, fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                                    Text(p.email, color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(1.4f), maxLines = 1)
                                    Text("${p.childCount} • ${p.children}",
                                        color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(2f), maxLines = 1)
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
