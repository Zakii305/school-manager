package com.school.manager.ui.invoices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Blue = Color(0xFF2563EB)
private val Green = Color(0xFF10B981)
private val Red = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }
    var showBulkDialog by remember { mutableStateOf(false) }
    var payInvoiceId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(s.successMessage, s.error) {
        s.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        s.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Invoices", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
                InvStat("Rs ${s.totalCollected.toInt()}", "Collected", Green, Modifier.weight(1f))
                InvStat("Rs ${s.totalPending.toInt()}", "Pending", Red, Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = s.query,
                            onValueChange = viewModel::onQuery,
                            placeholder = { Text("Search...", color = TextMuted, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { showBulkDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Bulk", fontSize = 11.sp)
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        Text("STUDENT", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.4f))
                        Text("MONTH", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("TOTAL", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f))
                        Text("STATUS", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f))
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue)
                        }
                    } else if (s.filtered.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No invoices yet — tap Bulk to generate", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(Modifier.heightIn(max = 500.dp)) {
                            items(s.filtered, key = { it.id }) { r ->
                                Row(Modifier.fillMaxWidth().padding(12.dp)
                                    .clickable { if (r.status != "Paid") payInvoiceId = r.id },
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(r.studentName, color = TextDark, fontSize = 11.sp,
                                        modifier = Modifier.weight(1.4f), maxLines = 1)
                                    Text(r.month, color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(1f))
                                    Text("Rs ${r.total.toInt()}", color = TextDark,
                                        fontSize = 10.sp, modifier = Modifier.weight(0.8f))
                                    Box(Modifier.weight(0.9f)) {
                                        Surface(shape = RoundedCornerShape(6.dp),
                                            color = when (r.status) {
                                                "Paid" -> Color(0xFFDCFCE7)
                                                "Partial" -> Color(0xFFFEF3C7)
                                                else -> Color(0xFFFEE2E2)
                                            }) {
                                            Text(r.status,
                                                color = when (r.status) {
                                                    "Paid" -> Green
                                                    "Partial" -> Color(0xFFD97706)
                                                    else -> Red
                                                },
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

    if (showBulkDialog) {
        BulkVoucherDialog(
            onDismiss = { showBulkDialog = false },
            onConfirm = { ids, month, amount ->
                viewModel.generateBulkVouchers(ids, month, amount)
                showBulkDialog = false
            }
        )
    }

    payInvoiceId?.let { id ->
        PayInvoiceDialog(
            invoiceId = id,
            onDismiss = { payInvoiceId = null },
            onPay = { amount ->
                viewModel.recordPayment(id, amount)
                payInvoiceId = null
            }
        )
    }
}

@Composable
private fun InvStat(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun BulkVoucherDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>, Long, Double) -> Unit
) {
    var amountText by remember { mutableStateOf("2500") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bulk Generate Vouchers") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Generates a monthly invoice for all students",
                    color = TextMuted, fontSize = 11.sp)
                OutlinedTextField(value = amountText, onValueChange = { amountText = it },
                    label = { Text("Monthly Amount (Rs)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                onConfirm(listOf("ALL"), System.currentTimeMillis(), amt)
            }, enabled = amountText.isNotBlank()) { Text("Generate") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun PayInvoiceDialog(
    invoiceId: String,
    onDismiss: () -> Unit,
    onPay: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            OutlinedTextField(value = amountText, onValueChange = { amountText = it },
                label = { Text("Amount (Rs)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp), singleLine = true)
        },
        confirmButton = {
            Button(onClick = {
                onPay(amountText.toDoubleOrNull() ?: 0.0)
            }, enabled = amountText.isNotBlank()) { Text("Pay") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

