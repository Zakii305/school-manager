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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.AmberAccent
import com.school.manager.ui.theme.IndigoDark
import com.school.manager.ui.theme.IndigoLight
import com.school.manager.ui.theme.IndigoPrimary

private val DrawerBg = Color(0xFF1A1145)
private val SectionText = Color(0xFFB39DFF)
private val ItemBg = Color(0xFF231763)
private val ItemSelectedBg = Color(0xFF3A2A8F)
private val ItemText = Color(0xFFE8E1FF)
private val ItemTextDim = Color(0xFFB0A3D9)

data class DrawerEntry(
    val emoji: String,
    val label: String,
    val route: String
)

data class DrawerGroup(
    val title: String,
    val entries: List<DrawerEntry>
)

fun groupsForRole(role: String): List<DrawerGroup> = when (role.lowercase()) {
    "admin" -> listOf(
        DrawerGroup("MAIN", listOf(
            DrawerEntry("🏠", "Dashboard", Routes.ADMIN_DASHBOARD),
            DrawerEntry("👥", "Users", Routes.ADMIN_USERS),
            DrawerEntry("🆕", "Pending Approvals", Routes.ADMIN_PENDING_USERS),
            DrawerEntry("👨‍🏫", "Staff Directory", Routes.STAFF_DIRECTORY)
        )),
        DrawerGroup("ACADEMICS", listOf(
            DrawerEntry("🏫", "Classes", Routes.ADMIN_CLASSES),
            DrawerEntry("📝", "Exams", Routes.ADMIN_EXAMS),
            DrawerEntry("📅", "Timetable", Routes.ADMIN_TIMETABLE),
            DrawerEntry("📊", "Analytics", Routes.ADMIN_ANALYTICS)
        )),
        DrawerGroup("COMMUNICATION", listOf(
            DrawerEntry("📢", "Notices", Routes.NOTICE_BOARD),
            DrawerEntry("🔔", "Notifications", Routes.NOTIFICATIONS),
            DrawerEntry("💰", "Fees", Routes.FEES)
        )),
        DrawerGroup("MORE", listOf(
            DrawerEntry("👤", "Profile", Routes.PROFILE),
            DrawerEntry("💬", "Feedback", Routes.FEEDBACK),
            DrawerEntry("ℹ️", "About School", Routes.ABOUT),
            DrawerEntry("📞", "Contact", Routes.CONTACT),
            DrawerEntry("⚙️", "Settings", Routes.SETTINGS)
        ))
    )
    "teacher" -> listOf(
        DrawerGroup("MAIN", listOf(
            DrawerEntry("🏠", "Dashboard", Routes.TEACHER_DASHBOARD),
            DrawerEntry("✅", "Attendance", Routes.TEACHER_ATTENDANCE),
            DrawerEntry("📚", "Gradebook", Routes.ADMIN_EXAMS),
            DrawerEntry("📝", "Assignments", Routes.TEACHER_ASSIGNMENTS),
            DrawerEntry("📅", "My Timetable", Routes.TEACHER_TIMETABLE)
        )),
        DrawerGroup("COMMUNICATION", listOf(
            DrawerEntry("📢", "Notices", Routes.NOTICE_BOARD),
            DrawerEntry("🔔", "Notifications", Routes.NOTIFICATIONS)
        )),
        DrawerGroup("MORE", listOf(
            DrawerEntry("👤", "Profile", Routes.PROFILE),
            DrawerEntry("💬", "Feedback", Routes.FEEDBACK),
            DrawerEntry("ℹ️", "About School", Routes.ABOUT),
            DrawerEntry("📞", "Contact", Routes.CONTACT),
            DrawerEntry("⚙️", "Settings", Routes.SETTINGS)
        ))
    )
    "student" -> listOf(
        DrawerGroup("MAIN", listOf(
            DrawerEntry("🏠", "Dashboard", Routes.STUDENT_DASHBOARD),
            DrawerEntry("📅", "Timetable", Routes.STUDENT_TIMETABLE),
            DrawerEntry("✅", "Attendance", Routes.STUDENT_ATTENDANCE),
            DrawerEntry("📊", "Results", Routes.STUDENT_RESULTS),
            DrawerEntry("📄", "Report Card", Routes.STUDENT_REPORT_CARD),
            DrawerEntry("📝", "Homework", Routes.STUDENT_ASSIGNMENTS),
            DrawerEntry("📋", "All Entries", Routes.ALL_ENTRIES)
        )),
        DrawerGroup("COMMUNICATION", listOf(
            DrawerEntry("📢", "Notices", Routes.NOTICE_BOARD),
            DrawerEntry("🔔", "Notifications", Routes.NOTIFICATIONS)
        )),
        DrawerGroup("FINANCE", listOf(
            DrawerEntry("💰", "Fees", Routes.FEES)
        )),
        DrawerGroup("MORE", listOf(
            DrawerEntry("👤", "Profile", Routes.PROFILE),
            DrawerEntry("💬", "Feedback", Routes.FEEDBACK),
            DrawerEntry("ℹ️", "About School", Routes.ABOUT),
            DrawerEntry("📞", "Contact", Routes.CONTACT),
            DrawerEntry("⚙️", "Settings", Routes.SETTINGS)
        ))
    )
    "parent" -> listOf(
        DrawerGroup("MAIN", listOf(
            DrawerEntry("🏠", "Dashboard", Routes.PARENT_DASHBOARD),
            DrawerEntry("💬", "Messages", Routes.CHAT_LIST)
        )),
        DrawerGroup("COMMUNICATION", listOf(
            DrawerEntry("📢", "Notices", Routes.NOTICE_BOARD),
            DrawerEntry("🔔", "Notifications", Routes.NOTIFICATIONS)
        )),
        DrawerGroup("FINANCE", listOf(
            DrawerEntry("💰", "Fees", Routes.FEES)
        )),
        DrawerGroup("MORE", listOf(
            DrawerEntry("👤", "Profile", Routes.PROFILE),
            DrawerEntry("💬", "Feedback", Routes.FEEDBACK),
            DrawerEntry("ℹ️", "About School", Routes.ABOUT),
            DrawerEntry("📞", "Contact", Routes.CONTACT),
            DrawerEntry("⚙️", "Settings", Routes.SETTINGS)
        ))
    )
    else -> emptyList()
}

