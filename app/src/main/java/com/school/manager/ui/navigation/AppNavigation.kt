package com.school.manager.ui.navigation

import com.school.manager.ui.settings.SettingsTabsScreen
import com.school.manager.ui.notifications.NotificationLogsScreen
import com.school.manager.ui.invoices.InvoiceScreen
import com.school.manager.ui.settings.PaymentMethodsScreen
import com.school.manager.ui.settings.FeeHeadsScreen
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.school.manager.SessionState
import com.school.manager.ui.owner.OwnerDashboard
import com.school.manager.util.OfflineBanner
import com.school.manager.util.UserRoles
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.activity.compose.BackHandler

@Composable
fun AppNavigation(session: SessionState) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var currentRole by remember(session) {
        mutableStateOf(if (session is SessionState.LoggedIn) session.role else "")
    }

    // ---- Global back: always return to role home ----
    val backStackEntry by navController.currentBackStackEntryAsState()
    val backRoute = backStackEntry?.destination?.route
    val homeRoute = if (session is SessionState.LoggedIn) {
        when (session.role) {
            UserRoles.OWNER   -> Routes.OWNER_DASHBOARD
            UserRoles.ADMIN   -> Routes.ADMIN_DASHBOARD
            UserRoles.TEACHER -> Routes.TEACHER_DASHBOARD
            UserRoles.PARENT  -> Routes.PARENT_DASHBOARD
            else              -> Routes.STUDENT_DASHBOARD
        }
    } else Routes.LOGIN

    BackHandler(enabled = backRoute != null && backRoute != homeRoute) {
        navController.navigate(homeRoute) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val navTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(Routes.LOGIN) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (session is SessionState.Loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val drawerController = remember {
        DrawerController(
            open = { scope.launch { drawerState.open() } },
            close = { scope.launch { drawerState.close() } }
        )
    }

    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""

    CompositionLocalProvider(LocalDrawerController provides drawerController) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = session is SessionState.LoggedIn,
            drawerContent = {
                if (session is SessionState.LoggedIn) {
                    AppDrawerContentNew(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            navTo(route)
                        },
                        onLogout = {
                            scope.launch { drawerState.close() }
                            currentRole = ""
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        ) {
            Column(Modifier.fillMaxSize()) {
                OfflineBanner()
                Box(Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = when (session) {
                            is SessionState.LoggedIn -> when (session.role) {
                                UserRoles.OWNER -> Routes.OWNER_DASHBOARD
                                        UserRoles.ADMIN -> Routes.ADMIN_DASHBOARD
                                UserRoles.TEACHER -> Routes.TEACHER_DASHBOARD
                                UserRoles.PARENT -> Routes.PARENT_DASHBOARD
                                else -> Routes.STUDENT_DASHBOARD
                            }
                            else -> Routes.LOGIN
                        },
                        enterTransition = {
                            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(280)) +
                                fadeIn(animationSpec = tween(280))
                        },
                        exitTransition = {
                            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(280)) +
                                fadeOut(animationSpec = tween(280))
                        },
                        popEnterTransition = {
                            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(280)) +
                                fadeIn(animationSpec = tween(280))
                        },
                        popExitTransition = {
                            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(280)) +
                                fadeOut(animationSpec = tween(280))
                        }
                    ) {
                        composable(Routes.LOGIN) {
                            com.school.manager.ui.auth.LoginScreen(
                                onLoginSuccess = { role ->
                                    currentRole = role
                                    val dest = when (role) {
                                        UserRoles.OWNER -> Routes.OWNER_DASHBOARD
                                        UserRoles.ADMIN -> Routes.ADMIN_DASHBOARD
                                        UserRoles.TEACHER -> Routes.TEACHER_DASHBOARD
                                        UserRoles.STUDENT -> Routes.STUDENT_DASHBOARD
                                        UserRoles.PARENT -> Routes.PARENT_DASHBOARD
                                        else -> Routes.STUDENT_DASHBOARD
                                    }
                                    navController.navigate(dest) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                },
                                onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                                onSignup = { navController.navigate(Routes.SIGNUP) }
                            )
                        }
                        composable(Routes.OWNER_DASHBOARD) {
                            OwnerDashboard(
                                onLogout = {
                                    currentRole = ""
                                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                        composable(Routes.OWNER_DASHBOARD) {
                            OwnerDashboard(
                                onLogout = {
                                    currentRole = ""
                                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                        composable(Routes.SIGNUP) {
                            com.school.manager.ui.auth.SignupScreen(
                                onBackToLogin = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.FORGOT_PASSWORD) {
                            com.school.manager.ui.auth.ForgotPasswordScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.PROFILE) {
                            com.school.manager.ui.profile.ProfileScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToSettings = { navTo(Routes.SETTINGS) },
                                onNavigateToNotifications = { navTo(Routes.NOTIFICATIONS) },
                                onNavigateToSearch = { navTo(Routes.GLOBAL_SEARCH) }
                            )
                        }
                        composable(Routes.SETTINGS) {
                            SettingsTabsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateTo = { route -> navController.navigate(route) }
                            )
                        }
                        composable(Routes.NOTIFICATIONS) {
                            com.school.manager.ui.notifications.NotificationsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.GLOBAL_SEARCH) {
                            com.school.manager.ui.search.GlobalSearchScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ABOUT) {
                            com.school.manager.ui.info.AboutScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.CONTACT) {
                            com.school.manager.ui.info.ContactScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.FEEDBACK) {
                            com.school.manager.ui.info.FeedbackScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ================ ADMIN ================
                        composable(Routes.ADMIN_DASHBOARD) {
                            com.school.manager.ui.admin.AdminDashboard(
                                onNavigate = { route -> navTo(route) }
                            )
                        }

                        composable(Routes.ADMIN_USERS) {
                            com.school.manager.ui.admin.AdminUserListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddUser = { navController.navigate(Routes.userForm("new")) },
                                onEditUser = { uid -> navController.navigate(Routes.userForm(uid)) }
                            )
                        }
                        
                        // ───── FAMILIES ─────
                        composable(Routes.ADMIN_FAMILIES_LIST) {
                            com.school.manager.ui.admin.FamiliesListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddFamily = { navController.navigate("admin_family_form/new") },
                                onOpenFamily = { id -> navController.navigate("admin_family_form/$id") }
                            )
                        }
                        composable(
                            Routes.ADMIN_FAMILY_FORM,
                            arguments = listOf(navArgument("familyId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.admin.FamilyFormScreen(
                                familyId = back.arguments?.getString("familyId") ?: "new",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ───── STAFF ─────
                        composable(Routes.ADMIN_STAFF_LIST) {
                            com.school.manager.ui.admin.StaffListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddStaff = { navController.navigate("admin_staff_form/new") },
                                onEditStaff = { id -> navController.navigate("admin_staff_form/$id") }
                            )
                        }
                        composable(
                            Routes.ADMIN_STAFF_FORM,
                            arguments = listOf(navArgument("staffId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.admin.StaffFormScreen(
                                staffId = back.arguments?.getString("staffId") ?: "new",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ───── EXISTING ─────
                        composable(Routes.ADMIN_USER_FORM,
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.admin.AdminUserFormScreen(
                                userId = back.arguments?.getString("userId") ?: "new",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_PENDING_USERS) {
                            com.school.manager.ui.admin.PendingUsersScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_STUDENTS_LIST) {
                            com.school.manager.ui.admin.StudentsListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddStudent = { navController.navigate("admin_user_form/new") },
                                onEditStudent = { id -> navController.navigate("admin_user_form/$id") },
                                onViewStudent = { id -> navController.navigate("admin_user_form/$id") }
                            )
                        }
                        composable(Routes.ADMIN_FAMILIES) {
                            com.school.manager.ui.admin.FamiliesListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddFamily = { navController.navigate("admin_family_form/new") },
                                onOpenFamily = { id -> navController.navigate("admin_family_form/$id") }
                            )
                        }
                        composable(Routes.STAFF_DIRECTORY) {
                            com.school.manager.ui.staff.StaffDirectoryScreen(
                                onBack = { navController.popBackStack() },
                                onSearch = { navTo(Routes.GLOBAL_SEARCH) },
                                onNotifications = { navTo(Routes.NOTIFICATIONS) }
                            )
                        }
                        composable(Routes.ADMIN_CLASSES) {
                            com.school.manager.ui.admin.ClassesListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddClass = { navController.navigate(Routes.classForm("new")) },
                                onEditClass = { cid -> navController.navigate(Routes.classForm(cid)) }
                            )
                        }
                        composable(Routes.ADMIN_CLASS_FORM,
                            arguments = listOf(navArgument("classId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.classes.ClassFormScreen(
                                classId = back.arguments?.getString("classId") ?: "new",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_CLASS_DETAIL,
                            arguments = listOf(navArgument("classId") { type = NavType.StringType })
                        ) { back ->
                            val cid = back.arguments?.getString("classId") ?: ""
                            com.school.manager.ui.classes.ClassDetailScreen(
                                classId = cid,
                                onNavigateBack = { navController.popBackStack() },
                                onEditClass = { navController.navigate(Routes.classForm(cid)) },
                                onAddSubject = { navController.navigate(Routes.subjectForm(cid, "new")) },
                                onEditSubject = { sid -> navController.navigate(Routes.subjectForm(cid, sid)) }
                            )
                        }
                        composable(Routes.ADMIN_SUBJECT_FORM,
                            arguments = listOf(
                                navArgument("classId") { type = NavType.StringType },
                                navArgument("subjectId") { type = NavType.StringType }
                            )
                        ) { back ->
                            com.school.manager.ui.classes.SubjectFormScreen(
                                classId = back.arguments?.getString("classId") ?: "",
                                subjectId = back.arguments?.getString("subjectId") ?: "new",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_SUBJECTS) {
                            com.school.manager.ui.admin.SubjectsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_SYLLABUS) {
                            com.school.manager.ui.academic.SyllabusScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_EXAMS) {
                            com.school.manager.ui.admin.ExamsListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddExam = { navController.navigate(Routes.examForm("new")) },
                                onEditExam = { id -> navController.navigate(Routes.examForm(id)) }
                            )
                        }
                        composable(Routes.ADMIN_EXAM_FORM,
                            arguments = listOf(navArgument("examId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.exams.ExamFormScreen(
                                examId = back.arguments?.getString("examId") ?: "new",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_EXAM_MARKS) {
                            com.school.manager.ui.academic.ExamScheduleScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_QUESTION_PAPERS) {
                            com.school.manager.ui.admin.QuestionPapersScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_GRADE_SETTINGS) {
                            com.school.manager.ui.admin.GradeSettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_TIMETABLE) {
                            com.school.manager.ui.timetable.AdminTimetableListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenClass = { cid -> navController.navigate(Routes.adminTimetableClass(cid)) }
                            )
                        }
                        composable(Routes.ADMIN_TIMETABLE_CLASS,
                            arguments = listOf(navArgument("classId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.timetable.TimetableGridScreen(
                                classId = back.arguments?.getString("classId") ?: "",
                                isAdmin = true,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_TIMETABLE_BULK,
                            arguments = listOf(navArgument("classId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.timetable.BulkTimetableScreen(
                                classId = back.arguments?.getString("classId") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_ANALYTICS) {
                            com.school.manager.ui.analytics.AnalyticsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_ATTENDANCE) {
                            com.school.manager.ui.admin.AdminAttendanceScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_STAFF_ATTENDANCE) {
                            com.school.manager.ui.admin.AdminStaffAttendanceScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_SCAN_ATTENDANCE) {
                            com.school.manager.ui.finance.ScanAttendanceScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_FEES) {
                            com.school.manager.ui.admin.AdminFeesScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_FEE_COLLECT) {
                            com.school.manager.ui.finance.FeeCollectionFullScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_FAMILY_FEE_COLLECTION) {
                            com.school.manager.ui.admin.FamilyFeeScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_RECEIPTS) {
                            com.school.manager.ui.finance.PaymentReceiptsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_PAYROLL) {
                            com.school.manager.ui.finance.PayrollFullScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_EXPENSES) {
                            com.school.manager.ui.finance.ExpensesFullScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_LIBRARY) {
                            com.school.manager.ui.finance.LibraryScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_TRANSPORT) {
                            com.school.manager.ui.finance.TransportScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_RESULT_CARDS) {
                            com.school.manager.ui.finance.ResultCardsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_CERTIFICATES) {
                            com.school.manager.ui.finance.CertificatesScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_SCHOOL_ASSETS) {
                            com.school.manager.ui.academic.SchoolAssetsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_NOTIFICATIONS_SMTP) {
                            com.school.manager.ui.finance.SmtpConfigScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_LEAVE_REQUESTS) {
                            com.school.manager.ui.admin.AdminLeaveScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_PARENT_ACCOUNTS) {
                            com.school.manager.ui.admissions.ParentAccountsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_REPORTS) {
                            com.school.manager.ui.finance.ReportsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_ROLES) {
                            com.school.manager.ui.finance.RolesScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_USERS_ACCESS) {
                            com.school.manager.ui.finance.UsersAccessScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_BRANCH) {
                            com.school.manager.ui.finance.UsersAccessScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_MODULES) {
                            com.school.manager.ui.finance.UsersAccessScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_AUDIT_LOG) {
                            com.school.manager.ui.finance.AuditLogScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_SYSTEM_UPDATES) {
                            com.school.manager.ui.finance.SystemScreen(
                                onNavigateBack = { navController.popBackStack() },
                                startTab = 2
                            )
                        }
                        composable(Routes.ADMIN_BACKUPS) {
                            com.school.manager.ui.finance.SystemScreen(
                                onNavigateBack = { navController.popBackStack() },
                                startTab = 0
                            )
                        }
                        composable(Routes.ADMIN_SUBSCRIPTION) {
                            com.school.manager.ui.finance.SystemScreen(
                                onNavigateBack = { navController.popBackStack() },
                                startTab = 1
                            )
                        }
                        composable(Routes.ADMIN_SOFTWARE_UPDATES) {
                            com.school.manager.ui.finance.SystemScreen(
                                onNavigateBack = { navController.popBackStack() },
                                startTab = 2
                            )
                        }
                        composable(Routes.ADMIN_NOTIFICATION_LOGS) {
                            NotificationLogsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable(Routes.ADMIN_INVOICES) {
                            InvoiceScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable(Routes.ADMIN_FEE_HEADS) {
                            FeeHeadsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable(Routes.ADMIN_PAYMENT_METHODS) {
                            PaymentMethodsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable(Routes.ADMIN_BRANDING) {
                            com.school.manager.ui.settings.SchoolBrandingScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_ADMISSIONS) {
                            com.school.manager.ui.admin.AdmissionsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ================ TEACHER ================
                        composable(Routes.TEACHER_DASHBOARD) {
                            com.school.manager.ui.teacher.TeacherDashboard(
                                onNavigateToAttendance = { navTo(Routes.TEACHER_ATTENDANCE) },
                                onNavigateToExams = { navTo(Routes.ADMIN_EXAMS) },
                                onNavigateToAssignments = { navTo(Routes.TEACHER_ASSIGNMENTS) },
                                onNavigateToTimetable = { navTo(Routes.TEACHER_TIMETABLE) },
                                onNavigateToSearch = { navTo(Routes.GLOBAL_SEARCH) },
                                onNavigateToNotifications = { navTo(Routes.NOTIFICATIONS) },
                                onLogout = {
                                    currentRole = ""
                                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                        composable(Routes.TEACHER_ATTENDANCE) {
                            com.school.manager.ui.teacher.TeacherAttendanceScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.TEACHER_MY_CLASSES) {
                            com.school.manager.ui.teacher.MyClassesScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.TEACHER_TIMETABLE) {
                            com.school.manager.ui.timetable.TeacherTimetableScreen(
                                onBack = { navController.popBackStack() },
                                onSearch = { navTo(Routes.GLOBAL_SEARCH) },
                                onNotifications = { navTo(Routes.NOTIFICATIONS) }
                            )
                        }
                        composable(Routes.TEACHER_FEES) {
                            com.school.manager.ui.teacher.TeacherFeesScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.TEACHER_HOMEWORK) {
                            com.school.manager.ui.teacher.HomeworkScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.TEACHER_LEAVE) {
                            com.school.manager.ui.teacher.LeaveRequestScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.TEACHER_NOTICES) {
                            com.school.manager.ui.notice.NoticeScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.TEACHER_MESSAGES) {
                            com.school.manager.ui.chat.ChatListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenChat = { cid -> navController.navigate(Routes.chatRoom(cid)) }
                            )
                        }
                        composable(Routes.TEACHER_GRADEBOOK,
                            arguments = listOf(navArgument("examId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.gradebook.GradebookScreen(
                                examId = back.arguments?.getString("examId") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.TEACHER_ASSIGNMENTS) {
                            com.school.manager.ui.assignments.AssignmentListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddAssignment = { navController.navigate(Routes.assignmentForm("new")) },
                                onEditAssignment = { id -> navController.navigate(Routes.assignmentForm(id)) },
                                onSubmitAssignment = { id ->
                                    navController.navigate("${Routes.STUDENT_ASSIGNMENTS}/submit/$id")
                                }
                            )
                        }
                        composable(Routes.TEACHER_ASSIGNMENT_FORM,
                            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.assignments.AssignmentFormScreen(
                                assignmentId = back.arguments?.getString("assignmentId") ?: "new",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.SUBMISSION_VIEWER,
                            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.submissions.SubmissionViewerScreen(
                                assignmentId = back.arguments?.getString("assignmentId") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ================ STUDENT ================
                        composable(Routes.STUDENT_DASHBOARD) {
                            com.school.manager.ui.student.StudentDashboard(
                                onNavigateToResults = { navTo(Routes.STUDENT_RESULTS) },
                                onNavigateToTimetable = { navTo(Routes.STUDENT_TIMETABLE) },
                                onNavigateToAssignments = { navTo(Routes.STUDENT_ASSIGNMENTS) },
                                onNavigateToAttendance = { navTo(Routes.STUDENT_ATTENDANCE) },
                                onNavigateToReportCard = { navTo(Routes.STUDENT_REPORT_CARD) },
                                onNavigateToAllEntries = { navTo(Routes.ALL_ENTRIES) },
                                onNavigateToSearch = { navTo(Routes.GLOBAL_SEARCH) },
                                onNavigateToNotifications = { navTo(Routes.NOTIFICATIONS) },
                                onLogout = {
                                    currentRole = ""
                                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                        composable(Routes.STUDENT_RESULTS) {
                            com.school.manager.ui.student.StudentResultsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.STUDENT_ATTENDANCE) {
                            com.school.manager.ui.student.StudentAttendanceScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.STUDENT_TIMETABLE) {
                            com.school.manager.ui.timetable.TimetableGridScreen(
                                classId = "",
                                isAdmin = false,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.STUDENT_REPORT_CARD) {
                            com.school.manager.ui.report.ReportCardScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.STUDENT_ASSIGNMENTS) {
                            com.school.manager.ui.assignments.AssignmentListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onAddAssignment = {},
                                onEditAssignment = {},
                                onSubmitAssignment = { id ->
                                    navController.navigate("${Routes.STUDENT_ASSIGNMENTS}/submit/$id")
                                }
                            )
                        }
                        composable("${Routes.STUDENT_ASSIGNMENTS}/submit/{assignmentId}",
                            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.assignments.StudentSubmissionScreen(
                                assignmentId = back.arguments?.getString("assignmentId") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ALL_ENTRIES) {
                            com.school.manager.ui.timetable.AllEntriesScreen(
                                onBack = { navController.popBackStack() },
                                onSearch = { navTo(Routes.GLOBAL_SEARCH) },
                                onNotifications = { navTo(Routes.NOTIFICATIONS) }
                            )
                        }
                        composable(Routes.HALL_TICKET,
                            arguments = listOf(navArgument("examId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.exams.HallTicketScreen(
                                examId = back.arguments?.getString("examId") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ================ PARENT ================
                        composable(Routes.PARENT_DASHBOARD) {
                            com.school.manager.ui.parent.ParentDashboard(
                                onNavigateToChat = { navTo(Routes.CHAT_LIST) },
                                onNavigateToSearch = { navTo(Routes.GLOBAL_SEARCH) },
                                onNavigateToNotifications = { navTo(Routes.NOTIFICATIONS) },
                                onLogout = {
                                    currentRole = ""
                                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                        composable(Routes.PARENT_CHILDREN) {
                            com.school.manager.ui.parent.MyChildrenScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.PARENT_MESSAGES) {
                            com.school.manager.ui.chat.ChatListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenChat = { cid -> navController.navigate(Routes.chatRoom(cid)) }
                            )
                        }
                        composable(Routes.CHAT_LIST) {
                            com.school.manager.ui.chat.ChatListScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onOpenChat = { cid -> navController.navigate(Routes.chatRoom(cid)) }
                            )
                        }
                        composable(Routes.CHAT_ROOM,
                            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.chat.ChatRoomScreen(
                                chatId = back.arguments?.getString("chatId") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ================ SHARED ================
                        composable(Routes.NOTICE_BOARD) {
                            com.school.manager.ui.notice.NoticeScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.FEES) {
                            com.school.manager.ui.fees.FeesScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onPayClick = { feeId, _, _ ->
                                    navController.navigate(Routes.feePayment(feeId))
                                }
                            )
                        }
                        composable(Routes.FEE_PAYMENT,
                            arguments = listOf(navArgument("feeId") { type = NavType.StringType })
                        ) { back ->
                            com.school.manager.ui.payment.FeePaymentScreen(
                                feeId = back.arguments?.getString("feeId") ?: "",
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.ADMIN_FEES_VOUCHERS) {
                            com.school.manager.ui.admin.AdminFeesScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
