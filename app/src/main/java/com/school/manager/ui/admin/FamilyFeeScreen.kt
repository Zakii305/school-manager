package com.school.manager.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyFeeScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: FamilyFeeViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(s.successMessage, s.error) {
        s.successMessage?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
        s.error?.let { snackbar.showSnackbar("Error: $it"); viewModel.clearMessages() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Family Fee Collection", color = Color.White,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold) },
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
            if (s.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
                return@Scaffold
            }

            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Families list
                item {
                    Text("Select a Family",
                        color = TextDark, fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp))
                }

                if (s.families.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(Modifier.fillMaxWidth().padding(40.dp),
                                contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("No families with multiple children",
                                        color = TextDark, fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Link children to parents first",
                                        color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(s.families, key = { it.familyId }) { family ->
                        val isSelected = s.selectedFamily?.familyId == family.familyId
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    Color(0xFF2563EB).copy(alpha = 0.12f)
                                else CardWhite
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                                .clickable { viewModel.selectFamily(family) }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(family.parentName, color = TextDark,
                                        fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${family.childCount} children • ${family.parentEmail}",
                                        color = TextMuted, fontSize = 11.sp,
                                        maxLines = 1)
                                }
                                Text("Rs ${family.pendingTotal.toInt()}",
                                    color = if (family.pendingTotal > 0)
                                        Color(0xFFDC2626) else Color(0xFF16A34A),
                                    fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Pending fees for selected family
                if (s.selectedFamily != null && s.siblingFees.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Text("Pending Fees — ${s.selectedFamily!!.parentName}",
                            color = TextDark, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(4.dp))
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(
                                    Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text("STUDENT", color = TextMuted, fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.4f))
                                    Text("FEE", color = TextMuted, fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.4f))
                                    Text("AMOUNT", color = TextMuted, fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(0.9f))
                                }
                                HorizontalDivider(color = Color(0xFFE2E8F0))

                                s.siblingFees.forEach { row ->
                                    Row(
                                        Modifier.fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(row.studentName, color = TextDark,
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(1.4f), maxLines = 1)
                                        Text(row.description, color = TextMuted,
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(1.4f), maxLines = 1)
                                        Text("Rs ${row.amount.toInt()}", color = TextDark,
                                            fontSize = 11.sp,
                                            modifier = Modifier.weight(0.9f))
                                    }
                                    HorizontalDivider(color = Color(0xFFE2E8F0))
                                }

                                // Total
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(Color(0xFF2563EB).copy(alpha = 0.06f))
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Pending", color = TextDark,
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Rs ${s.siblingTotal.toInt()}",
                                        color = Color(0xFF2563EB),
                                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.payAll() },
                            enabled = !s.isSaving,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            if (s.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Receive Rs ${s.siblingTotal.toInt()} — All Children",
                                    fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
