package com.school.manager.ui.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.staff.StaffDirectoryViewModel
import com.school.manager.ui.theme.IndigoPrimary

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPayrollScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: StaffDirectoryViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val staff = s.all

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll", color = Color.White,
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

            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search staff...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null,
                        tint = TextMuted, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(Modifier.size(8.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = IndigoPrimary) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Run Payroll", color = Color.White, fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text("STAFF", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.6f))
                        Text("ROLE", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                        Text("SALARY", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("STATUS", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.9f))
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (s.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = IndigoPrimary)
                        }
                    } else if (staff.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center) {
                            Text("No staff found", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(450.dp)) {
                            items(staff, key = { it.uid }) { m ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(m.name, color = TextDark, fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1.6f), maxLines = 1)
                                    Text(m.role.replaceFirstChar { it.uppercase() },
                                        color = TextMuted, fontSize = 10.sp,
                                        modifier = Modifier.weight(1.2f))
                                    Text("Rs 45,000", color = TextDark, fontSize = 11.sp,
                                        modifier = Modifier.weight(1f))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF3C7)
                                    ) {
                                        Text("Pending",
                                            color = Color(0xFFD97706),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(
                                                horizontal = 6.dp, vertical = 3.dp))
                                    }
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
