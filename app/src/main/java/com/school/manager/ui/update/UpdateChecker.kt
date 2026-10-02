package com.school.manager.ui.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

object UpdateChecker {

    suspend fun check(context: Context): Pair<Boolean, String>? {
        return try {
            val rc = FirebaseRemoteConfig.getInstance()
            rc.setDefaultsAsync(mapOf("min_version_code" to 1L, "update_message" to "")).await()
            rc.fetchAndActivate().await()

            val minVersion = rc.getLong("min_version_code")
            val currentVersion = context.packageManager
                .getPackageInfo(context.packageName, 0).longVersionCode

            if (currentVersion < minVersion) {
                val msg = rc.getString("update_message")
                    .ifBlank { "A newer version is available. Please update to continue." }
                Pair(true, msg)
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    fun openPlayStore(context: Context) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${context.packageName}")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        try {
            context.startActivity(intent)
        } catch (_: Throwable) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
