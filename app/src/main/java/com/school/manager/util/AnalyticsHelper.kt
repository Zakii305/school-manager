package com.school.manager.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    private val analytics: FirebaseAnalytics
) {

    fun logLogin(role: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "email")
            putString("role", role)
        })
        setUserRole(role)
    }

    fun setUserRole(role: String) {
        analytics.setUserProperty("role", role)
    }

    fun setUserId(uid: String?) {
        analytics.setUserId(uid)
    }

    fun logNoticeCreated(title: String) {
        analytics.logEvent("notice_created", Bundle().apply {
            putString("title", title.take(50))
        })
    }

    fun logAttendanceSaved(count: Int) {
        analytics.logEvent("attendance_saved", Bundle().apply {
            putInt("student_count", count)
        })
    }

    fun logFeePaid(amount: Double) {
        analytics.logEvent("fee_paid", Bundle().apply {
            putDouble("amount", amount)
        })
    }

    fun logScreenView(name: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
        })
    }

    fun logAssignmentSubmitted(assignmentId: String) {
        analytics.logEvent("assignment_submitted", Bundle().apply {
            putString("assignment_id", assignmentId)
        })
    }

    fun logReportCardGenerated() {
        analytics.logEvent("report_card_generated", Bundle.EMPTY)
    }
}
