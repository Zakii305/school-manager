package com.school.manager.ui.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import com.school.manager.ui.theme.IndigoPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---------- ViewModel ----------
data class FeedbackState(
    val subject: String = "",
    val message: String = "",
    val isSending: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _state = MutableStateFlow(FeedbackState())
    val state = _state.asStateFlow()

    fun onSubject(v: String) = _state.update { it.copy(subject = v, error = null) }
    fun onMessage(v: String) = _state.update { it.copy(message = v, error = null) }

    fun submit() {
        val s = _state.value
        if (s.subject.isBlank() || s.message.isBlank()) {
            _state.update { it.copy(error = "Both fields required") }
            return
        }
        _state.update { it.copy(isSending = true, error = null) }
        val uid = auth.currentUser?.uid ?: ""
        val email = auth.currentUser?.email ?: ""
        firestore.collection("feedback").add(
            mapOf(
                "uid" to uid,
                "email" to email,
                "subject" to s.subject.trim(),
                "message" to s.message.trim(),
                "createdAt" to System.currentTimeMillis()
            )
        ).addOnSuccessListener {
            _state.value = FeedbackState(success = true)
        }.addOnFailureListener {
            _state.update { it.copy(isSending = false, error = it.message) }
        }
    }

    fun reset() { _state.value = FeedbackState() }
}

// ---------- Screens ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoTopBar(title: String, onBack: () -> Unit) = TopAppBar(
    title = { Text(title) },
    navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = IndigoPrimary,
        titleContentColor = Color.White,
        navigationIconContentColor = Color.White
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onNavigateBack: () -> Unit) {
    val vm: FeedbackViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val s by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(s.success) {
        if (s.success) {
            snackbar.showSnackbar("Feedback sent. Thank you!")
            vm.reset()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { InfoTopBar("Feedback", onNavigateBack) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("We'd love your feedback",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Text("Report issues or suggest improvements.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = s.subject, onValueChange = vm::onSubject,
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = s.message, onValueChange = vm::onMessage,
                label = { Text("Message") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(14.dp)
            )
            s.error?.let { Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall) }
            Button(
                onClick = { vm.submit() },
                enabled = !s.isSending,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (s.isSending) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(Modifier.padding(start = 8.dp))
                    Text("Send Feedback", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Scaffold(topBar = { InfoTopBar("About School", onNavigateBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IndigoPrimary.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null,
                        tint = IndigoPrimary, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("School Manager",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary)
                    Text("Version 1.0.0 (build 1)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SectionCard("Our Mission",
                "To provide quality education that empowers every student to reach their full potential.")
            SectionCard("Our Vision",
                "To be a leading institution that shapes well-rounded, confident, and compassionate citizens.")
            SectionCard("About",
                "School Manager is a modern school management platform designed for schools that value organization, transparency, and communication between staff, students, and parents.")
            SectionCard("Acknowledgements",
                "Built with Kotlin, Jetpack Compose, and Firebase.")

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionCard(title: String, body: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold, color = IndigoPrimary)
            Spacer(Modifier.height(6.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val phone = "+92-300-0000000"
    val email = "info@schoolmanager.app"
    val address = "123 Education Lane, City"

    Scaffold(topBar = { InfoTopBar("Contact", onNavigateBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ContactRow(Icons.Default.Phone, "Phone", phone) {
                val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                context.startActivity(i)
            }
            ContactRow(Icons.Default.Email, "Email", email) {
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                context.startActivity(i)
            }
            ContactRow(Icons.Default.LocationOn, "Address", address) {
                val i = Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=${Uri.encode(address)}"))
                context.startActivity(i)
            }
            Spacer(Modifier.height(10.dp))
            Text("Office hours: Mon–Sat, 8:00 AM – 3:00 PM",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = IndigoPrimary)
            Spacer(Modifier.padding(start = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium)
            }
            Button(onClick = onClick, shape = RoundedCornerShape(10.dp)) {
                Text("Open")
            }
        }
    }
}
