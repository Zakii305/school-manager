package com.school.manager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.school.manager.util.SessionManager
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

data class PaymentMethod(
    val id: String,
    val name: String,
    val accountTitle: String,
    val accountNo: String,
    val active: Boolean
)

data class PaymentMethodsState(
    val isLoading: Boolean = true,
    val methods: List<PaymentMethod> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(PaymentMethodsState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("payment_methods").get().await()
                val methods = snap.documents.map { d ->
                    PaymentMethod(
                        id = d.id,
                        name = d.getString("name") ?: "-",
                        accountTitle = d.getString("accountTitle") ?: "",
                        accountNo = d.getString("accountNo") ?: "",
                        active = d.getBoolean("active") ?: true
                    )
                }
                _ui.update { it.copy(isLoading = false, methods = methods) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addMethod(name: String, title: String, no: String) {
        if (name.isBlank()) {
            _ui.update { it.copy(error = "Name required"); return }
        }
        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val sid = SessionManager.current.schoolId
                val data = mutableMapOf<String, Any>(
                    "name" to name.trim(),
                    "accountTitle" to title.trim(),
                    "accountNo" to no.trim(),
                    "active" to true,
                    "createdAt" to System.currentTimeMillis()
                )
                if (sid.isNotBlank()) data["schoolId"] = sid
                firestore.collection("payment_methods").add(data).await()
                _ui.update { it.copy(isSaving = false, successMessage = "Added ✅") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun toggleActive(id: String, active: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("payment_methods").document(id)
                    .update("active", !active).await()
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteMethod(id: String) {
        viewModelScope.launch {
            try {
                firestore.collection("payment_methods").document(id).delete().await()
                _ui.update { it.copy(successMessage = "Deleted") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() {
        _ui.update { it.copy(error = null, successMessage = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PaymentMethodsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(s.successMessage, s.error) {
        s.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        s.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Payment Methods", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Shown on printed fee vouchers",
                                color = TextMuted, fontSize = 10.sp)
                        }
                        Button(
                            onClick = { showDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add", fontSize = 11.sp)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue)
                        }
                    } else if (s.methods.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No methods yet — tap Add", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn {
                            items(s.methods, key = { it.id }) { m ->
                                Row(Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(m.name, color = TextDark, fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold)
                                        if (m.accountTitle.isNotBlank() || m.accountNo.isNotBlank()) {
                                            Text("${m.accountTitle} • ${m.accountNo}",
                                                color = TextMuted, fontSize = 10.sp)
                                        }
                                    }
                                    Switch(checked = m.active,
                                        onCheckedChange = { viewModel.toggleActive(m.id, m.active) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = Blue))
                                    IconButton(onClick = { viewModel.deleteMethod(m.id) },
                                        modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete",
                                            tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
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

    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var title by remember { mutableStateOf("") }
        var no by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Payment Method") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("Method (e.g., JazzCash)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = title, onValueChange = { title = it },
                        label = { Text("Account Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = no, onValueChange = { no = it },
                        label = { Text("Account / Wallet No") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addMethod(name, title, no)
                    showDialog = false
                }, enabled = name.isNotBlank()) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
