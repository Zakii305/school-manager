package com.school.manager.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
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

data class SettingsLink(val title: String, val subtitle: String, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTabsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateTo: (String) -> Unit = {}
) {
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    val items = listOf(
        SettingsLink("School Profile", "Name, logo, address, contact",
            com.school.manager.ui.navigation.Routes.ADMIN_BRANDING),
        SettingsLink("Fee Heads", "Tuition Fee, Annual Charges, etc.",
            com.school.manager.ui.navigation.Routes.ADMIN_FEE_HEADS),
        SettingsLink("Payment Methods", "JazzCash, EasyPaisa, Bank",
            com.school.manager.ui.navigation.Routes.ADMIN_PAYMENT_METHODS),
        SettingsLink("Notification Logs", "Track sent emails and WhatsApp",
            com.school.manager.ui.navigation.Routes.ADMIN_NOTIFICATION_LOGS),
        SettingsLink("Invoices", "Bulk voucher generation and payments",
            com.school.manager.ui.navigation.Routes.ADMIN_INVOICES),
        SettingsLink("Exams", "Schedule and marks",
            com.school.manager.ui.navigation.Routes.ADMIN_EXAMS),
        SettingsLink("Classes", "Class and section management",
            com.school.manager.ui.navigation.Routes.ADMIN_CLASSES),
        SettingsLink("Subjects", "Subjects per class",
            com.school.manager.ui.navigation.Routes.ADMIN_SUBJECTS)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            Text("Configuration", color = TextDark, fontSize = 14.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
            Spacer(Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    items.forEachIndexed { i, item ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { onNavigateTo(item.route) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title, color = TextDark, fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold)
                                Text(item.subtitle, color = TextMuted, fontSize = 10.sp)
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null,
                                tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                        if (i < items.size - 1) {
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
