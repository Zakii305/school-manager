package com.school.manager.ui.admissions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdmissionsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AdmissionsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val filters = listOf("All", "New", "Test Scheduled", "Interview Scheduled", "Approved", "Converted", "Rejected")
    val filtered = if (s.filter == "All") s.enquiries else s.enquiries.filter { it.status == s.filter }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admission Enquiries", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            // Filters
            Row(Modifier.fillMaxWidth().background(CardWhite)
                .horizontalScroll(rememberScrollState()).padding(8.dp)) {
                filters.forEach { f ->
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = if (s.filter == f) Blue else Color.Transparent,
                        modifier = Modifier.padding(end = 4.dp)) {
                        Text(f, color = if (s.filter == f) Color.White else TextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { viewModel.setFilter(f) }
                                .padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                }
            }
            HorizontalDivider(color = Color(0xFFE2E8F0))

            Spacer(Modifier.height(8.dp))

            Card(shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("${filtered.size} Enquiries", color = TextDark,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = Blue) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White,
                                    modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("New Enquiry", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(12.dp)) {
                        listOf("APPLICANT" to 1.6f, "CLASS" to 1f, "PHONE" to 1.2f, "STATUS" to 1f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Blue)
                        }
                    } else if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No enquiries", color = TextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn {
                            items(filtered, key = { it.id }) { e ->
                                Row(Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1.6f)) {
                                        Text(e.applicant, color = TextDark, fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(e.father, color = TextMuted, fontSize = 10.sp, maxLines = 1)
                                    }
                                    Text(e.desiredClass, color = TextMuted, fontSize = 11.sp,
                                        modifier = Modifier.weight(1f))
                                    Text(e.phone, color = TextMuted, fontSize = 11.sp,
                                        modifier = Modifier.weight(1.2f))
                                    StatusChipAdm(e.status, Modifier.weight(1f))
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
private fun StatusChipAdm(status: String, modifier: Modifier = Modifier) {
    val (bg, fg) = when (status.lowercase()) {
        "approved", "converted" -> Color(0xFFDCFCE7) to Color(0xFF16A34A)
        "rejected" -> Color(0xFFFEE2E2) to Color(0xFFDC2626)
        "interview scheduled", "test scheduled" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
        else -> Color(0xFFDBEAFE) to Color(0xFF2563EB)
    }
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        Surface(shape = RoundedCornerShape(6.dp), color = bg) {
            Text(status, color = fg, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
        }
    }
}