@Composable
fun AppDrawerContentNew(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val user0 by viewModel.user.collectAsState()
    if (user0.role.lowercase() == "teacher") {
        TeacherSidebarContent(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            onLogout = onLogout
        )
        return
    }
    if (user0.role.lowercase() == "parent") {
        ParentSidebarContent(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            onLogout = onLogout
        )
        return
    }
    if (user0.role.lowercase() == "admin") {
        AdminSidebarContent(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            onLogout = onLogout
        )
        return
    }
    if (user0.role.lowercase() == "student") {
        StudentSidebarContent(
            currentRoute = currentRoute,
            onNavigate = onNavigate,
            onLogout = onLogout
        )
        return
    }
    val user by viewModel.user.collectAsState()
    val groups = remember(user.role) { groupsForRole(user.role) }
    val expanded = remember {
        mutableStateMapOf<String, Boolean>().apply {
            listOf("MAIN", "ACADEMICS", "COMMUNICATION", "FINANCE", "MORE").forEach {
                put(it, true)
            }
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = DrawerBg,
        modifier = Modifier.width(310.dp)
    ) {
        Column(Modifier.fillMaxHeight().verticalScroll(rememberScrollState())) {

            // ── HEADER with violet gradient ──
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(IndigoPrimary, IndigoLight))
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.20f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user.name.firstOrNull()?.uppercase() ?: "?",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                user.name.ifBlank { "Loading…" },
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                user.email.ifBlank { "—" },
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Small logo capsule
                        Box(
                            Modifier.size(20.dp).clip(CircleShape).background(AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("S", color = IndigoDark, fontSize = 11.sp,
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "School Manager",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.weight(1f))
                        if (user.role.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = AmberAccent
                            ) {
                                Text(
                                    user.role.replaceFirstChar { it.uppercase() },
                                    Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    color = IndigoDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (groups.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading menu…",
                        color = ItemTextDim,
                        style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                groups.forEach { group ->
                    val isOpen = expanded[group.title] ?: true

                    // Section header
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { expanded[group.title] = !isOpen }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            group.title,
                            color = SectionText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            if (isOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = SectionText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (isOpen) {
                        Column(
                            Modifier.padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            group.entries.forEach { entry ->
                                DrawerMenuItem(
                                    entry = entry,
                                    selected = currentRoute == entry.route ||
                                        (currentRoute.startsWith(entry.route) && entry.route.length > 3),
                                    onClick = { onNavigate(entry.route) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── LOGOUT ──
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF5B1010))
                    .clickable { onLogout() }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF6B6B).copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFFF8A8A),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Logout",
                    color = Color(0xFFFF8A8A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerMenuItem(
    entry: DrawerEntry,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) ItemSelectedBg else ItemBg
    val contentColor = if (selected) Color.White else ItemText

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon in soft rounded box
        Box(
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (selected) AmberAccent.copy(alpha = 0.22f)
                    else Color.White.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(entry.emoji, fontSize = 15.sp)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            entry.label,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1
        )
    }
}
