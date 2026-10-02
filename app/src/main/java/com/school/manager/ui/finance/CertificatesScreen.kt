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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Blue = Color(0xFF2563EB)

data class CertificateRow(
    val id: String, val certNo: String, val type: String,
    val forWhom: String, val issueDate: String, val issuedBy: String
)

data class CertState(val isLoading: Boolean = true, val rows: List<CertificateRow> = emptyList())

@HiltViewModel
class CertificatesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(CertState())
    val uiState = _ui.asStateFlow()
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("certificates").get().await()
                val list = snap.documents.map { d ->
                    CertificateRow(
                        id = d.id,
                        certNo = d.getString("certNo") ?: "CERT-${d.id.takeLast(4)}",
                        type = d.getString("type") ?: "Bonafide",
                        forWhom = d.getString("forWhom") ?: "-",
                        issueDate = d.getLong("issueDate")?.let { fmt.format(Date(it)) } ?: "-",
                        issuedBy = d.getString("issuedBy") ?: "Admin"
                    )
                }
                _ui.update { it.copy(isLoading = false, rows = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificatesScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: CertificatesViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Certificates & ID Cards", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Certificates", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF1F5F9)) {
                        Text("ID Cards", color = TextDark, fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Blue) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White,
                                modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Generate Certificate", color = Color.White,
                                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("CERT NO." to 1.2f, "TYPE" to 1.4f, "FOR" to 1.2f, "ISSUE" to 1f, "BY" to 0.8f).forEach {
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
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No certificates generated",
                                    color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text("Tap Generate Certificate to begin",
                                    color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 600.dp)) {
                            items(s.rows, key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.certNo, color = TextDark, fontSize = 10.sp, modifier = Modifier.weight(1.2f))
                                    Text(r.type, color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(1.4f), maxLines = 1)
                                    Text(r.forWhom, color = TextDark, fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.2f), maxLines = 1)
                                    Text(r.issueDate, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text(r.issuedBy, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(0.8f))
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
