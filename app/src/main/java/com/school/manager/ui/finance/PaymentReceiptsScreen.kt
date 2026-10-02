package com.school.manager.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.school.manager.util.FirestoreCollections
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
private val AccentBlue = Color(0xFF2563EB)

data class ReceiptRow(
    val id: String, val student: String, val className: String,
    val voucher: String, val amount: Double, val status: String, val verifiedBy: String
)

data class ReceiptsState(val isLoading: Boolean = true, val rows: List<ReceiptRow> = emptyList())

@HiltViewModel
class ReceiptsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(ReceiptsState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val fees = firestore.collection(FirestoreCollections.FEES).get().await()
                val users = firestore.collection(FirestoreCollections.USERS).get().await()
                val nameMap = users.documents.associate { it.id to (it.getString("name") ?: "-") }

                val rows = fees.documents.mapNotNull { d ->
                    val sid = d.getString("studentId") ?: return@mapNotNull null
                    val paid = d.getBoolean("paid") ?: false
                    if (!paid) null else ReceiptRow(
                        id = d.id,
                        student = nameMap[sid] ?: "-",
                        className = "-",
                        voucher = d.getString("receiptNo") ?: "VCH-${d.id.takeLast(4)}",
                        amount = d.getDouble("amount") ?: 0.0,
                        status = d.getString("reviewStatus") ?: "Approved",
                        verifiedBy = d.getString("reviewedBy") ?: "Admin"
                    )
                }
                _ui.update { it.copy(isLoading = false, rows = rows) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }

    fun approve(id: String) = updateStatus(id, "Approved")
    fun reject(id: String) = updateStatus(id, "Rejected")

    private fun updateStatus(id: String, status: String) {
        viewModelScope.launch {
            try {
                firestore.collection(FirestoreCollections.FEES).document(id)
                    .update(
                        "reviewStatus", status,
                        "reviewedAt", System.currentTimeMillis(),
                        "reviewedBy", "Admin"
                    ).await()
                load()
            } catch (_: Exception) { }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentReceiptsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ReceiptsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Receipts", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Recently Reviewed", color = TextDark, fontSize = 14.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))

                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("STUDENT" to 1.4f, "VOUCHER" to 1f, "AMOUNT" to 0.9f, "STATUS" to 0.8f, "BY" to 0.8f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    } else if (s.rows.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No receipts yet", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 600.dp)) {
                            items(s.rows.take(30), key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.student, color = TextDark, fontSize = 11.sp,
                                        modifier = Modifier.weight(1.4f), maxLines = 1)
                                    Text(r.voucher, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text("Rs ${r.amount.toInt()}", color = TextDark, fontSize = 11.sp, modifier = Modifier.weight(0.9f))
                                    Box(Modifier.weight(0.8f)) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
                                            Text(r.status, color = Color(0xFF16A34A), fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                        }
                                    }
                                    Text(r.verifiedBy, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(0.8f))
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
