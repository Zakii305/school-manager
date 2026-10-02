package com.school.manager.ui.admin

import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStaffAttendanceScreen(
    onNavigateBack: () -> Unit = {},
    staffVm: StaffDirectoryViewModel = hiltViewModel()
) {
    val s by staffVm.uiState.collectAsState()
    val context = LocalContext.current

    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val today = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date())
    val staff = s.all

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Attendance", color = Color.White,
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = "All Departments",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Department", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = today,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Column {
                    Text("Staff Daily Attendance",
                        color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp))

                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text("STAFF", color = TextMuted, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
                        Text("STATUS", color = TextMuted, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
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
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(2f)) {
                                        Text(m.name, color = TextDark, fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        Text(m.role.replaceFirstChar { it.uppercase() },
                                            color = TextMuted, fontSize = 10.sp)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text("Present",
                                            color = Color(0xFF16A34A), fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp, vertical = 4.dp))
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { Toast.makeText(context, "Attendance saved ✓", Toast.LENGTH_SHORT).show() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(44.dp)
            ) {
                Text("Save Attendance", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
