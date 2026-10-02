package com.school.manager.ui.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Locale

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val LineGrey = Color(0xFFE2E8F0)
private val BluePrimary = Color(0xFF2563EB)
private val GreenUp = Color(0xFF16A34A)
private val RedDown = Color(0xFFDC2626)
private val Amber = Color(0xFFF59E0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onNavigate: (String) -> Unit = {},
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", color = Color.White, fontSize = 17.sp,
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
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).background(PageBg),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ═══ WELCOME ═══
            item {
                Card(shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Welcome back, Admin!",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                        Spacer(Modifier.height(2.dp))
                        Text("Here's what's happening at Auvixededa Group of School.",
                            fontSize = 12.sp, color = TextMuted)
                    }
                }
            }

            // ═══ DASHBOARD FILTERS ═══
            item {
                FiltersCard(s, viewModel)
            }

            // ═══ STAT ROW 1 ═══
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(Icons.Default.Person, BluePrimary, s.totalStudents.toString(),
                        "Total Students", s.studentsChange, true, Modifier.weight(1f))
                    StatCard(Icons.Default.Person, GreenUp, s.totalStaff.toString(),
                        "Total Staff", s.staffChange, true, Modifier.weight(1f))
                    StatCard(Icons.Default.School, Amber, s.totalClasses.toString(),
                        "Total Classes", s.classesChange, true, Modifier.weight(1f))
                }
            }

            // ═══ STAT ROW 2 ═══
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(Icons.Default.Payments, Color(0xFF7C3AED),
                        "Rs ${fmt(s.totalFeesCollected)}", "Total Fees Collected",
                        s.feesChange, false, Modifier.weight(1f))
                    StatCard(Icons.Default.TrendingUp, RedDown,
                        "Rs ${fmt(s.totalExpenses)}", "Total Expenses",
                        s.expensesChange, true, Modifier.weight(1f))
                }
            }

            // ═══ FEE COLLECTION OVERVIEW ═══
            item {
                FeeOverviewCard(s)
            }

            // ═══ ATTENDANCE + STUDENT STATUS ═══
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttendanceCard(s, Modifier.weight(1f))
                    StudentStatusCard(s, Modifier.weight(1f))
                }
            }

            // ═══ RECENT ADMISSIONS + UPCOMING ═══
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecentAdmissionsCard(s, onNavigate, Modifier.weight(1f))
                    UpcomingCard(s, onNavigate, Modifier.weight(1f))
                }
            }

            // ═══ RECENT MESSAGES ═══
            item { RecentMessagesCard(s, onNavigate) }

            // ═══ BOTTOM ROW 1 ═══
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniCard(Icons.Default.CheckCircle, GreenUp, "Today's Attendance",
                        "-", "Not marked yet today", null, null, Modifier.weight(1f))
                    MiniCard(Icons.Default.Payments, BluePrimary, "Today's Fee Collection",
                        "Rs ${fmt(s.todaysFeeCollection)}", "${s.todaysPayments} payments today · vs yesterday",
                        "▲ 100%", 0.02f, Modifier.weight(1f))
                }
            }

            // ═══ BOTTOM ROW 2 ═══
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniCard(Icons.Default.Notifications, RedDown, "Pending Fees (now)",
                        "Rs ${fmt(s.pendingFees)}", "${s.pendingStudents} students still owe fees",
                        "▲ 1%", 0.7f, Modifier.weight(1f),
                        badgeColor = RedDown, progressColor = RedDown)
                    MiniCard(Icons.Default.TrendingUp, Color(0xFF7C3AED), "Today's Expenses",
                        "Rs ${fmt(s.todaysExpenses)}", "0 expenses today · vs yesterday",
                        "0%", 0.02f, Modifier.weight(1f),
                        progressColor = Color(0xFF7C3AED))
                }
            }

            item { Spacer(Modifier.height(30.dp)) }
        }
    }
}

