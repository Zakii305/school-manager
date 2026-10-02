package com.school.manager.ui.admin

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
private val RedDelete = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliesListScreen(
    onNavigateBack: () -> Unit = {},
    onAddFamily: () -> Unit = {},
    onOpenFamily: (String) -> Unit = {},
    viewModel: FamiliesListViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Families", color = Color.White, fontSize = 17.sp,
                    fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg)) {

            // ═══════ FILTERS ═══════
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) {
                Column(Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    OutlinedTextField(
                        value = s.query,
                        onValueChange = viewModel::onQuery,
                        placeholder = { Text("Search families...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, null,
                            tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        trailingIcon = if (s.query.isNotBlank()) {
                            { IconButton(onClick = { viewModel.onQuery("") }) {
                                Icon(Icons.Default.Close, null,
                                    tint = TextMuted, modifier = Modifier.size(16.dp))
                            } }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Button(
                        onClick = onAddFamily,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Family", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            maxLines = 1)
                    }

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF))
                    ) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, null,
                                tint = BluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Link siblings to a family from each student's Admission Form (Family field).",
                                fontSize = 11.sp, color = Color(0xFF1E3A8A))
                        }
                    }
                }
            }

            // ═══════ TABLE ═══════
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 10.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f)
                        .horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(760.dp).fillMaxHeight()) {
                            Row(
                                Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("FAMILY", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(180.dp))
                                Text("FATHER NAME", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(160.dp))
                                Text("PHONE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(140.dp))
                                Text("CHILDREN", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(90.dp))
                                Text("PENDING FEE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(140.dp))
                                Spacer(Modifier.width(50.dp))
                            }
                            HorizontalDivider(color = LineGrey)

                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = IndigoPrimary)
                                }
                                s.filtered.isEmpty() -> Box(
                                    Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No families found", color = TextMuted, fontSize = 13.sp)
                                }
                                else -> Column(
                                    Modifier.fillMaxWidth().weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    s.filtered.forEach { f ->
                                        Row(
                                            Modifier.fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(Modifier.width(180.dp)) {
                                                Text(f.name, color = TextDark, fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium, maxLines = 1)
                                                if (f.cnic.isNotBlank()) {
                                                    Text("CNIC: ${f.cnic}", color = TextMuted,
                                                        fontSize = 10.sp, maxLines = 1)
                                                }
                                            }
                                            Text(f.fatherName, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(160.dp))
                                            Text(f.phone, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(140.dp))
                                            Text(f.childrenCount.toString(), color = TextDark,
                                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                                maxLines = 1, modifier = Modifier.width(90.dp))
                                            Text("Rs ${String.format("%,.0f", f.pendingFee)}",
                                                color = if (f.pendingFee > 0) RedDelete else TextMuted,
                                                fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                                maxLines = 1, modifier = Modifier.width(140.dp))
                                            IconButton(onClick = { onOpenFamily(f.id) },
                                                modifier = Modifier.width(50.dp).size(32.dp)) {
                                                Icon(Icons.Default.ArrowForward, null,
                                                    tint = TextMuted, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        HorizontalDivider(color = LineGrey)
                                    }
                                }
                            }
                        }
                    }

                    if (s.filtered.isNotEmpty()) {
                        Row(Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.Center) {
                            Text("Showing 1-${s.filtered.size} of ${s.families.size}",
                                fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}
