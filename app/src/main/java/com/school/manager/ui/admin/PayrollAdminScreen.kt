package com.school.manager.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.IndigoPrimary

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val LineGrey = Color(0xFFE2E8F0)
private val BluePrimary = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollAdminScreen(onNavigateBack: () -> Unit = {}, viewModel: PayrollAdminViewModel = hiltViewModel()) {
    val s by viewModel.state.collectAsState()
    val ctx = LocalContext.current
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Payroll", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { drawer?.open() }) { Icon(Icons.Default.Menu, "Menu", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg).padding(10.dp)) {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = s.query, onValueChange = viewModel::onQuery,
                        placeholder = { Text("Search staff...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), singleLine = true)
                    Button(onClick = { Toast.makeText(ctx, "Generate payroll for this month", Toast.LENGTH_SHORT).show() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp)); Text("Run Payroll", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f).horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(720.dp).fillMaxHeight()) {
                            Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(horizontal = 10.dp, vertical = 10.dp)) {
                                Text("STAFF", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(200.dp))
                                Text("MONTH", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(120.dp))
                                Text("GROSS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(120.dp))
                                Text("NET", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(120.dp))
                                Text("STATUS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(100.dp))
                                Text("ACTIONS", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(60.dp))
                            }
                            HorizontalDivider(color = LineGrey)
                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = IndigoPrimary) }
                                s.filtered.isEmpty() -> Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text("No payroll records", color = TextMuted, fontSize = 13.sp) }
                                else -> Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                                    s.filtered.forEach { r ->
                                        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(r.staff, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, modifier = Modifier.width(200.dp))
                                            Text(r.month, color = TextMuted, fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(120.dp))
                                            Text(r.gross, color = TextMuted, fontSize = 12.sp, maxLines = 1, modifier = Modifier.width(120.dp))
                                            Text(r.net, color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.width(120.dp))
                                            Box(Modifier.width(100.dp)) {
                                                val paid = r.status.lowercase() == "paid"
                                                Surface(shape = RoundedCornerShape(6.dp), color = if (paid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)) {
                                                    Text(r.status.replaceFirstChar { it.uppercase() },
                                                        color = if (paid) Color(0xFF16A34A) else Color(0xFFB45309),
                                                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                                }
                                            }
                                            IconButton(onClick = { Toast.makeText(ctx, "Payslip for ${r.staff}", Toast.LENGTH_SHORT).show() },
                                                modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Download, null, tint = Color(0xFF2563EB), modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        HorizontalDivider(color = LineGrey)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