// ══════════════ FILTERS CARD ══════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersCard(s: AdminDashboardState, vm: AdminDashboardViewModel) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(34.dp).background(BluePrimary, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FilterAlt, null, tint = Color.White,
                        modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Dashboard Filters", fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = TextDark)
                    Text("No filters applied — showing the normal overview. Pick filters and press Apply Filters.",
                        fontSize = 11.sp, color = TextMuted)
                }
            }

            // Preset row
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()) {
                PresetBtn("Today", s.preset == DatePreset.TODAY) { vm.setPreset(DatePreset.TODAY) }
                PresetBtn("This Week", s.preset == DatePreset.WEEK) { vm.setPreset(DatePreset.WEEK) }
                PresetBtn("This Month", s.preset == DatePreset.MONTH) { vm.setPreset(DatePreset.MONTH) }
                PresetBtn("This Year", s.preset == DatePreset.YEAR) { vm.setPreset(DatePreset.YEAR) }
                OutlinedButton(
                    onClick = { vm.setPreset(DatePreset.CUSTOM) },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Custom Date", fontSize = 11.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { vm.reset() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reset", fontSize = 12.sp)
                }
                Button(
                    onClick = { vm.apply() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Icon(Icons.Default.FilterAlt, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Apply Filters", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(color = LineGrey)

            // Grid of dropdowns: 2 per row for readability
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterBox(Icons.Default.Business, "Branch",
                    s.branches.firstOrNull { it.first == s.branchFilter }?.second ?: "All Branches",
                    s.branches.map { it.second }, Modifier.weight(1f)) { idx ->
                    vm.setBranch(if (idx == 0) "all" else s.branches[idx].first)
                }
                FilterBox(Icons.Default.School, "Class",
                    s.classes.firstOrNull { it.first == s.classFilter }?.second ?: "All Classes",
                    s.classes.map { it.second }, Modifier.weight(1f)) { idx ->
                    vm.setClass(if (idx == 0) "all" else s.classes[idx].first)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterBox(Icons.Default.BarChart, "Data Type",
                    "All Data", listOf("All Data", "Fees Only", "Attendance Only", "Expenses Only"),
                    Modifier.weight(1f)) { idx ->
                    vm.setDataType(if (idx == 0) "all" else listOf("fees","attendance","expenses")[idx-1])
                }
                FilterBox(Icons.Default.Person, "Student Status",
                    "All Students", listOf("All Students", "Active", "Left", "Suspended"),
                    Modifier.weight(1f)) { idx ->
                    vm.setStudentStatus(if (idx == 0) "all" else listOf("active","left","suspended")[idx-1])
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterBox(Icons.Default.CalendarMonth, "Academic Year",
                    s.academicYear, listOf("2024", "2025", "2026", "2027"),
                    Modifier.weight(1f)) { idx ->
                    vm.setAcademicYear(listOf("2024","2025","2026","2027")[idx])
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PresetBtn(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) BluePrimary else CardWhite,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, LineGrey),
        onClick = onClick
    ) {
        Text(label, fontSize = 11.sp,
            color = if (selected) Color.White else TextDark,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    options: List<String>,
    modifier: Modifier,
    onSelectIndex: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineGrey),
        modifier = modifier
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = BluePrimary, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark)
            }
            Spacer(Modifier.height(6.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    maxLines = 1,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                )
                ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    options.forEachIndexed { idx, opt ->
                        DropdownMenuItem(
                            text = { Text(opt, fontSize = 12.sp) },
                            onClick = { onSelectIndex(idx); expanded = false })
                    }
                }
            }
        }
    }
}

