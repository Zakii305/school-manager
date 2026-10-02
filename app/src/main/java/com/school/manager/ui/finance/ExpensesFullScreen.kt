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
private val RedAccent = Color(0xFFEF4444)

data class ExpenseRow(
    val id: String, val voucher: String, val category: String,
    val description: String, val paidTo: String, val date: String, val amount: Double
)

data class ExpensesState(val isLoading: Boolean = true, val rows: List<ExpenseRow> = emptyList(), val total: Double = 0.0)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(ExpensesState())
    val uiState = _ui.asStateFlow()
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("expenses").get().await()
                val rows = snap.documents.map { d ->
                    ExpenseRow(
                        id = d.id,
                        voucher = d.getString("voucherNo") ?: "EXP-${d.id.takeLast(3)}",
                        category = d.getString("category") ?: "General",
                        description = d.getString("description") ?: "-",
                        paidTo = d.getString("paidTo") ?: "-",
                        date = d.getLong("date")?.let { fmt.format(Date(it)) } ?: "-",
                        amount = d.getDouble("amount") ?: 0.0
                    )
                }.sortedByDescending { it.date }
                _ui.update { it.copy(isLoading = false, rows = rows, total = rows.sumOf { r -> r.amount }) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesFullScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ExpenseSummary("Rs ${s.total.toInt()}", "Total Expenses", RedAccent, Modifier.weight(1f))
                ExpenseSummary("${s.rows.size}", "Vouchers Recorded", Color(0xFF2563EB), Modifier.weight(1f))
                ExpenseSummary("Sep 2026", "Current Period", Color(0xFF10B981), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = "", onValueChange = {},
                            placeholder = { Text("Search description...", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), singleLine = true)
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = AccentRed()) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Expense", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("VOUCHER #" to 1f, "CATEGORY" to 1.1f, "DESCRIPTION" to 1.3f, "PAID TO" to 1f, "DATE" to 1f, "AMOUNT" to 0.9f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = RedAccent)
                        }
                    } else if (s.rows.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No expenses recorded", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 500.dp)) {
                            items(s.rows.take(20), key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.voucher, color = TextDark, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Box(Modifier.weight(1.1f)) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFEDE9FE)) {
                                            Text(r.category, color = Color(0xFF6D28D9), fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                        }
                                    }
                                    Text(r.description, color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(1.3f), maxLines = 1)
                                    Text(r.paidTo, color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(1f), maxLines = 1)
                                    Text(r.date, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                    Text("${r.amount.toInt()}", color = RedAccent, fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f))
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

@Composable
private fun AccentRed() = Color(0xFFEF4444)

@Composable
private fun ExpenseSummary(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, color = TextMuted, fontSize = 10.sp, maxLines = 2)
        }
    }
}
