package com.school.manager.util

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.school.manager.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CloudinaryHelper {

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        try {
            MediaManager.init(context, mapOf("cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME))
            initialized = true
        } catch (_: Throwable) {
            // already initialized or missing config
        }
    }

    /** Result callback: success(url) or error(message). */
    fun uploadFile(
        uri: Uri,
        folder: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
        onProgress: (Int) -> Unit = {}
    ) {
        if (BuildConfig.CLOUDINARY_CLOUD_NAME.isBlank() ||
            BuildConfig.CLOUDINARY_UPLOAD_PRESET.isBlank()) {
            onError("Cloudinary not configured. Check gradle.properties.")
            return
        }

        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())

        MediaManager.get().upload(uri)
            .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .option("folder", "school_manager/$folder")
            .option("public_id", "${folder}_$stamp")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    if (totalBytes > 0) onProgress((bytes * 100 / totalBytes).toInt())
                }
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                        ?: resultData["url"] as? String
                    if (url != null) onSuccess(url) else onError("No URL returned")
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Upload failed")
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    onError("Rescheduled: ${error?.description}")
                }
            })
            .dispatch()
    }
}
