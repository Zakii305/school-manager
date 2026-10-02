package com.school.manager.ui.finance

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
fun SystemScreen(
    onNavigateBack: () -> Unit = {},
    startTab: Int = 0
) {
    val context = LocalContext.current
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    var tab by remember { mutableStateOf(startTab) }
    val tabs = listOf("Backups", "Subscription", "Software Updates")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg)) {
            Row(Modifier.fillMaxWidth().background(CardWhite)
                .horizontalScroll(rememberScrollState()).padding(8.dp)) {
                tabs.forEachIndexed { i, t ->
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = if (i == tab) Blue else Color.Transparent,
                        modifier = Modifier.padding(end = 4.dp)) {
                        Text(t, color = if (i == tab) Color.White else TextMuted,
                            fontSize = 12.sp, fontWeight = if (i == tab) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.clickable { tab = i }
                                .padding(horizontal = 14.dp, vertical = 10.dp))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))

            Column(Modifier.fillMaxSize().padding(12.dp)) {
                when (tab) {
                    0 -> BackupsTab()
                    1 -> SubscriptionTab()
                    2 -> SoftwareTab()
                }
            }
        }
    }
}

@Composable
private fun BackupsTab() {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Backups", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Download a full database backup anytime",
                color = TextMuted, fontSize = 11.sp)
            Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)) { Text("💾 Backup Now") }
            HorizontalDivider()
            Text("Recent Backups", color = TextDark, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            listOf(
                "backup_2026-09-29.sql" to "1,270 KB",
                "backup_2026-09-27.sql" to "1,240 KB",
                "backup_2026-09-26.sql" to "1,196 KB"
            ).forEach { (name, size) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(name, color = TextDark, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(size, color = TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun SubscriptionTab() {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Subscription", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFDCFCE7)) {
                    Text("Active", color = Color(0xFF16A34A), fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            listOf(
                "Plan" to "Trial",
                "Amount Paid" to "PKR 0",
                "Current Plan Price" to "PKR 0",
                "Status" to "Active",
                "Expires" to "28 Oct 2026",
                "Remaining" to "29 Days",
                "Student Limit" to "100"
            ).forEach { (label, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = TextMuted, fontSize = 11.sp)
                    Text(value, color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)) { Text("Check Now") }
        }
    }
}

@Composable
private fun SoftwareTab() {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Software Updates", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Current Version", color = TextMuted, fontSize = 11.sp)
            Text("v2.26.0", color = TextDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { Toast.makeText(context, "Coming in next update", Toast.LENGTH_SHORT).show() }, modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(10.dp)) { Text("⚡ Check for Updates") }
        }
    }
}
