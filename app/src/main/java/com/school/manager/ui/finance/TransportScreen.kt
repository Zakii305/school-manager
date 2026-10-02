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
private val Green = Color(0xFF10B981)

data class TransportRoute(
    val id: String, val route: String, val vehicle: String,
    val driver: String, val capacity: Int, val monthlyFee: Double
)

data class TransportState(
    val isLoading: Boolean = true,
    val routes: List<TransportRoute> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TransportViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(TransportState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("transport_routes").get().await()
                val list = snap.documents.map { d ->
                    TransportRoute(
                        id = d.id,
                        route = d.getString("route") ?: "-",
                        vehicle = d.getString("vehicleNo") ?: "-",
                        driver = d.getString("driver") ?: "-",
                        capacity = (d.getLong("capacity") ?: 0L).toInt(),
                        monthlyFee = d.getDouble("monthlyFee") ?: 0.0
                    )
                }
                _ui.update { it.copy(isLoading = false, routes = list) }
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addRoute(route: String, vehicle: String, driver: String, capacity: Int, fee: Double) {
        if (route.isBlank()) {
            _ui.update { it.copy(error = "Route name required"); return
            }
        }
        _ui.update { it.copy(isSaving = true, error = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val sid = SessionManager.current.schoolId
                val data = mutableMapOf<String, Any>(
                    "route" to route.trim(),
                    "vehicleNo" to vehicle.trim(),
                    "driver" to driver.trim(),
                    "capacity" to capacity,
                    "monthlyFee" to fee,
                    "createdAt" to System.currentTimeMillis()
                )
                if (sid.isNotBlank()) data["schoolId"] = sid
                firestore.collection("transport_routes").add(data).await()
                _ui.update { it.copy(isSaving = false, successMessage = "Route added ✅") }
                load()
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TransportViewModel = hiltViewModel()
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
                title = { Text("Transport", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                        Text("Routes", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Route", fontSize = 11.sp)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Green)
                        }
                    } else if (s.routes.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No routes yet — tap Add", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 550.dp)) {
                            items(s.routes, key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(r.route, color = TextDark, fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text("${r.vehicle} • ${r.driver}",
                                            color = TextMuted, fontSize = 10.sp)
                                    }
                                    Text("Rs ${r.monthlyFee.toInt()}", color = TextDark, fontSize = 11.sp)
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
        var route by remember { mutableStateOf("") }
        var vehicle by remember { mutableStateOf("") }
        var driver by remember { mutableStateOf("") }
        var capacity by remember { mutableStateOf("40") }
        var fee by remember { mutableStateOf("2500") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Route") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = route, onValueChange = { route = it },
                        label = { Text("Route Name") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = vehicle, onValueChange = { vehicle = it },
                        label = { Text("Vehicle No") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = driver, onValueChange = { driver = it },
                        label = { Text("Driver Name") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = capacity, onValueChange = { capacity = it },
                            label = { Text("Capacity") }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp), singleLine = true)
                        OutlinedTextField(value = fee, onValueChange = { fee = it },
                            label = { Text("Monthly Fee") }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp), singleLine = true)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addRoute(route, vehicle, driver,
                        capacity.toIntOrNull() ?: 40, fee.toDoubleOrNull() ?: 0.0)
                    showDialog = false
                }, enabled = route.isNotBlank()) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}
