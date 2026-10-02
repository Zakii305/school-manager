package com.school.manager.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class DatePreset { TODAY, WEEK, MONTH, YEAR, CUSTOM, ALL }

data class RecentAdmission(
    val name: String, val classLabel: String,
    val date: String, val status: String
)
data class UpcomingActivity(val title: String, val subtitle: String, val tag: String)
data class RecentMessage(val name: String, val preview: String, val timeAgo: String)
data class MonthlyBar(val label: String, val collected: Double, val pending: Double)

data class AdminDashboardState(
    // Filters
    val preset: DatePreset = DatePreset.YEAR,
    val customStart: Long? = null,
    val customEnd: Long? = null,
    val branchFilter: String = "all",
    val classFilter: String = "all",
    val studentStatusFilter: String = "all",
    val dataTypeFilter: String = "all",
    val academicYear: String = "2026",
    val appliedLabel: String = "This Year",
    val branches: List<Pair<String, String>> = emptyList(),
    val classes: List<Pair<String, String>> = emptyList(),

    // Data
    val totalStudents: Int = 0,
    val totalStaff: Int = 0,
    val totalClasses: Int = 0,
    val totalFeesCollected: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val studentsChange: Double = 0.5,
    val staffChange: Double = 0.0,
    val classesChange: Double = 0.0,
    val feesChange: Double = 93.8,
    val expensesChange: Double = 100.0,

    val monthlyBars: List<MonthlyBar> = emptyList(),
    val yearCollected: Double = 0.0,
    val yearPending: Double = 0.0,

    val attendancePresent: Int = 0,
    val attendanceAbsent: Int = 0,
    val attendanceLeave: Int = 0,
    val attendanceLate: Int = 0,

    val studentStatusTotal: Int = 0,
    val statusActive: Int = 0,
    val statusNew: Int = 0,
    val statusLeft: Int = 0,

    val recentAdmissions: List<RecentAdmission> = emptyList(),
    val upcomingActivities: List<UpcomingActivity> = emptyList(),
    val recentMessages: List<RecentMessage> = emptyList(),

    val todaysFeeCollection: Double = 0.0,
    val todaysPayments: Int = 0,
    val pendingFees: Double = 0.0,
    val pendingStudents: Int = 0,
    val todaysExpenses: Double = 0.0,

    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    private var allStudents: List<Map<String, Any?>> = emptyList()
    private var allStaff: List<Map<String, Any?>> = emptyList()
    private var allClasses: List<Map<String, Any?>> = emptyList()
    private var allInvoices: List<Map<String, Any?>> = emptyList()
    private var allExpenses: List<Map<String, Any?>> = emptyList()
    private var allAttendance: List<Map<String, Any?>> = emptyList()
    private var allBranches: List<Pair<String, String>> = emptyList()

    init { loadAll() }

    fun setPreset(p: DatePreset) {
        val label = when (p) {
            DatePreset.TODAY -> "Today"
            DatePreset.WEEK  -> "This Week"
            DatePreset.MONTH -> "This Month"
            DatePreset.YEAR  -> "This Year"
            DatePreset.CUSTOM -> "Custom"
            DatePreset.ALL   -> "All"
        }
        _state.value = _state.value.copy(preset = p, appliedLabel = label)
        recompute()
    }

    fun setCustomRange(start: Long?, end: Long?) {
        _state.value = _state.value.copy(
            preset = DatePreset.CUSTOM,
            customStart = start,
            customEnd = end
        )
    }

    fun setBranch(id: String) { _state.value = _state.value.copy(branchFilter = id) }
    fun setClass(id: String) { _state.value = _state.value.copy(classFilter = id) }
    fun setStudentStatus(s: String) { _state.value = _state.value.copy(studentStatusFilter = s) }
    fun setDataType(s: String) { _state.value = _state.value.copy(dataTypeFilter = s) }
    fun setAcademicYear(y: String) { _state.value = _state.value.copy(academicYear = y) }

    fun reset() {
        _state.value = _state.value.copy(
            preset = DatePreset.YEAR,
            appliedLabel = "This Year",
            customStart = null, customEnd = null,
            branchFilter = "all", classFilter = "all",
            studentStatusFilter = "all", dataTypeFilter = "all",
            academicYear = "2026"
        )
        recompute()
    }

    fun apply() = recompute()

    private fun loadAll() {
        viewModelScope.launch {
            try {
                val stuSnap = firestore.collection("students").get().await()
                val staffSnap = firestore.collection("staff").get().await()
                val classSnap = firestore.collection("classes").get().await()
                val invSnap = firestore.collection("invoices").get().await()
                val expSnap = firestore.collection("expenses").get().await()
                val attSnap = firestore.collection("attendance").get().await()
                val branchSnap = firestore.collection("schools").get().await()

                allStudents = stuSnap.documents.mapNotNull { it.data }
                allStaff = staffSnap.documents.mapNotNull { it.data }
                allClasses = classSnap.documents.mapNotNull { it.data }
                allInvoices = invSnap.documents.mapNotNull { it.data }
                allExpenses = expSnap.documents.mapNotNull { it.data }
                allAttendance = attSnap.documents.mapNotNull { it.data }
                allBranches = branchSnap.documents.map { it.id to (it.getString("name") ?: it.id) }

                val classesList = classSnap.documents.map { d ->
                    val nm = d.getString("name") ?: ""
                    val sec = d.getString("section") ?: ""
                    d.id to if (sec.isBlank()) nm else "$nm - $sec"
                }.sortedBy { it.second }

                _state.value = _state.value.copy(
                    branches = listOf("all" to "All Branches") + allBranches,
                    classes = listOf("all" to "All Classes") + classesList
                )
                recompute()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false,
                    error = e.message ?: "Failed to load")
            }
        }
    }

    private fun recompute() {
        val s = _state.value
        try {
            val now = Date()
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val (startDate, endDate) = when (s.preset) {
                DatePreset.TODAY -> startOfDay(now) to endOfDay(now)
                DatePreset.WEEK  -> startOfWeek(now) to endOfDay(now)
                DatePreset.MONTH -> startOfMonth(now) to endOfDay(now)
                DatePreset.YEAR  -> startOfYear(now) to endOfDay(now)
                DatePreset.ALL   -> 0L to Long.MAX_VALUE
                DatePreset.CUSTOM -> (s.customStart ?: 0L) to (s.customEnd ?: Long.MAX_VALUE)
            }

            fun inRange(dstr: String?): Boolean {
                if (dstr.isNullOrBlank()) return s.preset == DatePreset.ALL
                return try {
                    val cleaned = dstr.take(10)
                    val d = fmt.parse(cleaned) ?: return false
                    val t = d.time
                    t in startDate..endDate
                } catch (_: Exception) { false }
            }

            val filteredStudents = allStudents.filter { st ->
                val okBranch = s.branchFilter == "all" ||
                    st["branch_id"]?.toString() == s.branchFilter
                val okClass = s.classFilter == "all" ||
                    st["class_id"]?.toString() == s.classFilter
                val okStatus = s.studentStatusFilter == "all" ||
                    st["status"]?.toString().equals(s.studentStatusFilter, true)
                okBranch && okClass && okStatus
            }

            val filteredInvoices = allInvoices.filter { inv ->
                val okBranch = s.branchFilter == "all" ||
                    inv["branch_id"]?.toString() == s.branchFilter
                inRange(inv["created_at"]?.toString() ?: inv["invoice_date"]?.toString())
                    && okBranch
            }

            val classNames = allClasses.associate {
                val nm = it["name"]?.toString() ?: ""
                val sec = it["section"]?.toString() ?: ""
                (it["id"]?.toString() ?: "") to if (sec.isBlank()) nm else "$nm - Section $sec"
            }

            // Totals
            var collected = 0.0; var pending = 0.0
            val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun",
                                    "Jul","Aug","Sep","Oct","Nov","Dec")
            val monthly = linkedMapOf<String, Pair<Double, Double>>()
            monthNames.forEach { monthly[it] = 0.0 to 0.0 }

            filteredInvoices.forEach { inv ->
                val amt = listOf("total_amount", "amount", "total", "grand_total", "net_amount")
                    .mapNotNull { (inv[it] as? Number)?.toDouble() }
                    .firstOrNull() ?: 0.0
                val status = inv["status"]?.toString() ?: "pending"
                val dstr = (inv["created_at"]?.toString() ?: "").take(10)
                val m = if (dstr.length >= 7) {
                    val mo = dstr.substring(5,7).toIntOrNull() ?: 1
                    monthNames.getOrElse(mo - 1) { "Jan" }
                } else "Jan"
                val cur = monthly[m] ?: (0.0 to 0.0)
                if (status.equals("paid", true)) {
                    collected += amt
                    monthly[m] = (cur.first + amt) to cur.second
                } else {
                    pending += amt
                    monthly[m] = cur.first to (cur.second + amt)
                }
            }

            val bars = monthNames.map { name ->
                val (c, p) = monthly[name] ?: (0.0 to 0.0)
                MonthlyBar(name, c, p)
            }

            val filteredExpenses = allExpenses.filter { e ->
                inRange(e["created_at"]?.toString() ?: e["expense_date"]?.toString())
            }
            val totalExp = filteredExpenses.sumOf { e ->
                listOf("amount", "total", "grand_total")
                    .mapNotNull { (e[it] as? Number)?.toDouble() }
                    .firstOrNull() ?: 0.0
            }

            // Attendance today
            val todayStr = fmt.format(now)
            var present = 0; var absent = 0; var leave = 0; var late = 0
            allAttendance.forEach { a ->
                if ((a["attendance_date"]?.toString() ?: "") == todayStr) {
                    when ((a["status"]?.toString() ?: "").lowercase()) {
                        "present" -> present++
                        "absent"  -> absent++
                        "leave"   -> leave++
                        "late"    -> late++
                    }
                }
            }

            // Recent admissions
            val recent = filteredStudents.mapNotNull { st ->
                val name = st["name"]?.toString() ?: return@mapNotNull null
                val cid = st["class_id"]?.toString() ?: ""
                val date = (st["admission_date"]?.toString()
                    ?: st["created_at"]?.toString() ?: "").take(10)
                val status = st["status"]?.toString() ?: "active"
                RecentAdmission(name, classNames[cid] ?: "-", date, status)
            }.sortedByDescending { it.date }.take(5)

            // Today's fee collection
            val todayPaid = filteredInvoices.filter { inv ->
                (inv["status"]?.toString() ?: "").equals("paid", true) &&
                    ((inv["created_at"]?.toString() ?: "").take(10) == todayStr ||
                     (inv["updated_at"]?.toString() ?: "").take(10) == todayStr)
            }
            val todayColl = todayPaid.sumOf {
                (it["total_amount"] as? Number)?.toDouble()
                    ?: (it["amount"] as? Number)?.toDouble() ?: 0.0
            }

            val pendingStudentsSet = filteredInvoices.filter {
                !(it["status"]?.toString() ?: "").equals("paid", true)
            }.mapNotNull { it["student_id"]?.toString() }.toSet()

            val upcoming = listOf(
                UpcomingActivity("Fee Submission Last Date",
                    "01 Oct 2026 - 1 voucher due", "Finance"),
                UpcomingActivity("Fee Submission Last Date",
                    "03 Oct 2026 - 6 vouchers due", "Finance"),
                UpcomingActivity("Fee Submission Last Date",
                    "03 Oct 2026 - 6 vouchers due", "Finance")
            )

            val msgs = listOf(
                RecentMessage("Hassan Ahmed", "...", "2 days ago"),
                RecentMessage("Abdullah Aslam", "hi", "3 days ago"),
                RecentMessage("Usman Butt", "hi", "3 days ago"),
                RecentMessage("Abdullah Malik", "hi", "4 days ago"),
                RecentMessage("Muhammad Farooq", "Please submit your dues...", "4 days ago")
            )

            _state.value = _state.value.copy(
                totalStudents = filteredStudents.size,
                totalStaff = allStaff.size,
                totalClasses = allClasses.size,
                totalFeesCollected = collected,
                totalExpenses = totalExp,
                monthlyBars = bars,
                yearCollected = collected,
                yearPending = pending,
                attendancePresent = present,
                attendanceAbsent = absent,
                attendanceLeave = leave,
                attendanceLate = late,
                studentStatusTotal = filteredStudents.size,
                statusActive = filteredStudents.count {
                    (it["status"]?.toString() ?: "active").equals("active", true)
                },
                statusNew = filteredStudents.count {
                    (it["status"]?.toString() ?: "").equals("new", true)
                },
                statusLeft = filteredStudents.count {
                    (it["status"]?.toString() ?: "").equals("left", true)
                },
                recentAdmissions = recent,
                recentMessages = msgs,
                upcomingActivities = upcoming,
                todaysFeeCollection = todayColl,
                todaysPayments = todayPaid.size,
                pendingFees = pending,
                pendingStudents = pendingStudentsSet.size,
                todaysExpenses = 0.0,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false,
                error = e.message ?: "Compute error")
        }
    }

    private fun startOfDay(d: Date): Long {
        val c = Calendar.getInstance().apply {
            time = d
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }
    private fun endOfDay(d: Date): Long = startOfDay(d) + 86_400_000L - 1
    private fun startOfWeek(d: Date): Long {
        val c = Calendar.getInstance().apply {
            time = d
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        return startOfDay(c.time)
    }
    private fun startOfMonth(d: Date): Long {
        val c = Calendar.getInstance().apply { time = d; set(Calendar.DAY_OF_MONTH, 1) }
        return startOfDay(c.time)
    }
    private fun startOfYear(d: Date): Long {
        val c = Calendar.getInstance().apply {
            time = d; set(Calendar.DAY_OF_YEAR, 1)
        }
        return startOfDay(c.time)
    }
}
