package com.school.manager.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveRequestScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: LeaveRequestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage, uiState.error) {
        uiState.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        uiState.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Leave Request", color = Color.White, fontSize = 17.sp,
                            fontWeight = FontWeight.Bold)
                        Text("Submit and view leave requests",
                            color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                actions = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B1730)
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).background(PageBg)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Submit Leave Request", color = TextDark, fontSize = 14.sp,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            .format(Date(uiState.fromDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Date *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            .format(Date(uiState.toDate)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Date *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.reason,
                        onValueChange = viewModel::onReason,
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.submit() },
                        enabled = !uiState.isSaving,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(Modifier.size(18.dp),
                                color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Submit Request", fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("My Leave Requests", color = TextDark, fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp))

                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text("FROM", color = TextMuted, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("TO", color = TextMuted, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("REASON", color = TextMuted, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                        Text("STATUS", color = TextMuted, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f))
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (uiState.rows.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center) {
                            Text("No leave requests yet",
                                color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        uiState.rows.forEachIndexed { i, r ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(if (i % 2 == 0) CardWhite else Color(0xFFF8FAFC))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(r.fromText(), color = TextDark, fontSize = 12.sp,
                                    modifier = Modifier.weight(1f))
                                Text(r.toText(), color = TextDark, fontSize = 12.sp,
                                    modifier = Modifier.weight(1f))
                                Text(r.reason, color = TextDark, fontSize = 12.sp,
                                    modifier = Modifier.weight(1.2f), maxLines = 1)
                                StatusBadge(r.status, Modifier.weight(0.9f))
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
private fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bg, fg, label) = when (status.lowercase()) {
        "approved" -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "Approved")
        "rejected" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "Rejected")
        else -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "Pending")
    }
    Box(modifier, contentAlignment = Alignment.CenterStart) {
        Surface(shape = RoundedCornerShape(6.dp), color = bg) {
            Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}
