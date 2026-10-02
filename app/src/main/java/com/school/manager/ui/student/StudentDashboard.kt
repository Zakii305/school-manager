package com.school.manager.ui.student

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.components.ConfirmDialog
import com.school.manager.ui.theme.AmberAccent
import com.school.manager.ui.theme.IndigoDark
import com.school.manager.ui.theme.IndigoPrimary

// Reference palette
private val TileGreen = Color(0xFF43A047)
private val TilePurple = Color(0xFF7B1FA2)
private val TileBlue = Color(0xFF2196F3)
private val MenuTileBg = Color(0xFFE8EAF6)
private val PageBg = Color(0xFFF5F6FA)
private val MenuIconColor = Color(0xFF3949AB)

@Composable
fun StudentDashboard(
    onNavigateToResults: () -> Unit = {},
    onNavigateToTimetable: () -> Unit = {},
    onNavigateToAssignments: () -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToReportCard: () -> Unit = {},
    onNavigateToAllEntries: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToFees: () -> Unit = {},
    onNavigateToNotices: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: StudentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLogout by remember { mutableStateOf(false) }
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold { padding ->
        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(padding).background(PageBg),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = IndigoPrimary) }
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
                    .background(IndigoPrimary)
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { drawer?.open() }) {
                    Icon(Icons.Default.GridView, contentDescription = "Menu", tint = Color.White)
                }
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(24.dp).clip(CircleShape).background(AmberAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null,
                            tint = IndigoDark, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("School Manager",
                        color = Color.White, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                }
                IconButton(onClick = onNavigateToProfile) {
                    Box(
                        Modifier.size(34.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.studentName.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White, fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // ── PROFILE ROW ──
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        uiState.studentName.ifBlank { "Student" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1C2E)
                    )
                    Text("Student Account",
                        fontSize = 12.sp,
                        color = Color(0xFF6B6E80))
                }
                Box(
                    Modifier.size(48.dp).clip(CircleShape)
                        .background(TileGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        uiState.studentName.firstOrNull()?.uppercase() ?: "?",
                        color = TileGreen, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // ── STAT TILES 2x2 ──
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReferenceStatTile(
                    label = "Attendance",
                    value = if (uiState.attendancePercent > 0) "${uiState.attendancePercent}%" else "Not Taken",
                    icon = Icons.Default.CheckCircle,
                    accent = TileGreen,
                    modifier = Modifier.weight(1f)
                )
                ReferenceStatTile(
                    label = "Dues",
                    value = if (uiState.pendingFees > 0) "1" else "0",
                    icon = Icons.Default.Payments,
                    accent = TilePurple,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReferenceStatTile(
                    label = "Balance",
                    value = if (uiState.pendingFees > 0) "-${uiState.pendingFees.toInt()}" else "0",
                    icon = Icons.Default.AccountBalanceWallet,
                    accent = TileBlue,
                    modifier = Modifier.weight(1f)
                )
                ReferenceStatTile(
                    label = "Status",
                    value = "Active",
                    icon = Icons.Default.CheckCircle,
                    accent = TilePurple,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── MAIN MENU HEADER ──
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Main Menu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C2E),
                    modifier = Modifier.weight(1f))
                Icon(Icons.Default.GridView, contentDescription = null,
                    tint = MenuIconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))

            // ── MENU GRID 4x3 ──
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReferenceMenuTile("Attendance", Icons.Default.CheckCircle,
                    onNavigateToAttendance, Modifier.weight(1f))
                ReferenceMenuTile("Payments", Icons.Default.Payments,
                    onNavigateToFees, Modifier.weight(1f))
                ReferenceMenuTile("Timetable", Icons.Default.CalendarMonth,
                    onNavigateToTimetable, Modifier.weight(1f))
                ReferenceMenuTile("Marks", Icons.Default.Grade,
                    onNavigateToResults, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReferenceMenuTile("Online Classes", Icons.Default.VideoCall,
                    { Toast.makeText(context, "Feature coming in next update", Toast.LENGTH_SHORT).show() },
                    Modifier.weight(1f))
                ReferenceMenuTile("Homework Diary", Icons.AutoMirrored.Filled.Assignment,
                    onNavigateToAssignments, Modifier.weight(1f))
                ReferenceMenuTile("Notice Board", Icons.Default.EventNote,
                    onNavigateToNotices, Modifier.weight(1f))
                ReferenceMenuTile("Study Material", Icons.Default.MenuBook,
                    { Toast.makeText(context, "Feature coming in next update", Toast.LENGTH_SHORT).show() },
                    Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReferenceMenuTile("Leave Application", Icons.Default.EventNote,
                    { Toast.makeText(context, "Feature coming in next update", Toast.LENGTH_SHORT).show() },
                    Modifier.weight(1f))
                ReferenceMenuTile("Contact School", Icons.Default.ContactMail,
                    onNavigateToContact, Modifier.weight(1f))
                ReferenceMenuTile("Change Password", Icons.Default.Lock,
                    onNavigateToSettings, Modifier.weight(1f))
                ReferenceMenuTile("Logout", Icons.Default.ExitToApp,
                    { showLogout = true }, Modifier.weight(1f))
            }

            Spacer(Modifier.height(90.dp))
        }
    }

    if (showLogout) {
        ConfirmDialog(
            title = "Log out?",
            message = "You'll need to sign in again to use the app.",
            confirmLabel = "Log Out",
            isDestructive = true,
            onConfirm = onLogout,
            onDismiss = { showLogout = false }
        )
    }
}

@Composable
private fun ReferenceStatTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .height(102.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accent)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium)
            Icon(icon, contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(18.dp))
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1)
            Box(
                Modifier
                    .height(16.dp)
                    .width(38.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.20f))
            )
        }
    }
}

@Composable
private fun ReferenceMenuTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .height(88.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MenuTileBg)
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label,
                tint = MenuIconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1C2E),
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            maxLines = 2)
    }
}
