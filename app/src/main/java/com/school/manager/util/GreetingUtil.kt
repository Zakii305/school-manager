package com.school.manager.util

import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object GreetingUtil {

    fun greeting(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }
    }

    fun emoji(): String {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "☀️"
            in 12..16 -> "🌤"
            in 17..20 -> "🌇"
            else -> "🌙"
        }
    }

    fun hijriDate(): String = try {
        val hd = HijrahDate.now()
        val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.US)
        "${hd.format(fmt)} AH"
    } catch (_: Throwable) {
        ""
    }

    fun gregorianDate(): String = try {
        val now = java.time.LocalDate.now()
        val fmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US)
        now.format(fmt)
    } catch (_: Throwable) { "" }
}
