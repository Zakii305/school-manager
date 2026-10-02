package com.school.manager.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Blue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanAttendanceScreen(onNavigateBack: () -> Unit = {}) {
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val recentScans = remember {
        listOf(
            Triple("Zain Raza", "Playgroup - A", "present"),
            Triple("Zain Khan", "Playgroup - A", "absent"),
            Triple("Talha Aslam", "Playgroup - A", "present"),
            Triple("Sobhan", "Playgroup - A", "present"),
            Triple("Sara Sheikh", "Playgroup - A", "present"),
            Triple("Laiba Khan", "Playgroup - A", "present"),
            Triple("Hassan Aslam", "Playgroup - A", "present"),
            Triple("Hania Sheikh", "Playgroup - A", "present"),
            Triple("Eman Ahmed", "Playgroup - A", "present"),
            Triple("Areeba Hussain", "Playgroup - A", "present")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Attendance", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            Text("Point a QR/barcode scanner at a student's ID card — attendance is marked instantly",
                color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))

            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(60.dp).background(Blue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📷", fontSize = 28.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Ready to scan", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Waiting for a card...", color = TextMuted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Today's Scans", color = TextDark, fontSize = 14.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp))
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("STUDENT" to 1.4f, "CLASS" to 1f, "STATUS" to 0.8f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    LazyColumn(Modifier.heightIn(max = 400.dp)) {
                        items(recentScans) { (name, cls, status) ->
                            Row(Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = TextDark, fontSize = 11.sp,
                                    modifier = Modifier.weight(1.4f))
                                Text(cls, color = TextMuted, fontSize = 10.sp, modifier = Modifier.weight(1f))
                                Box(Modifier.weight(0.8f)) {
                                    Surface(shape = RoundedCornerShape(6.dp),
                                        color = if (status == "present") Color(0xFFDCFCE7) else Color(0xFFFEE2E2)) {
                                        Text(status, color = if (status == "present") Color(0xFF16A34A) else Color(0xFFDC2626),
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
            Spacer(Modifier.height(40.dp))
        }
    }
}
