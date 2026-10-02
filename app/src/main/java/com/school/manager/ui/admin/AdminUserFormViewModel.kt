package com.school.manager.ui.admin

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
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

data class StudentFormState(
    val id: String = "",
    val isEditMode: Boolean = false,

    val admissionNo: String = "",
    val rollNo: String = "",
    val sessionLabel: String = "2026-2027",
    val classId: String = "",
    val name: String = "",
    val nameUrdu: String = "",
    val cnicBform: String = "",
    val religion: String = "",
    val bloodGroup: String = "",
    val dob: Long? = null,
    val admissionDate: Long? = null,
    val previousSchool: String = "",
    val nationality: String = "Pakistani",
    val gender: String = "",
    val hafizEQuran: String = "No",
    val address: String = "",
    val phone: String = "",
    val email: String = "",

    val fatherName: String = "",
    val motherName: String = "",
    val fatherContact: String = "",
    val motherContact: String = "",
    val fatherOccupation: String = "",
    val motherOccupation: String = "",
    val guardianName: String = "",
    val guardianRelation: String = "",
    val guardianContact: String = "",
    val guardianOccupation: String = "",

    val monthlyFee: String = "",
    val vanFee: String = "",
    val admissionFee: String = "",
    val examFee: String = "",
    val remarks: String = "",
    val familyId: String = "",

    // Photo + documents
    val photoUrl: String? = null,
    val photoLocalUri: Uri? = null,
    val photoUploading: Boolean = false,
    val documents: Map<String, String> = emptyMap(),
    val uploadingDocKey: String? = null,

    val classes: List<Pair<String, String>> = emptyList(),
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminUserFormViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(StudentFormState())
    val state: StateFlow<StudentFormState> = _state.asStateFlow()

    init { loadClasses() }

    private fun loadClasses() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val list = snap.documents.mapNotNull { d ->
                    val nm = d.getString("name") ?: return@mapNotNull null
                    val sec = d.getString("section") ?: ""
                    d.id to if (sec.isBlank()) nm else "$nm-$sec"
                }.sortedBy { it.second }
                _state.value = _state.value.copy(classes = list)
            } catch (_: Exception) {}
        }
    }

    fun loadUser(userId: String) {
        if (userId == "new" || userId.isBlank()) return
        viewModelScope.launch {
            try {
                val doc = firestore.collection(FirestoreCollections.STUDENTS)
                    .document(userId).get().await()
                if (!doc.exists()) return@launch
                fun s(k: String) = doc.getString(k) ?: ""
                _state.value = _state.value.copy(
                    id = userId, isEditMode = true,
                    admissionNo = s("admission_no"),
                    rollNo = s("roll_no"),
                    sessionLabel = s("session_label").ifBlank { "2026-2027" },
                    classId = s("class_id"),
                    name = s("name"),
                    nameUrdu = s("name_urdu"),
                    cnicBform = s("cnic_bform"),
                    religion = s("religion"),
                    bloodGroup = s("blood_group"),
                    previousSchool = s("previous_school"),
                    nationality = s("nationality").ifBlank { "Pakistani" },
                    gender = s("gender"),
                    hafizEQuran = s("hafiz_e_quran").ifBlank { "No" },
                    address = s("address"),
                    phone = s("phone"),
                    email = s("email"),
                    fatherName = s("father_name"),
                    motherName = s("mother_name"),
                    fatherContact = s("father_contact"),
                    motherContact = s("mother_contact"),
                    fatherOccupation = s("father_occupation"),
                    motherOccupation = s("mother_occupation"),
                    guardianName = s("guardian_name"),
                    guardianRelation = s("guardian_relation"),
                    guardianContact = s("guardian_contact"),
                    guardianOccupation = s("guardian_occupation"),
                    monthlyFee = s("monthly_fee_override"),
                    vanFee = s("van_fee"),
                    admissionFee = s("admission_fee"),
                    examFee = s("exam_fee"),
                    remarks = s("remarks"),
                    photoUrl = s("photo_path").ifBlank { null }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    // --- setters ---
    fun onAdmissionNo(v: String) = _state.value.let { _state.value = it.copy(admissionNo = v) }
    fun onRollNo(v: String) = _state.value.let { _state.value = it.copy(rollNo = v) }
    fun onSession(v: String) = _state.value.let { _state.value = it.copy(sessionLabel = v) }
    fun onClass(v: String) = _state.value.let { _state.value = it.copy(classId = v) }
    fun onName(v: String) = _state.value.let { _state.value = it.copy(name = v) }
    fun onNameUrdu(v: String) = _state.value.let { _state.value = it.copy(nameUrdu = v) }
    fun onCnic(v: String) = _state.value.let { _state.value = it.copy(cnicBform = v) }
    fun onReligion(v: String) = _state.value.let { _state.value = it.copy(religion = v) }
    fun onBloodGroup(v: String) = _state.value.let { _state.value = it.copy(bloodGroup = v) }
    fun onDob(v: Long?) = _state.value.let { _state.value = it.copy(dob = v) }
    fun onAdmissionDate(v: Long?) = _state.value.let { _state.value = it.copy(admissionDate = v) }
    fun onPreviousSchool(v: String) = _state.value.let { _state.value = it.copy(previousSchool = v) }
    fun onNationality(v: String) = _state.value.let { _state.value = it.copy(nationality = v) }
    fun onGender(v: String) = _state.value.let { _state.value = it.copy(gender = v) }
    fun onHafiz(v: String) = _state.value.let { _state.value = it.copy(hafizEQuran = v) }
    fun onAddress(v: String) = _state.value.let { _state.value = it.copy(address = v) }
    fun onPhone(v: String) = _state.value.let { _state.value = it.copy(phone = v) }
    fun onEmail(v: String) = _state.value.let { _state.value = it.copy(email = v) }
    fun onFatherName(v: String) = _state.value.let { _state.value = it.copy(fatherName = v) }
    fun onMotherName(v: String) = _state.value.let { _state.value = it.copy(motherName = v) }
    fun onFatherContact(v: String) = _state.value.let { _state.value = it.copy(fatherContact = v) }
    fun onMotherContact(v: String) = _state.value.let { _state.value = it.copy(motherContact = v) }
    fun onFatherOccupation(v: String) = _state.value.let { _state.value = it.copy(fatherOccupation = v) }
    fun onMotherOccupation(v: String) = _state.value.let { _state.value = it.copy(motherOccupation = v) }
    fun onGuardianName(v: String) = _state.value.let { _state.value = it.copy(guardianName = v) }
    fun onGuardianRelation(v: String) = _state.value.let { _state.value = it.copy(guardianRelation = v) }
    fun onGuardianContact(v: String) = _state.value.let { _state.value = it.copy(guardianContact = v) }
    fun onGuardianOccupation(v: String) = _state.value.let { _state.value = it.copy(guardianOccupation = v) }
    fun onMonthlyFee(v: String) = _state.value.let { _state.value = it.copy(monthlyFee = v) }
    fun onVanFee(v: String) = _state.value.let { _state.value = it.copy(vanFee = v) }
    fun onAdmissionFee(v: String) = _state.value.let { _state.value = it.copy(admissionFee = v) }
    fun onExamFee(v: String) = _state.value.let { _state.value = it.copy(examFee = v) }
    fun onRemarks(v: String) = _state.value.let { _state.value = it.copy(remarks = v) }
    fun onFamily(v: String) = _state.value.let { _state.value = it.copy(familyId = v) }

    // --- Photo / documents ---
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

    fun pickDocument(key: String, uri: Uri, resolver: ContentResolver) {
        _state.value = _state.value.copy(uploadingDocKey = key, error = null)
        viewModelScope.launch {
            try {
                val bytes = withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { it.readBytes() }
                } ?: throw Exception("Cannot read file")
                val mime = resolver.getType(uri) ?: "application/octet-stream"
                val url = withContext(Dispatchers.IO) { uploadToCloudinary(bytes, mime) }
                val docs = _state.value.documents.toMutableMap().apply { put(key, url) }
                _state.value = _state.value.copy(documents = docs, uploadingDocKey = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    uploadingDocKey = null,
                    error = "Document upload failed: ${e.message}"
                )
            }
        }
    }

    private fun uploadToCloudinary(bytes: ByteArray, mime: String): String {
        val boundary = "----AndroidBoundary${System.currentTimeMillis()}"
        val url = URL("https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD/auto/upload")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 30_000
            readTimeout = 60_000
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        DataOutputStream(conn.outputStream).use { out ->
            fun writeText(s: String) = out.write(s.toByteArray(Charsets.UTF_8))

            writeText("--$boundary\r\n")
            writeText("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n")
            writeText("$CLOUDINARY_PRESET\r\n")

            val ext = when {
                mime.contains("png") -> "png"
                mime.contains("pdf") -> "pdf"
                mime.contains("jpeg") || mime.contains("jpg") -> "jpg"
                else -> "bin"
            }
            writeText("--$boundary\r\n")
            writeText("Content-Disposition: form-data; name=\"file\"; filename=\"upload.$ext\"\r\n")
            writeText("Content-Type: $mime\r\n\r\n")
            out.write(bytes)
            writeText("\r\n--$boundary--\r\n")
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw Exception("Cloudinary $code: ${body.take(200)}")

        val m = Regex("\"secure_url\"\\s*:\\s*\"([^\"]+)\"").find(body)
            ?: throw Exception("No secure_url in response")
        return m.groupValues[1]
    }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.value = s.copy(error = "Student name is required"); return
        }
        _state.value = s.copy(isSaving = true, error = null)

        val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val isoDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val now = isoDateTime.format(Date())

        val data = mutableMapOf<String, Any?>(
            "name" to s.name.trim(),
            "name_urdu" to s.nameUrdu.ifBlank { null },
            "admission_no" to s.admissionNo.trim(),
            "roll_no" to s.rollNo.trim(),
            "session_label" to s.sessionLabel.trim(),
            "class_id" to s.classId.trim(),
            "gender" to s.gender.ifBlank { null },
            "dob" to (s.dob?.let { isoFmt.format(Date(it)) }),
            "admission_date" to (s.admissionDate?.let { isoFmt.format(Date(it)) } ?: isoFmt.format(Date())),
            "cnic_bform" to s.cnicBform.ifBlank { null },
            "religion" to s.religion.ifBlank { null },
            "blood_group" to s.bloodGroup.ifBlank { null },
            "previous_school" to s.previousSchool.ifBlank { null },
            "nationality" to s.nationality.ifBlank { null },
            "hafiz_e_quran" to s.hafizEQuran,
            "address" to s.address.ifBlank { null },
            "phone" to s.phone.ifBlank { null },
            "email" to s.email.ifBlank { null },
            "father_name" to s.fatherName.ifBlank { null },
            "mother_name" to s.motherName.ifBlank { null },
            "father_contact" to s.fatherContact.ifBlank { null },
            "mother_contact" to s.motherContact.ifBlank { null },
            "father_occupation" to s.fatherOccupation.ifBlank { null },
            "mother_occupation" to s.motherOccupation.ifBlank { null },
            "guardian_name" to s.guardianName.ifBlank { null },
            "guardian_relation" to s.guardianRelation.ifBlank { null },
            "guardian_contact" to s.guardianContact.ifBlank { null },
            "guardian_occupation" to s.guardianOccupation.ifBlank { null },
            "monthly_fee_override" to s.monthlyFee.toDoubleOrNull(),
            "van_fee" to s.vanFee.toDoubleOrNull(),
            "admission_fee" to s.admissionFee.toDoubleOrNull(),
            "exam_fee" to s.examFee.toDoubleOrNull(),
            "remarks" to s.remarks.ifBlank { null },
            "family_id" to s.familyId.ifBlank { null },
            "photo_path" to s.photoUrl,
            "documents" to if (s.documents.isEmpty()) null else s.documents,
            "status" to "active",
            "updated_at" to now
        )

        viewModelScope.launch {
            try {
                val coll = firestore.collection(FirestoreCollections.STUDENTS)
                if (s.isEditMode && s.id.isNotBlank()) {
                    coll.document(s.id).update(data).await()
                } else {
                    data["created_at"] = now
                    coll.add(data).await()
                }
                _state.value = _state.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
