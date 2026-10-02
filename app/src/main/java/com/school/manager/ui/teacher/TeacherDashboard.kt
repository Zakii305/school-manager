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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.components.ConfirmDialog
import com.school.manager.ui.theme.AmberAccent
import com.school.manager.ui.theme.IndigoDark
import com.school.manager.ui.theme.IndigoPrimary

private val PageBg = Color(0xFFF5F6FA)
private val StatBlue = Color(0xFF2563EB)
private val StatAmber = Color(0xFFF59E0B)
private val StatGreen = Color(0xFF10B981)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)

@Composable
fun TeacherDashboard(
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToExams: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: TeacherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogout by remember { mutableStateOf(false) }
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding).background(PageBg),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IndigoPrimary)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PageBg)
                .verticalScroll(rememberScrollState())
        ) {
            // ── TOP BAR ──
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B1730))
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { drawer?.open() }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text("Dashboard",
                        color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Welcome to School Manager",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.GridView, contentDescription = "Search", tint = Color.White)
                }
                IconButton(onClick = onNavigateToNotifications) {
                    Box(
                        Modifier.size(34.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.teacherName.firstOrNull()?.uppercase() ?: "T",
                            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── STAT CARDS (3 across, compact) ──
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactStatCard(
                    value = "8",
                    label = "Classes Allocated",
                    icon = Icons.Default.School,
                    accent = StatBlue,
                    modifier = Modifier.weight(1f)
                )
                CompactStatCard(
                    value = uiState.totalStudents.toString(),
                    label = "Total Students",
                    icon = Icons.Default.Person,
                    accent = StatAmber,
                    modifier = Modifier.weight(1f)
                )
                CompactStatCard(
                    value = "1",
                    label = "Pending Leave Requests",
                    icon = Icons.Default.ExitToApp,
                    accent = StatGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── TODAY'S TIMETABLE ──
            SectionCard(
                title = "Today's Timetable",
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column {
                    // Header row
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        TableHeader("PERIOD", 0.8f)
                        TableHeader("CLASS", 1.2f)
                        TableHeader("SUBJECT", 1.2f)
                        TableHeader("TIME", 1.4f)
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    // Row
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell("3", 0.8f, bold = true)
                        TableCell("Class 4 - A", 1.2f)
                        TableCell("Urdu", 1.2f)
                        TableCell("09:20 - 10:00", 1.4f)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── RECENT NOTICES ──
            SectionCard(
                title = "Recent Notices",
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NoticeItem("Winter Vacation Notice",
                        "School will remain closed for winter vacations. Classes resume as per the academic calendar.",
                        "2026-08-20")
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    NoticeItem("Parent-Teacher Meeting",
                        "PTM is scheduled for this Saturday from 9am to 1pm. All parents are requested to attend.",
                        "2026-08-20")
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    NoticeItem("Mid-Term Exam Schedule",
                        "Mid-term examinations will begin from next Monday. Date sheet has been shared with class teachers.",
                        "2026-08-20")
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showLogout) {
        ConfirmDialog(
            title = "Log out?",
            message = "You'll need to sign in again.",
            confirmLabel = "Log Out",
            isDestructive = true,
            onConfirm = onLogout,
            onDismiss = { showLogout = false }
        )
    }
}

@Composable
private fun CompactStatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent,
                    modifier = Modifier.size(16.dp))
            }
            Column {
                Text(value, color = TextDark, fontSize = 22.sp,
                    fontWeight = FontWeight.Bold)
                Text(label, color = TextMuted, fontSize = 10.sp,
                    lineHeight = 12.sp, maxLines = 2)
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = TextDark, fontSize = 14.sp,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TableHeader(text: String, weight: Float) {
    Text(text, color = TextMuted, fontSize = 10.sp,
        fontWeight = FontWeight.Medium, letterSpacing = 0.5.sp,
        modifier = Modifier.weight(weight))
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TableCell(
    text: String, weight: Float, bold: Boolean = false
) {
    Text(text, color = TextDark, fontSize = 12.sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier.weight(weight))
}

@Composable
private fun NoticeItem(title: String, body: String, date: String) {
    Column {
        Text(title, color = TextDark, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(2.dp))
        Text(body, color = TextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(date, color = TextMuted, fontSize = 10.sp)
    }
}
