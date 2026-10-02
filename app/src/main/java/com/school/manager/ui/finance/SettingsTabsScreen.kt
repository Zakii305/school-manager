package com.school.manager.ui.finance

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

data class SettingsUiState(
    val isLoading: Boolean = true,
    val schoolName: String = "",
    val session: String = "2026-2027",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val currency: String = "Rs",
    val isSaving: Boolean = false
)

@HiltViewModel
class SettingsTabsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(SettingsUiState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("settings").document("branding").get().await()
                _ui.update {
                    it.copy(
                        isLoading = false,
                        schoolName = doc.getString("name") ?: "School Manager",
                        session = doc.getString("session") ?: "2026-2027",
                        address = doc.getString("address") ?: "",
                        phone = doc.getString("phone") ?: "",
                        email = doc.getString("email") ?: "",
                        currency = doc.getString("currency") ?: "Rs"
                    )
                }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }

    fun onSchoolName(v: String) = _ui.update { it.copy(schoolName = v) }
    fun onSession(v: String) = _ui.update { it.copy(session = v) }
    fun onAddress(v: String) = _ui.update { it.copy(address = v) }
    fun onPhone(v: String) = _ui.update { it.copy(phone = v) }
    fun onEmail(v: String) = _ui.update { it.copy(email = v) }
    fun onCurrency(v: String) = _ui.update { it.copy(currency = v) }

    fun save() {
        val s = _ui.value
        _ui.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                firestore.collection("settings").document("branding").set(
                    mapOf(
                        "name" to s.schoolName,
                        "session" to s.session,
                        "address" to s.address,
                        "phone" to s.phone,
                        "email" to s.email,
                        "currency" to s.currency,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
                _ui.update { it.copy(isSaving = false) }
            } catch (_: Exception) { _ui.update { it.copy(isSaving = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsTabsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val tabs = listOf(
        "School Profile", "Invoice Settings", "Late Fee", "Fee Heads",
        "Payment Methods", "Academic Session", "Users & Access", "Security"
    )
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            // Tab Row
            Row(
                Modifier.fillMaxWidth().background(CardWhite)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                tabs.forEachIndexed { i, t ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (i == selectedTab) Blue else Color.Transparent,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(t,
                            color = if (i == selectedTab) Color.White else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (i == selectedTab) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier
                                .clickable { selectedTab = i }
                                .padding(horizontal = 14.dp, vertical = 10.dp))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))

            // Content
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)
            ) {
                when (selectedTab) {
                    0 -> SchoolProfileTab(s, viewModel)
                    1 -> SimpleInfoTab("Invoice / Voucher Settings",
                        "Urdu note footer: برائے مہربانی فیس مقررہ تاریخ سے پہلے جمع کروائیں۔")
                    2 -> LateFeeTab()
                    3 -> FeeHeadsTab()
                    4 -> PaymentMethodsTab()
                    5 -> AcademicSessionTab(s, viewModel)
                    6 -> UsersAccessTab()
                    7 -> SecurityTab()
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SchoolProfileTab(s: SettingsUiState, viewModel: SettingsTabsViewModel) {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("School Profile", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = s.schoolName, onValueChange = viewModel::onSchoolName,
                label = { Text("School Name") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp), singleLine = true)
            OutlinedTextField(value = s.session, onValueChange = viewModel::onSession,
                label = { Text("Academic Session") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp), singleLine = true)
            OutlinedTextField(value = s.address, onValueChange = viewModel::onAddress,
                label = { Text("Address") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = s.phone, onValueChange = viewModel::onPhone,
                    label = { Text("Phone") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = s.email, onValueChange = viewModel::onEmail,
                    label = { Text("Email") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
            }
            OutlinedTextField(value = s.currency, onValueChange = viewModel::onCurrency,
                label = { Text("Currency Symbol") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp), singleLine = true)
            Button(onClick = { viewModel.save() }, enabled = !s.isSaving,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(10.dp)) {
                Text(if (s.isSaving) "Saving..." else "Save Profile", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SimpleInfoTab(title: String, body: String) {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = body, onValueChange = {},
                modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(10.dp))
            Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LateFeeTab() {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Late Fee Slabs", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = "8", onValueChange = {},
                    label = { Text("From Day") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = "15", onValueChange = {},
                    label = { Text("To Day") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = "50", onValueChange = {},
                    label = { Text("Fine") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
            }
            Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)) { Text("Save Late Fee Slabs") }
        }
    }
}

@Composable
private fun FeeHeadsTab() {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Fee Heads", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Reusable 'quick add' buttons on every voucher",
                color = TextMuted, fontSize = 11.sp)
            FeeHeadRow("Tuition Fee", "2500")
            FeeHeadRow("Annual Charges", "3000")
            FeeHeadRow("Misc. Fund", "300")
            Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)) { Text("+ Add Fee Head") }
        }
    }
}

@Composable
private fun FeeHeadRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = label, onValueChange = {},
            modifier = Modifier.weight(2f), shape = RoundedCornerShape(10.dp), singleLine = true)
        OutlinedTextField(value = value, onValueChange = {},
            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true)
    }
}

@Composable
private fun PaymentMethodsTab() {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Payment Methods", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Shown to parents on printed fee vouchers",
                color = TextMuted, fontSize = 11.sp)
            PaymentMethodRow("JazzCash", "03079497527")
            PaymentMethodRow("EasyPaisa", "03079497527")
            PaymentMethodRow("Bank Transfer", "PK00-0000-0000-0000")
            Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)) { Text("Save Payment Methods") }
        }
    }
}

@Composable
private fun PaymentMethodRow(name: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(name, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        OutlinedTextField(value = value, onValueChange = {},
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true)
    }
}

@Composable
private fun AcademicSessionTab(s: SettingsUiState, viewModel: SettingsTabsViewModel) {
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Academic Session", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = s.session, onValueChange = viewModel::onSession,
                label = { Text("Current Session") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp), singleLine = true)
            Button(onClick = { viewModel.save() }, modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)) { Text("Save Session") }
        }
    }
}

@Composable
private fun UsersAccessTab() {
    SimpleInfoTab("Users & Access", "Manage who can log in and what they can see. Uses Roles from the Roles module.")
}

@Composable
private fun SecurityTab() {
    SimpleInfoTab("Security", "Configure 2FA, password policy, session timeout, and IP whitelist.")
}