// ══════════════ FEE OVERVIEW ══════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeeOverviewCard(s: AdminDashboardState) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            // Row 1: Icon + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null,
                    tint = BluePrimary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Fee Collection Overview", fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, color = TextDark, maxLines = 1)
            }
            Spacer(Modifier.height(8.dp))

            // Row 2: Chip + Legend + View Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LineGrey),
                    color = CardWhite) {
                    Text("This Year", fontSize = 10.sp, color = TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
                Spacer(Modifier.width(10.dp))
                LegendDot(BluePrimary, "Collected")
                Spacer(Modifier.width(8.dp))
                LegendDot(Amber, "Pending")
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }) {
                    Text("View Details", fontSize = 11.sp, color = BluePrimary,
                        fontWeight = FontWeight.SemiBold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                        tint = BluePrimary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(10.dp))

            // Row 3: Collected / Unpaid / By Month
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Collected in This Year", fontSize = 10.sp, color = TextMuted)
                    Text("Rs ${fmt(s.yearCollected)}",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Column(Modifier.weight(1f)) {
                    Text("New vouchers unpaid", fontSize = 10.sp, color = TextMuted)
                    Text("Rs ${fmt(s.yearPending)}",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                Text("By Month", fontSize = 10.sp, color = TextMuted, maxLines = 1)
            }
            Spacer(Modifier.height(10.dp))
            FeeBarChart(s.monthlyBars, Modifier.fillMaxWidth().height(170.dp))
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun FeeBarChart(bars: List<MonthlyBar>, modifier: Modifier = Modifier) {
    val maxVal = remember(bars) {
        bars.maxOfOrNull { it.collected + it.pending }?.coerceAtLeast(1.0) ?: 1.0
    }
    Column(modifier) {
        // Y-axis labels + bars
        Row(Modifier.fillMaxWidth().weight(1f)) {
            // Y axis
            Column(Modifier.width(30.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween) {
                listOf("400K","320K","240K","160K","80K","0").forEach {
                    Text(it, fontSize = 8.sp, color = TextMuted)
                }
            }
            Row(Modifier.weight(1f).fillMaxHeight(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom) {
                bars.forEach { b ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom) {
                        Box(Modifier.width(16.dp).weight(1f),
                            contentAlignment = Alignment.BottomCenter) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom) {
                                val ph = (b.pending / maxVal * 130).toInt().coerceAtLeast(0)
                                val ch = (b.collected / maxVal * 130).toInt().coerceAtLeast(0)
                                if (ph > 0) Box(Modifier.width(16.dp).height(ph.dp)
                                    .background(Amber))
                                if (ch > 0) Box(Modifier.width(16.dp).height(ch.dp)
                                    .background(BluePrimary))
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth().padding(start = 30.dp),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            bars.forEach { b ->
                Text(b.label, fontSize = 8.sp, color = TextMuted)
            }
        }
    }
}

// ══════════════ ATTENDANCE / STUDENT STATUS ══════════════

@Composable
private fun AttendanceCard(s: AdminDashboardState, modifier: Modifier) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, null, tint = BluePrimary,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Attendance Overview", fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, modifier = Modifier.weight(1f), color = TextDark)
                Surface(shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LineGrey),
                    color = CardWhite) {
                    Text("This Month", fontSize = 9.sp, color = TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val total = s.attendancePresent + s.attendanceAbsent +
                    s.attendanceLeave + s.attendanceLate
                DonutChart(
                    slices = if (total == 0)
                        listOf(Color(0xFFE5E7EB) to 1f)
                    else listOf(
                        GreenUp to s.attendancePresent.toFloat(),
                        RedDown to s.attendanceAbsent.toFloat(),
                        Amber to s.attendanceLeave.toFloat(),
                        BluePrimary to s.attendanceLate.toFloat()
                    ),
                    centerText = if (total == 0) "—" else total.toString(),
                    centerSub = if (total == 0) "No records" else "Total",
                    size = 90.dp
                )
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)) {
                    AttnRow(GreenUp, "Present", s.attendancePresent)
                    AttnRow(RedDown, "Absent", s.attendanceAbsent)
                    AttnRow(Amber, "Leave", s.attendanceLeave)
                    AttnRow(BluePrimary, "Late", s.attendanceLate)
                }
            }
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9)) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null,
                        tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (s.attendancePresent + s.attendanceAbsent + s.attendanceLeave +
                            s.attendanceLate == 0)
                            "No attendance marked in this period. View Attendance"
                        else "Attendance summary for this period.",
                        fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun StudentStatusCard(s: AdminDashboardState, modifier: Modifier) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = BluePrimary,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Student Status", fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, modifier = Modifier.weight(1f), color = TextDark)
                Surface(shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LineGrey),
                    color = CardWhite) {
                    Text("All Students", fontSize = 9.sp, color = TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DonutChart(
                    slices = listOf(
                        BluePrimary to s.statusActive.toFloat(),
                        Amber to s.statusNew.toFloat(),
                        GreenUp to s.statusLeft.toFloat()
                    ),
                    centerText = s.studentStatusTotal.toString(),
                    centerSub = "Students",
                    size = 90.dp
                )
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)) {
                    StatusRow(BluePrimary, "Active", s.statusActive)
                    StatusRow(GreenUp, "New Admissions", s.statusNew)
                    StatusRow(Amber, "Left / Transferred", s.statusLeft)
                }
            }
        }
    }
}

// ══════════════ RECENT ADMISSIONS / UPCOMING ══════════════

@Composable
private fun RecentAdmissionsCard(
    s: AdminDashboardState, onNavigate: (String) -> Unit, modifier: Modifier
) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, null, tint = BluePrimary,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Recent Admissions", fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, modifier = Modifier.weight(1f), color = TextDark)
                TextButton(onClick = { onNavigate("admin_students_list") }) {
                    Text("View All", fontSize = 10.sp, color = BluePrimary)
                }
            }
            s.recentAdmissions.take(4).forEach { r ->
                AdmissionRow(r)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun UpcomingCard(
    s: AdminDashboardState,
    onNavigate: (String) -> Unit,
    modifier: Modifier
) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, null, tint = BluePrimary,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Upcoming Activities", fontWeight = FontWeight.Bold,
                    fontSize = 12.sp, modifier = Modifier.weight(1f), color = TextDark)
                TextButton(onClick = { onNavigate("admin_students_list") }) {
                    Text("View All", fontSize = 10.sp, color = BluePrimary)
                }
            }
            s.upcomingActivities.forEach { a ->
                UpcomingRow(a)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun RecentMessagesCard(s: AdminDashboardState, onNavigate: (String) -> Unit) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, null, tint = BluePrimary,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Recent Messages", fontWeight = FontWeight.Bold,
                    fontSize = 14.sp, modifier = Modifier.weight(1f), color = TextDark)
                TextButton(onClick = {}) {
                    Text("View All", fontSize = 10.sp, color = BluePrimary)
                }
            }
            s.recentMessages.take(5).forEach { m ->
                MessageRow(m)
                HorizontalDivider(color = LineGrey)
            }
        }
    }
}

