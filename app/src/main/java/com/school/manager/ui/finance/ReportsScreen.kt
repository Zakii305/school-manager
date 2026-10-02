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

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Blue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onNavigateBack: () -> Unit = {}) {
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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

            Text("Generate a printable / downloadable PDF report",
                color = TextMuted, fontSize = 12.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportCard("Student List", "Every student with admission no, class, status",
                    Blue, Modifier.weight(1f))
                ReportCard("Staff List", "All teaching and support staff with salary",
                    Color(0xFF10B981), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportCard("Fee Collection Report", "Invoices in date range, with total",
                    Color(0xFFF59E0B), Modifier.weight(1f))
                ReportCard("Payroll Summary", "Payslips for a month with net pay",
                    Color(0xFF7C3AED), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ReportCard("Expense Report", "Expense vouchers in date range",
                    Color(0xFFEF4444), Modifier.weight(1f))
                ReportCard("Attendance Summary", "Present / Absent / Leave per student",
                    Color(0xFF06B6D4), Modifier.weight(1f))
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ReportCard(title: String, subtitle: String, accent: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier.height(150.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(title, color = TextDark, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, maxLines = 2)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = TextMuted, fontSize = 10.sp, maxLines = 3,
                    lineHeight = 13.sp)
            }
            Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)) {
                Text("📄 Generate PDF", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
