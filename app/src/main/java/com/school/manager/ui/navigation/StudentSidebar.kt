package com.school.manager.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val SidebarBg = Color(0xFF0B1730)
private val SidebarActive = Color(0xFF1E3A8A)
private val SectionLabel = Color(0xFF64748B)
private val SidebarText = Color(0xFFCBD5E1)
private val SidebarActiveText = Color(0xFFFFFFFF)
private val SidebarIconDim = Color(0xFF94A3B8)
private val UserCapsule = Color(0xFF1E293B)
private val LogoutRed = Color(0xFFEF4444)

private data class StudentNavItem(val label: String, val route: String, val icon: ImageVector)
private data class StudentNavGroup(val title: String, val items: List<StudentNavItem>)

@Composable
fun StudentSidebarContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val branding by viewModel.branding.collectAsState()

    val groups = listOf(
        StudentNavGroup("MAIN", listOf(
            StudentNavItem("Dashboard", Routes.STUDENT_DASHBOARD, Icons.Default.Home),
            StudentNavItem("Timetable", Routes.STUDENT_TIMETABLE, Icons.Default.CalendarMonth),
            StudentNavItem("Attendance", Routes.STUDENT_ATTENDANCE, Icons.Default.CheckCircle),
            StudentNavItem("Results", Routes.STUDENT_RESULTS, Icons.Default.Grade),
            StudentNavItem("Report Card", Routes.STUDENT_REPORT_CARD, Icons.Default.Description),
            StudentNavItem("Homework", Routes.STUDENT_ASSIGNMENTS, Icons.AutoMirrored.Filled.Assignment)
        )),
        StudentNavGroup("COMMUNICATION", listOf(
            StudentNavItem("Notices", Routes.NOTICE_BOARD, Icons.Default.Campaign)
        )),
        StudentNavGroup("ACADEMICS", listOf(
            StudentNavItem("All Entries", Routes.ALL_ENTRIES, Icons.Default.Event)
        )),
        StudentNavGroup("FINANCE", listOf(
            StudentNavItem("Fees", Routes.FEES, Icons.Default.Payments)
        ))
    )

    ModalDrawerSheet(
        drawerContainerColor = SidebarBg,
        modifier = Modifier.width(280.dp)
    ) {
        Column(Modifier.fillMaxHeight()) {

            // ── Header ──
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.School, contentDescription = null,
                            tint = Color(0xFF1E3A8A), modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(branding.name, color = Color.White,
                            fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("STUDENT PORTAL", color = Color(0xFF94A3B8),
                            fontSize = 9.sp, fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Signed-in capsule
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = UserCapsule,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(24.dp).clip(CircleShape)
                                .background(Color(0xFF3B82F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.name.firstOrNull()?.uppercase() ?: "?",
                                color = Color.White, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Signed in as", color = Color(0xFF94A3B8), fontSize = 9.sp)
                            Text(user.name.ifBlank { "Student" },
                                color = Color.White, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }

            // ── Menu groups ──
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
            ) {
                groups.forEach { group ->
                    Spacer(Modifier.height(10.dp))
                    Text(group.title, color = SectionLabel, fontSize = 9.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))

                    group.items.forEach { item ->
                        val selected = currentRoute == item.route ||
                            (currentRoute.startsWith(item.route) && item.route.length > 3)

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) SidebarActive else Color.Transparent)
                                .clickable { onNavigate(item.route) }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(item.icon, contentDescription = item.label,
                                tint = if (selected) SidebarActiveText else SidebarIconDim,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(item.label,
                                color = if (selected) SidebarActiveText else SidebarText,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold
                                else FontWeight.Normal,
                                maxLines = 1)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Logout ──
            Row(
                Modifier.fillMaxWidth().clickable { onLogout() }
                    .padding(horizontal = 26.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout",
                    tint = LogoutRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("Log Out", color = LogoutRed, fontSize = 13.sp,
                    fontWeight = FontWeight.Medium)
            }
        }
    }
}