// ══════════════ SHARED COMPOSABLES ══════════════

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color, value: String, label: String,
    change: Double, up: Boolean, modifier: Modifier
) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier) {
        Column(Modifier.padding(10.dp)) {
            Box(Modifier.size(28.dp).background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
            Text(label, fontSize = 10.sp, color = TextMuted)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (up) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    null, tint = if (up) GreenUp else RedDown, modifier = Modifier.size(10.dp))
                Spacer(Modifier.width(2.dp))
                Text("${change}%", fontSize = 9.sp,
                    color = if (up) GreenUp else RedDown, fontWeight = FontWeight.SemiBold)
            }
            Text("vs last month", fontSize = 8.sp, color = TextMuted)
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<Pair<Color, Float>>,
    centerText: String, centerSub: String,
    size: androidx.compose.ui.unit.Dp
) {
    val total = slices.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(1f)
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 22f
            var start = -90f
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            slices.forEach { (color, value) ->
                val sweep = (value / total) * 360f
                if (sweep > 0.3f) {
                    drawArc(color, start, sweep, false,
                        Offset(inset, inset), arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt))
                }
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerText, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextDark)
            Text(centerSub, fontSize = 9.sp, color = TextMuted)
        }
    }
}

@Composable
private fun AttnRow(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, color = TextDark, modifier = Modifier.weight(1f))
        Text(count.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun StatusRow(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, color = TextDark, modifier = Modifier.weight(1f))
        Text(count.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
    }
}

@Composable
private fun AdmissionRow(r: RecentAdmission) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).background(colorFor(r.name), CircleShape),
            contentAlignment = Alignment.Center) {
            Text(initials(r.name), color = Color.White, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(r.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = TextDark, maxLines = 1)
            Text("${r.classLabel} · ${r.date}", fontSize = 9.sp, color = TextMuted, maxLines = 1)
        }
        Surface(shape = RoundedCornerShape(6.dp),
            color = if (r.status.equals("active", true)) Color(0xFFDCFCE7)
                    else Color(0xFFF1F5F9)) {
            Text(r.status.replaceFirstChar { it.uppercase() },
                fontSize = 8.sp, fontWeight = FontWeight.SemiBold,
                color = if (r.status.equals("active", true)) GreenUp else TextMuted,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun UpcomingRow(a: UpcomingActivity) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(28.dp).background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Event, null, tint = GreenUp,
                modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(a.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = TextDark, maxLines = 1)
            Text(a.subtitle, fontSize = 9.sp, color = TextMuted, maxLines = 1)
        }
        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFDCFCE7)) {
            Text(a.tag, fontSize = 8.sp, fontWeight = FontWeight.SemiBold,
                color = GreenUp,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun MessageRow(m: RecentMessage) {
    Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).background(colorFor(m.name), CircleShape),
            contentAlignment = Alignment.Center) {
            Text(initials(m.name), color = Color.White, fontSize = 11.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(m.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(m.preview, fontSize = 10.sp, color = TextMuted, maxLines = 1)
        }
        Text(m.timeAgo, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun MiniCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color, title: String, value: String, subtitle: String,
    badge: String?, progress: Float?, modifier: Modifier,
    badgeColor: Color = TextMuted,
    progressColor: Color = BluePrimary
) {
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = modifier) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    color = TextDark, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                    tint = TextMuted, modifier = Modifier.size(12.dp))
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                if (badge != null) {
                    Spacer(Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(6.dp),
                        color = if (badge.startsWith("▲")) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)) {
                        Text(badge, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                            color = if (badge.startsWith("▲")) RedDown else GreenUp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 9.sp, color = TextMuted)
            if (progress != null) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = progressColor,
                    trackColor = LineGrey
                )
            }
        }
    }
}

// ══════════════ HELPERS ══════════════

private fun fmt(v: Double): String = String.format(Locale.US, "%,.0f", v)

private fun initials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> (parts[0].first().toString() + parts[1].first().toString()).uppercase()
    }
}

private fun colorFor(name: String): Color {
    val palette = listOf(
        Color(0xFF2563EB), Color(0xFF16A34A), Color(0xFFF59E0B),
        Color(0xFFDC2626), Color(0xFF7C3AED), Color(0xFF0891B2),
        Color(0xFFDB2777), Color(0xFFEA580C)
    )
    return palette[(name.hashCode() and 0x7FFFFFFF) % palette.size]
}
