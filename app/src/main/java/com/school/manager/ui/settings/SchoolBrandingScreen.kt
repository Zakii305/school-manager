package com.school.manager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

data class BrandingUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val tagline: String = "",
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class BrandingViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(BrandingUiState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("settings").document("branding").get().await()
                _ui.update {
                    it.copy(
                        isLoading = false,
                        name = doc.getString("name") ?: "School Manager",
                        phone = doc.getString("phone") ?: "",
                        email = doc.getString("email") ?: "",
                        address = doc.getString("address") ?: "",
                        tagline = doc.getString("tagline") ?: "Excellence in Education"
                    )
                }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }

    fun onName(v: String) = _ui.update { it.copy(name = v) }
    fun onPhone(v: String) = _ui.update { it.copy(phone = v) }
    fun onEmail(v: String) = _ui.update { it.copy(email = v) }
    fun onAddress(v: String) = _ui.update { it.copy(address = v) }
    fun onTagline(v: String) = _ui.update { it.copy(tagline = v) }

    fun save() {
        val s = _ui.value
        _ui.update { it.copy(isSaving = true, successMessage = null, error = null) }
        viewModelScope.launch {
            try {
                firestore.collection("settings").document("branding").set(
                    mapOf(
                        "name" to s.name.trim(),
                        "phone" to s.phone.trim(),
                        "email" to s.email.trim(),
                        "address" to s.address.trim(),
                        "tagline" to s.tagline.trim(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
                _ui.update { it.copy(isSaving = false, successMessage = "Saved ✅") }
            } catch (e: Exception) {
                _ui.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun clearMessages() = _ui.update { it.copy(error = null, successMessage = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolBrandingScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BrandingViewModel = hiltViewModel()
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
                title = { Text("School Branding", color = Color.White,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        if (s.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding).background(PageBg),
                contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
            }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize().padding(padding).background(PageBg)
                .verticalScroll(rememberScrollState()).padding(12.dp)
        ) {
            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("School Identity", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = s.name, onValueChange = viewModel::onName,
                        label = { Text("School Name") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = s.phone, onValueChange = viewModel::onPhone,
                        label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = s.email, onValueChange = viewModel::onEmail,
                        label = { Text("Email") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    OutlinedTextField(value = s.address, onValueChange = viewModel::onAddress,
                        label = { Text("Address") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = s.tagline, onValueChange = viewModel::onTagline,
                        label = { Text("Tagline") }, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp), singleLine = true)
                    Spacer(Modifier.height(4.dp))
                    Button(onClick = { viewModel.save() }, enabled = !s.isSaving,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)) {
                        if (s.isSaving) {
                            CircularProgressIndicator(Modifier.height(18.dp),
                                color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null,
                                modifier = Modifier.height(16.dp))
                            Spacer(Modifier.padding(start = 6.dp))
                            Text("Save Branding", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
