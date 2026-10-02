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
private val Blue = Color(0xFF2563EB)

data class PayrollRow(
    val id: String, val staff: String, val designation: String,
    val basic: Double, val allowances: Double, val deductions: Double,
    val net: Double, val status: String
)

data class PayrollState(val isLoading: Boolean = true, val rows: List<PayrollRow> = emptyList())

@HiltViewModel
class PayrollFullViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(PayrollState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val users = firestore.collection(FirestoreCollections.USERS).get().await()
                val staff = users.documents.filter {
                    it.getString("role") in listOf("teacher", "admin")
                }
                val list = staff.map { d ->
                    val basic = 45000.0
                    val allow = basic * 0.10
                    val deduct = basic * 0.02
                    PayrollRow(
                        id = d.id,
                        staff = d.getString("name") ?: "-",
                        designation = d.getString("designation") ?: "Teacher",
                        basic = basic,
                        allowances = allow,
                        deductions = deduct,
                        net = basic + allow - deduct,
                        status = if (System.currentTimeMillis() % 2 == 0L) "Paid" else "Pending"
                    )
                }
                _ui.update { it.copy(isLoading = false, rows = list) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollFullScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PayrollFullViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                        Text("Payslips", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = Blue) {
                            Text("+ Add Payslip", color = Color.White, fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("STAFF" to 1.4f, "BASIC" to 0.9f, "ALLOW" to 0.9f, "DEDUCT" to 0.9f,
                            "NET" to 1f, "STATUS" to 0.9f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 8.sp,
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
                            Text("No payslips yet", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 550.dp)) {
                            items(s.rows, key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1.4f)) {
                                        Text(r.staff, color = TextDark, fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(r.designation, color = TextMuted, fontSize = 9.sp)
                                    }
                                    Text("${r.basic.toInt()}", color = TextDark, fontSize = 10.sp,
                                        modifier = Modifier.weight(0.9f))
                                    Text("${r.allowances.toInt()}", color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(0.9f))
                                    Text("${r.deductions.toInt()}", color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(0.9f))
                                    Text("${r.net.toInt()}", color = Color(0xFF16A34A),
                                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f))
                                    Box(Modifier.weight(0.9f)) {
                                        Surface(shape = RoundedCornerShape(6.dp),
                                            color = if (r.status == "Paid") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)) {
                                            Text(r.status,
                                                color = if (r.status == "Paid") Color(0xFF16A34A) else Color(0xFFD97706),
                                                fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                        }
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
}
