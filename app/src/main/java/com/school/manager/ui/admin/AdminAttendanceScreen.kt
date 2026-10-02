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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.classes.ClassesViewModel
import com.school.manager.ui.teacher.TeacherViewModel
import com.school.manager.ui.theme.IndigoPrimary

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAttendanceScreen(
    onNavigateBack: () -> Unit = {},
    classesVm: ClassesViewModel = hiltViewModel(),
    attendanceVm: TeacherViewModel = hiltViewModel()
) {
    val classesState by classesVm.uiState.collectAsState()
    val attendanceState by attendanceVm.attendanceState.collectAsState()
    val context = LocalContext.current

    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    var selectedClassId by remember { mutableStateOf("") }
    var classExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance", color = Color.White,
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

            // Class + Date
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = classExpanded,
                    onExpandedChange = { classExpanded = !classExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    val current = classesState.classes.firstOrNull { it.id == selectedClassId }
                    OutlinedTextField(
                        value = current?.name ?: "Select Class",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Class", fontSize = 11.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(classExpanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(classExpanded,
                        onDismissRequest = { classExpanded = false }) {
                        classesState.classes.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedClassId = c.id
                                    classExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = attendanceState.date,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Table
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Column {
                    Text("Daily Attendance",
                        color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(14.dp))

                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text("STUDENT", color = TextMuted, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(2f))
                        Text("STATUS", color = TextMuted, fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (attendanceState.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = IndigoPrimary)
                        }
                    } else if (attendanceState.students.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center) {
                            Text("Select a class to view students",
                                color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(400.dp)) {
                            items(attendanceState.students, key = { it.id }) { s ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(s.name, color = TextDark, fontSize = 12.sp,
                                        modifier = Modifier.weight(2f), maxLines = 1)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (s.status == "present")
                                            Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                    ) {
                                        Text(
                                            if (s.status == "present") "Present" else "Absent",
                                            color = if (s.status == "present")
                                                Color(0xFF16A34A) else Color(0xFFDC2626),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { attendanceVm.saveAttendance() },
                    enabled = attendanceState.students.isNotEmpty() && !attendanceState.isSaving,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("Save Attendance", fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = { Toast.makeText(context, "Attendance saved ✓", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.weight(1f).height(44.dp)
                ) {
                    Text("Clear Today's Attendance", color = Color(0xFFDC2626),
                        fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
