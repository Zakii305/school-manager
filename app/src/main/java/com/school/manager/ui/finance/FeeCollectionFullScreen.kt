package com.school.manager.ui.finance

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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
import com.school.manager.ui.teacher.TeacherFeesViewModel

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val AccentBlue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeCollectionFullScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TeacherFeesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fee Collection", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).background(PageBg)
                .verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary cards
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard("Rs 511,650", "Collected", Color(0xFF10B981), Modifier.weight(1f))
                SummaryCard("Rs 490,884", "Pending / Overdue", Color(0xFFEF4444), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard("11", "Overdue Vouchers", Color(0xFFF59E0B), Modifier.weight(1f))
                SummaryCard("333", "Vouchers Generated", Color(0xFF2563EB), Modifier.weight(1f))
            }

            // Bulk generate card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bulk Generate Vouchers", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Pick a class — Monthly Fee auto-fills from Fee Structure",
                        color = TextMuted, fontSize = 11.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = "Select Class", onValueChange = {},
                            readOnly = true, label = { Text("Class", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                        OutlinedTextField(value = "September 2026", onValueChange = {},
                            readOnly = true, label = { Text("Month", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp))
                    }
                    Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))) {
                        Text("⚡ Generate for Class", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Tab row (visual only)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TabPill("Single Students 319", selected = true, Modifier.weight(1f))
                TabPill("Families 1", selected = false, Modifier.weight(1f))
            }

            // Filters
            OutlinedTextField(
                value = "", onValueChange = {},
                placeholder = { Text("Search name, voucher #...", color = TextMuted, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true
            )

            // Table
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("VOUCHER #" to 1f, "STUDENT" to 1.2f, "CLASS" to 0.8f, "TOTAL" to 0.8f, "STATUS" to 0.8f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    } else if (s.rows.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No vouchers yet", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        s.rows.take(15).forEach { r ->
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("VCH-${r.id.takeLast(4)}", color = TextDark, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                Text(r.studentName, color = TextDark, fontSize = 10.sp,
                                    modifier = Modifier.weight(1.2f), maxLines = 1)
                                Text(r.className, color = TextMuted, fontSize = 10.sp,
                                    modifier = Modifier.weight(0.8f), maxLines = 1)
                                Text("${r.total.toInt()}", color = TextDark, fontSize = 10.sp, modifier = Modifier.weight(0.8f))
                                FeeChip(r.status, Modifier.weight(0.8f))
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SummaryCard(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, color = TextMuted, fontSize = 10.sp, maxLines = 2)
        }
    }
}

@Composable
private fun TabPill(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) AccentBlue else CardWhite,
        modifier = modifier
    ) {
        Text(label,
            color = if (selected) Color.White else TextMuted,
            fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 10.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun FeeChip(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status.lowercase()) {
        "paid" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "partial" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        else -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
    }
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        Surface(shape = RoundedCornerShape(6.dp), color = bg) {
            Text(status, color = fg, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
        }
    }
}
