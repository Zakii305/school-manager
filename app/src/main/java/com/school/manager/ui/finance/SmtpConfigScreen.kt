package com.school.manager.ui.finance

import androidx.compose.foundation.background
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

data class SmtpState(
    val isLoading: Boolean = true,
    val smtpHost: String = "smtp.gmail.com",
    val smtpPort: String = "587",
    val smtpUser: String = "",
    val smtpPass: String = "",
    val fromName: String = "",
    val waPhoneId: String = "",
    val waToken: String = "",
    val waEnabled: Boolean = true,
    val triggers: Map<String, Boolean> = mapOf(
        "invoice_generated" to true,
        "payment_received" to true,
        "fee_overdue_reminder" to true,
        "new_admission" to true,
        "exam_result" to true
    ),
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class SmtpViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(SmtpState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("settings").document("smtp").get().await()
                _ui.update {
                    it.copy(
                        isLoading = false,
                        smtpHost = doc.getString("smtpHost") ?: "smtp.gmail.com",
                        smtpPort = doc.getString("smtpPort") ?: "587",
                        smtpUser = doc.getString("smtpUser") ?: "",
                        smtpPass = doc.getString("smtpPass") ?: "",
                        fromName = doc.getString("fromName") ?: "",
                        waPhoneId = doc.getString("waPhoneId") ?: "",
                        waToken = doc.getString("waToken") ?: "",
                        waEnabled = doc.getBoolean("waEnabled") ?: true
                    )
                }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }

    fun onHost(v: String) = _ui.update { it.copy(smtpHost = v) }
    fun onPort(v: String) = _ui.update { it.copy(smtpPort = v) }
    fun onUser(v: String) = _ui.update { it.copy(smtpUser = v) }
    fun onPass(v: String) = _ui.update { it.copy(smtpPass = v) }
    fun onFromName(v: String) = _ui.update { it.copy(fromName = v) }
    fun onWaPhoneId(v: String) = _ui.update { it.copy(waPhoneId = v) }
    fun onWaToken(v: String) = _ui.update { it.copy(waToken = v) }
    fun onWaEnabled(v: Boolean) = _ui.update { it.copy(waEnabled = v) }
    fun onTrigger(key: String, value: Boolean) = _ui.update {
        it.copy(triggers = it.triggers + (key to value))
    }

    fun saveSmtp() {
        val s = _ui.value
        _ui.update { it.copy(isSaving = true, successMessage = null, error = null) }
        viewModelScope.launch {
            try {
                firestore.collection("settings").document("smtp").set(
                    mapOf(
                        "smtpHost" to s.smtpHost,
                        "smtpPort" to s.smtpPort,
                        "smtpUser" to s.smtpUser,
                        "smtpPass" to s.smtpPass,
                        "fromName" to s.fromName,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
                _ui.update { it.copy(isSaving = false, successMessage = "SMTP saved ✅") }
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun saveWhatsApp() {
        val s = _ui.value
        _ui.update { it.copy(isSaving = true, successMessage = null, error = null) }
        viewModelScope.launch {
            try {
                firestore.collection("settings").document("smtp").update(
                    mapOf(
                        "waPhoneId" to s.waPhoneId,
                        "waToken" to s.waToken,
                        "waEnabled" to s.waEnabled
                    )
                ).await()
                _ui.update { it.copy(isSaving = false, successMessage = "WhatsApp saved ✅") }
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun saveTriggers() {
        val s = _ui.value
        _ui.update { it.copy(isSaving = true, successMessage = null, error = null) }
        viewModelScope.launch {
            try {
                firestore.collection("settings").document("smtp").update(
                    "triggers", _ui.value.triggers
                ).await()
                _ui.update { it.copy(isSaving = false, successMessage = "Triggers saved ✅") }
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun sendTestEmail() {
        val s = _ui.value
        if (s.smtpUser.isBlank()) {
            _ui.update { it.copy(error = "Enter SMTP username first") }
            return
        }
        viewModelScope.launch {
            try {
                firestore.collection("notification_logs").add(
                    mapOf(
                        "event" to "test_email",
                        "recipient" to s.smtpUser,
                        "subject" to "Test Email from School Manager",
                        "emailStatus" to "sent",
                        "whatsappStatus" to "—",
                        "sentAt" to System.currentTimeMillis()
                    )
                ).await()
                _ui.update { it.copy(successMessage = "Test email logged ✅") }
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun sendTestWhatsApp() {
        val s = _ui.value
        if (s.waPhoneId.isBlank() || s.waToken.isBlank()) {
            _ui.update { it.copy(error = "Enter WhatsApp Phone ID + Token first") }
            return
        }
        viewModelScope.launch {
            try {
                firestore.collection("notification_logs").add(
                    mapOf(
                        "event" to "test_whatsapp",
                        "recipient" to s.waPhoneId,
                        "subject" to "Test WhatsApp",
                        "emailStatus" to "—",
                        "whatsappStatus" to "sent",
                        "sentAt" to System.currentTimeMillis()
                    )
                ).await()
                _ui.update { it.copy(successMessage = "Test WhatsApp logged ✅") }
            } catch (e: Exception) {
                _ui.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmtpConfigScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SmtpViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(s.successMessage, s.error) {
        s.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        s.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Notifications / SMTP", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg)
            .verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // SMTP
            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SMTP Configuration", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = s.smtpHost, onValueChange = viewModel::onHost,
                        label = { Text("SMTP Host") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = s.smtpUser, onValueChange = viewModel::onUser,
                            label = { Text("Username") }, modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp), singleLine = true)
                        OutlinedTextField(value = s.smtpPort, onValueChange = viewModel::onPort,
                            label = { Text("Port") }, modifier = Modifier.weight(0.5f),
                            shape = RoundedCornerShape(10.dp), singleLine = true)
                    }
                    OutlinedTextField(value = s.smtpPass, onValueChange = viewModel::onPass,
                        label = { Text("Password") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = s.fromName, onValueChange = viewModel::onFromName,
                        label = { Text("From Name") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.saveSmtp() }, enabled = !s.isSaving,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)) {
                            Text("Save Settings")
                        }
                        OutlinedButton(onClick = { viewModel.sendTestEmail() },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)) {
                            Text("Send Test Email")
                        }
                    }
                }
            }

            // WhatsApp
            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("WhatsApp (Meta Cloud API)", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enable WhatsApp notifications", color = TextMuted, fontSize = 11.sp,
                            modifier = Modifier.weight(1f))
                        Switch(checked = s.waEnabled, onCheckedChange = viewModel::onWaEnabled,
                            colors = SwitchDefaults.colors(checkedTrackColor = Blue))
                    }
                    OutlinedTextField(value = s.waPhoneId, onValueChange = viewModel::onWaPhoneId,
                        label = { Text("Phone Number ID") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = s.waToken, onValueChange = viewModel::onWaToken,
                        label = { Text("Access Token") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.saveWhatsApp() }, enabled = !s.isSaving,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)) { Text("Save WhatsApp") }
                        OutlinedButton(onClick = { viewModel.sendTestWhatsApp() },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp)) { Text("Send Test") }
                    }
                }
            }

            // Triggers
            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notification Triggers", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Auto-send when event fires", color = TextMuted, fontSize = 10.sp)
                    s.triggers.forEach { (key, value) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(key.replace("_", " ").replaceFirstChar { it.uppercase() },
                                color = TextDark, fontSize = 12.sp,
                                modifier = Modifier.weight(1f))
                            Switch(checked = value,
                                onCheckedChange = { viewModel.onTrigger(key, it) },
                                colors = SwitchDefaults.colors(checkedTrackColor = Blue))
                        }
                    }
                    Button(onClick = { viewModel.saveTriggers() }, enabled = !s.isSaving,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp)) { Text("Save Triggers") }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
