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
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
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

private data class AdminNavItem(val label: String, val route: String, val icon: ImageVector)
private data class AdminNavGroup(val title: String, val items: List<AdminNavItem>)

@Composable
fun AdminSidebarContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val branding by viewModel.branding.collectAsState()

    val groups = listOf(
        AdminNavGroup("OVERVIEW", listOf(
            AdminNavItem("Dashboard", Routes.ADMIN_DASHBOARD, Icons.Default.Home)
        )),
        AdminNavGroup("ACADEMICS", listOf(
            AdminNavItem("Students", Routes.ADMIN_STUDENTS_LIST, Icons.Default.Person),
            AdminNavItem("Families", Routes.ADMIN_FAMILIES_LIST, Icons.Default.FamilyRestroom),
            AdminNavItem("Teachers & Staff", Routes.ADMIN_STAFF_LIST, Icons.Default.Groups),
            AdminNavItem("Classes & Sections", Routes.ADMIN_CLASSES, Icons.Default.Class),
            AdminNavItem("Attendance", Routes.ADMIN_ATTENDANCE, Icons.Default.Event),
            AdminNavItem("Staff Attendance", Routes.ADMIN_STAFF_ATTENDANCE, Icons.Default.Event),
            AdminNavItem("Scan Attendance", Routes.ADMIN_SCAN_ATTENDANCE, Icons.Default.Event),
            AdminNavItem("Exams", Routes.ADMIN_EXAMS, Icons.Default.Event),
            AdminNavItem("Exam Schedule & Marks", Routes.ADMIN_EXAM_MARKS, Icons.Default.Event),
            AdminNavItem("Question Papers", Routes.ADMIN_QUESTION_PAPERS, Icons.Default.Class),
            AdminNavItem("Grade Settings", Routes.ADMIN_GRADE_SETTINGS, Icons.Default.Assessment),
            AdminNavItem("Timetable", Routes.ADMIN_TIMETABLE, Icons.Default.CalendarMonth),
            AdminNavItem("Subjects", Routes.ADMIN_SUBJECTS, Icons.Default.Class),
            AdminNavItem("Syllabus", Routes.ADMIN_SYLLABUS, Icons.Default.Class),
            AdminNavItem("Admission Enquiries", Routes.ADMIN_ADMISSIONS, Icons.Default.Person)
        )),
        AdminNavGroup("FINANCE", listOf(
            AdminNavItem("Fee Collection", Routes.ADMIN_FEE_COLLECT, Icons.Default.Payments),
            AdminNavItem("Family Fee Collection", Routes.ADMIN_FAMILY_FEE_COLLECTION, Icons.Default.Payments),
            AdminNavItem("Payment Receipts", Routes.ADMIN_RECEIPTS, Icons.Default.Payments),
            AdminNavItem("Payroll", Routes.ADMIN_PAYROLL, Icons.Default.Payments),
            AdminNavItem("Expenses", Routes.ADMIN_EXPENSES, Icons.Default.Payments)
        )),
        AdminNavGroup("FACILITIES", listOf(
            AdminNavItem("Library", Routes.ADMIN_LIBRARY, Icons.Default.Class),
            AdminNavItem("Transport", Routes.ADMIN_TRANSPORT, Icons.Default.Class)
        )),
        AdminNavGroup("SYSTEM", listOf(
            AdminNavItem("Result Cards", Routes.ADMIN_RESULT_CARDS, Icons.Default.Assessment),
            AdminNavItem("Student ID Cards", Routes.ADMIN_CERTIFICATES, Icons.Default.Class),
            AdminNavItem("School Assets", Routes.ADMIN_SCHOOL_ASSETS, Icons.Default.Class),
            AdminNavItem("Notifications / SMTP", Routes.ADMIN_NOTIFICATIONS_SMTP, Icons.Default.Campaign),
            AdminNavItem("Announcements", Routes.NOTICE_BOARD, Icons.Default.Campaign),
            AdminNavItem("Messages", Routes.CHAT_LIST, Icons.Default.Campaign),
            AdminNavItem("Notice Board", Routes.NOTICE_BOARD, Icons.Default.Campaign),
            AdminNavItem("Leave Requests", Routes.ADMIN_LEAVE_REQUESTS, Icons.Default.Event),
            AdminNavItem("Parent Accounts", Routes.ADMIN_PARENT_ACCOUNTS, Icons.Default.Person),
            AdminNavItem("Reports", Routes.ADMIN_REPORTS, Icons.Default.Assessment),
            AdminNavItem("Roles & Permissions", Routes.ADMIN_ROLES, Icons.Default.Person),
            AdminNavItem("Users & Access", Routes.ADMIN_USERS_ACCESS, Icons.Default.Person),
            AdminNavItem("Branch Management", Routes.ADMIN_BRANCH, Icons.Default.School),
            AdminNavItem("Module Management", Routes.ADMIN_MODULES, Icons.Default.Settings),
            AdminNavItem("Audit Log", Routes.ADMIN_AUDIT_LOG, Icons.Default.Assessment),
            AdminNavItem("System Updates", Routes.ADMIN_SYSTEM_UPDATES, Icons.Default.Settings),
            AdminNavItem("Backups", Routes.ADMIN_BACKUPS, Icons.Default.Settings),
            AdminNavItem("Settings", Routes.SETTINGS, Icons.Default.Settings),
            AdminNavItem("Subscription", Routes.ADMIN_SUBSCRIPTION, Icons.Default.Settings),
            AdminNavItem("Software Updates", Routes.ADMIN_SOFTWARE_UPDATES, Icons.Default.Settings)
        ))
    )

    ModalDrawerSheet(
        drawerContainerColor = SidebarBg,
        modifier = Modifier.width(280.dp)
    ) {
        Column(Modifier.fillMaxHeight()) {
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
                        Text("SCHOOL MANAGEMENT", color = Color(0xFF94A3B8),
                            fontSize = 9.sp, fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
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
                            Text(user.name.ifBlank { "Admin" },
                                color = Color.White, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }
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
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(item.icon, contentDescription = item.label,
                                tint = if (selected) SidebarActiveText else SidebarIconDim,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(item.label,
                                color = if (selected) SidebarActiveText else SidebarText,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold
                                else FontWeight.Normal,
                                maxLines = 1)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
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
