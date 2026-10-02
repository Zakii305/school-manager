package com.school.manager.ui.admin

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val CLOUDINARY_CLOUD = "z06q7obd"
private const val CLOUDINARY_PRESET = "school_manager_unsigned"

data class StaffFormState(
    val id: String = "",
    val isEdit: Boolean = false,

    val staffId: String = "",
    val fullName: String = "",
    val designation: String = "",
    val department: String = "",
    val phone: String = "",
    val email: String = "",
    val cnic: String = "",
    val monthlySalary: String = "",
    val hireDate: Long? = null,
    val status: String = "active",

    val photoUrl: String? = null,
    val photoLocalUri: Uri? = null,
    val photoUploading: Boolean = false,

    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StaffFormViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(StaffFormState())
    val state: StateFlow<StaffFormState> = _state.asStateFlow()

    fun load(id: String) {
        if (id == "new" || id.isBlank()) return
        viewModelScope.launch {
            try {
                val d = firestore.collection("staff").document(id).get().await()
                if (!d.exists()) return@launch
                fun g(k: String) = d.getString(k) ?: ""
                _state.value = StaffFormState(
                    id = id, isEdit = true,
                    staffId = g("staff_id"),
                    fullName = g("name").ifBlank { g("full_name") },
                    designation = g("designation"),
                    department = g("department"),
                    phone = g("phone"),
                    email = g("email"),
                    cnic = g("cnic"),
                    monthlySalary = d.getDouble("monthly_salary")?.toInt()?.toString() ?: "",
                    status = g("status").ifBlank { "active" },
                    photoUrl = g("photo_path").ifBlank { null }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun onStaffId(v: String) = _state.value.let { _state.value = it.copy(staffId = v) }
    fun onFullName(v: String) = _state.value.let { _state.value = it.copy(fullName = v) }
    fun onDesignation(v: String) = _state.value.let { _state.value = it.copy(designation = v) }
    fun onDepartment(v: String) = _state.value.let { _state.value = it.copy(department = v) }
    fun onPhone(v: String) = _state.value.let { _state.value = it.copy(phone = v) }
    fun onEmail(v: String) = _state.value.let { _state.value = it.copy(email = v) }
    fun onCnic(v: String) = _state.value.let { _state.value = it.copy(cnic = v) }
    fun onSalary(v: String) = _state.value.let { _state.value = it.copy(monthlySalary = v) }
    fun onHireDate(v: Long?) = _state.value.let { _state.value = it.copy(hireDate = v) }
    fun onStatus(v: String) = _state.value.let { _state.value = it.copy(status = v) }

    fun pickPhoto(uri: Uri, resolver: ContentResolver) {
        _state.value = _state.value.copy(photoLocalUri = uri, photoUploading = true, error = null)
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw Exception("Cannot read image")
                val mime = resolver.getType(uri) ?: "image/jpeg"
                val url = withContext(Dispatchers.IO) { uploadToCloudinary(bytes, mime) }
                _state.value = _state.value.copy(photoUrl = url, photoUploading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    photoUploading = false,
                    error = "Photo upload failed: ${e.message}"
                )
            }
        }
    }

    private fun uploadToCloudinary(bytes: ByteArray, mime: String): String {
        val boundary = "----AndroidBoundary${System.currentTimeMillis()}"
        val url = URL("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD/auto/upload")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"; doOutput = true
            connectTimeout = 30_000; readTimeout = 60_000
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }
        DataOutputStream(conn.outputStream).use { out ->
            fun w(s: String) = out.write(s.toByteArray(Charsets.UTF_8))
            w("--$boundary\r\n")
            w("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
            w("$CLOUDINARY_PRESET\r\n")
            val ext = when {
                mime.contains("png") -> "png"
                mime.contains("pdf") -> "pdf"
                mime.contains("jpeg") || mime.contains("jpg") -> "jpg"
                else -> "bin"
            }
            w("--$boundary\r\n")
            w("Content-Disposition: form-data; name=\"file\"; filename=\"upload.$ext\"\r\n")
            w("Content-Type: $mime\r\n\r\n")
            out.write(bytes)
            w("\r\n--$boundary--\r\n")
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw Exception("Cloudinary $code: ${body.take(200)}")
        val m = Regex("\"secure_url\"\\s*:\\s*\"([^\"]+)\"").find(body)
            ?: throw Exception("No secure_url")
        return m.groupValues[1]
    }

    fun save() {
        val s = _state.value
        if (s.staffId.isBlank()) {
            _state.value = s.copy(error = "Staff ID is required"); return
        }
        if (s.fullName.isBlank()) {
            _state.value = s.copy(error = "Full name is required"); return
        }
        if (s.monthlySalary.isBlank()) {
            _state.value = s.copy(error = "Monthly salary is required"); return
        }
        _state.value = s.copy(isSaving = true, error = null)

        val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val isoDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val now = isoDateTime.format(Date())

        val data = mutableMapOf<String, Any?>(
            "staff_id" to s.staffId.trim(),
            "name" to s.fullName.trim(),
            "full_name" to s.fullName.trim(),
            "designation" to s.designation.trim(),
            "department" to s.department.trim(),
            "phone" to s.phone.trim(),
            "email" to s.email.trim(),
            "cnic" to s.cnic.trim(),
            "monthly_salary" to s.monthlySalary.toDoubleOrNull(),
            "hire_date" to (s.hireDate?.let { isoDate.format(Date(it)) }),
            "status" to s.status,
            "photo_path" to s.photoUrl,
            "updated_at" to now
        )

        viewModelScope.launch {
            try {
                val coll = firestore.collection("staff")
                if (s.isEdit && s.id.isNotBlank()) {
                    coll.document(s.id).update(data).await()
                } else {
                    coll.add(data + ("created_at" to now)).await()
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false, error = e.message ?: "Save failed")
            }
        }
    }
}
