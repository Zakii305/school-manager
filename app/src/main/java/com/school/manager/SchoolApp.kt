package com.school.manager

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.school.manager.util.CloudinaryHelper
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltAndroidApp
class SchoolApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Crashlytics
        try {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        } catch (_: Throwable) { }

        // Cloudinary — replaces Firebase Storage
        try {
            CloudinaryHelper.init(this)
        } catch (_: Throwable) { }

        installCrashLogger()
    }

    private fun installCrashLogger() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try { FirebaseCrashlytics.getInstance().recordException(throwable) } catch (_: Throwable) { }
            try { writeCrashToDownloads(throwable) } catch (_: Throwable) { }
            previous?.uncaughtException(thread, throwable)
        }
    }

    private fun writeCrashToDownloads(throwable: Throwable) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "SchoolManager_crash_$timestamp.txt"
        val content = buildString {
            append("CRASH REPORT\n")
            append("Time: ").append(Date()).append("\n")
            append("Device: ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL).append("\n")
            append("Android: ").append(Build.VERSION.RELEASE)
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n\n")
            append("────── STACK TRACE ──────\n\n")
            append(throwable.stackTraceToString())
        }
        var written = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { os ->
                        os.write(content.toByteArray()); os.flush()
                    }
                    written = true
                }
            } catch (_: Throwable) { }
        }
        if (!written) {
            try {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "")
                dir.mkdirs()
                File(dir, fileName).writeText(content)
            } catch (_: Throwable) { }
        }
        if (!written) {
            try { File(filesDir, fileName).writeText(content) } catch (_: Throwable) { }
        }
    }
}
