package com.school.manager.ui.admin

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.ErrorRed
import com.school.manager.ui.theme.IndigoPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextMuted = Color(0xFF64748B)
private val TextDark = Color(0xFF0F172A)
private val LineGrey = Color(0xFFE2E8F0)
private val FieldBg = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserFormScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminUserFormViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val context = LocalContext.current

    var classExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var religionExpanded by remember { mutableStateOf(false) }
    var bloodExpanded by remember { mutableStateOf(false) }
    var hafizExpanded by remember { mutableStateOf(false) }
    var familyExpanded by remember { mutableStateOf(false) }
    var showDobPicker by remember { mutableStateOf(false) }
    var showAdmissionPicker by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.pickPhoto(it, context.contentResolver) } }

    var currentDocKey by remember { mutableStateOf<String?>(null) }
    val docPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let {
        currentDocKey?.let { k -> viewModel.pickDocument(k, it, context.contentResolver) }
    } }

    LaunchedEffect(userId) { viewModel.loadUser(userId) }
    LaunchedEffect(s.saved) { if (s.saved) onNavigateBack() }

    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.isEditMode) "Edit Admission" else "Admission Form",
                    fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(PageBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ════════════ STUDENT INFORMATION ════════════
            SectionCard("Student Information", "(According to Official Document)") {
                // Photo row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF1F6))
                            .border(1.dp, LineGrey, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val bmp = remember(s.photoLocalUri) {
                            s.photoLocalUri?.let { uri ->
                                try {
                                    context.contentResolver.openInputStream(uri)?.use {
                                        BitmapFactory.decodeStream(it)
                                    }
                                } catch (_: Exception) { null }
                            }
                        }
                        if (bmp != null) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, null, tint = TextMuted,
                                modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Student Photo", fontSize = 12.sp,
                            fontWeight = FontWeight.Medium, color = TextDark)
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { photoPicker.launch("image/*") },
                            enabled = !s.photoUploading,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) { Text("Choose File", fontSize = 11.sp) }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            when {
                                s.photoUploading -> "Uploading…"
                                s.photoUrl != null -> "Uploaded ✓"
                                else -> "No file chosen"
                            },
                            fontSize = 11.sp,
                            color = if (s.photoUrl != null) Color(0xFF16A34A) else TextMuted
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Used on the ID card.", fontSize = 10.sp, color = TextMuted)
                        if (s.photoUploading) {
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(Modifier.fillMaxWidth().height(3.dp))
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Admission No + hint
                LabeledField("Admission No *", s.admissionNo, viewModel::onAdmissionNo)
                Hint("Auto-suggested from the last admission — edit if you need a different number.")

                LabeledField("Roll No", s.rollNo, viewModel::onRollNo)
                LabeledField("Session", s.sessionLabel, viewModel::onSession)

                LabeledDropdown(
                    label = "Class *",
                    selected = s.classes.firstOrNull { it.first == s.classId }?.second ?: "",
                    placeholder = "— Select —",
                    expanded = classExpanded,
                    options = s.classes.map { it.second },
                    onExpand = { classExpanded = it },
                    onSelect = { label ->
                        val id = s.classes.firstOrNull { it.second == label }?.first ?: ""
                        viewModel.onClass(id)
                    }
                )

                LabeledField("Name (In English) *", s.name, viewModel::onName)
                LabeledField("Name (In Urdu)", s.nameUrdu, viewModel::onNameUrdu)
                LabeledField("Student CNIC / B-Form No", s.cnicBform, viewModel::onCnic,
                    hint = "00000-0000000-0")

                LabeledDropdown(
                    label = "Religion",
                    selected = s.religion,
                    placeholder = "— Not Set —",
                    expanded = religionExpanded,
                    options = listOf("Islam", "Christianity", "Hinduism", "Other"),
                    onExpand = { religionExpanded = it },
                    onSelect = viewModel::onReligion
                )

                LabeledDropdown(
                    label = "Blood Group",
                    selected = s.bloodGroup,
                    placeholder = "— Not Set —",
                    expanded = bloodExpanded,
                    options = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"),
                    onExpand = { bloodExpanded = it },
                    onSelect = viewModel::onBloodGroup
                )

                LabeledDateField(
                    label = "Date of Birth",
                    value = s.dob?.let { fmt.format(Date(it)) } ?: "",
                    onPick = { showDobPicker = true }
                )
                LabeledDateField(
                    label = "Date of Admission",
                    value = s.admissionDate?.let { fmt.format(Date(it)) } ?: "",
                    onPick = { showAdmissionPicker = true }
                )

                LabeledField("Previous School (if any)", s.previousSchool, viewModel::onPreviousSchool)
                LabeledField("Nationality", s.nationality, viewModel::onNationality)

                LabeledDropdown(
                    label = "Gender",
                    selected = s.gender,
                    placeholder = "— Select —",
                    expanded = genderExpanded,
                    options = listOf("male", "female", "other"),
                    onExpand = { genderExpanded = it },
                    onSelect = viewModel::onGender
                )

                LabeledDropdown(
                    label = "Hafiz-e-Quran",
                    selected = s.hafizEQuran,
                    placeholder = "— Select —",
                    expanded = hafizExpanded,
                    options = listOf("Yes", "No"),
                    onExpand = { hafizExpanded = it },
                    onSelect = viewModel::onHafiz
                )

                LabeledField("Home Address", s.address, viewModel::onAddress, lines = 3)
                LabeledField("Contact Number", s.phone, viewModel::onPhone)
                LabeledField("Email", s.email, viewModel::onEmail)
            }

            // ════════════ PARENTS / GUARDIAN INFORMATION ════════════
            SectionCard("Parents / Guardian Information") {
                TwoCol {
                    LabeledField("Father Name", s.fatherName, viewModel::onFatherName, Modifier.weight(1f))
                    LabeledField("Mother Name", s.motherName, viewModel::onMotherName, Modifier.weight(1f))
                }
                TwoCol {
                    LabeledField("Father Contact No", s.fatherContact, viewModel::onFatherContact, Modifier.weight(1f))
                    LabeledField("Mother Contact No", s.motherContact, viewModel::onMotherContact, Modifier.weight(1f))
                }
                TwoCol {
                    LabeledField("Father Occupation", s.fatherOccupation, viewModel::onFatherOccupation, Modifier.weight(1f))
                    LabeledField("Mother Occupation", s.motherOccupation, viewModel::onMotherOccupation, Modifier.weight(1f))
                }
            }

            // ════════════ FAMILY (SIBLINGS) ════════════
            SectionCard("Family (Siblings)") {
                Hint("Link this student to a family so a \"Family Report\" (all siblings + combined fee picture) can be pulled up in one click from Families. Leave unset if this is their only child at the school.")
                LabeledDropdown(
                    label = "Family",
                    selected = "",
                    placeholder = "— Not part of a linked family —",
                    expanded = familyExpanded,
                    options = listOf("— Not part of a linked family —"),
                    onExpand = { familyExpanded = it },
                    onSelect = { viewModel.onFamily("") }
                )
                Hint("Don't see the right family? Add one (opens in a new tab), then refresh this dropdown.")
            }

            // ════════════ GUARDIAN (IF DIFFERENT) ════════════
            SectionCard("Guardian (If Different)") {
                TwoCol {
                    LabeledField("Guardian Name", s.guardianName, viewModel::onGuardianName, Modifier.weight(1f))
                    LabeledField("Relation", s.guardianRelation, viewModel::onGuardianRelation, Modifier.weight(1f))
                }
                TwoCol {
                    LabeledField("Contact No", s.guardianContact, viewModel::onGuardianContact, Modifier.weight(1f))
                    LabeledField("Occupation", s.guardianOccupation, viewModel::onGuardianOccupation, Modifier.weight(1f))
                }
            }

            // ════════════ DOCUMENTS REQUIRED ════════════
            SectionCard("Documents Required") {
                DocUploadRow("Passport Size Photograph", "photograph", s,
                    { currentDocKey = it; docPicker.launch("image/*") })
                DocUploadRow("Birth Certificate / Form-B", "birth_certificate", s,
                    { currentDocKey = it; docPicker.launch("*/*") })
                DocUploadRow("CNIC Copy (Father/Mother/Guardian)", "cnic_copy", s,
                    { currentDocKey = it; docPicker.launch("*/*") })
                DocUploadRow("School Leaving Certificate", "leaving_certificate", s,
                    { currentDocKey = it; docPicker.launch("*/*") })
                DocUploadRow("Previous Academic Records", "previous_records", s,
                    { currentDocKey = it; docPicker.launch("*/*") })
            }

            // ════════════ FEE ════════════
            SectionCard("Fee") {
                LabeledField("Monthly Fee (auto-filled from class Fee Structure — edit to give this student a different amount)",
                    s.monthlyFee, viewModel::onMonthlyFee, hint = "0")
                LabeledField("Van Fee (leave at 0 if this student doesn't use the van — added to every voucher automatically otherwise)",
                    s.vanFee, viewModel::onVanFee, hint = "0")
                LabeledField("Admission Fee (one-time, invoiced immediately on save)",
                    s.admissionFee, viewModel::onAdmissionFee, hint = "0")
                LabeledField("Exam / Admission Test Fee (one-time, invoiced immediately on save)",
                    s.examFee, viewModel::onExamFee, hint = "0")
                LabeledField("Remarks (optional — a quick note that shows on the student's profile, e.g. \"bright and hardworking\")",
                    s.remarks, viewModel::onRemarks, lines = 3)
            }

            // ════════════ SAVE / CANCEL ════════════
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { viewModel.save() },
                    enabled = !s.isSaving && !s.photoUploading,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    if (s.isSaving) CircularProgressIndicator(Modifier.size(20.dp),
                        color = Color.White, strokeWidth = 2.dp)
                    else Text(if (s.isEditMode) "Save Changes" else "Save Admission",
                        fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Cancel") }
            }

            s.error?.let { Text(it, color = ErrorRed, fontSize = 12.sp) }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showDobPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDobPicker = false },
            confirmButton = { TextButton(onClick = {
                pickerState.selectedDateMillis?.let { viewModel.onDob(it) }
                showDobPicker = false
            }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDobPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }

    if (showAdmissionPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showAdmissionPicker = false },
            confirmButton = { TextButton(onClick = {
                pickerState.selectedDateMillis?.let { viewModel.onAdmissionDate(it) }
                showAdmissionPicker = false
            }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showAdmissionPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = pickerState) }
    }
}

// ════════════════ Helpers ════════════════

@Composable
private fun SectionCard(
    title: String, subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
            subtitle?.let { Text(it, fontSize = 11.sp, color = TextMuted) }
            content()
        }
    }
}

@Composable
private fun TwoCol(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content)
}

@Composable
private fun Hint(text: String) {
    Text(text, fontSize = 10.sp, color = TextMuted,
        modifier = Modifier.fillMaxWidth())
}

@Composable
private fun LabeledField(
    label: String, value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    lines: Int = 1,
    hint: String? = null
) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = TextDark, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = hint?.let { { Text(it, fontSize = 12.sp, color = TextMuted) } },
            singleLine = lines == 1,
            minLines = lines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledDropdown(
    label: String, selected: String, placeholder: String,
    expanded: Boolean, options: List<String>,
    onExpand: (Boolean) -> Unit, onSelect: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = TextDark, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpand) {
            OutlinedTextField(
                value = selected.ifBlank { placeholder },
                onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(expanded, onDismissRequest = { onExpand(false) }) {
                options.forEach { opt ->
                    DropdownMenuItem(text = { Text(opt, fontSize = 13.sp) },
                        onClick = { onSelect(opt); onExpand(false) })
                }
            }
        }
    }
}

@Composable
private fun LabeledDateField(label: String, value: String, onPick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = TextDark, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true,
            placeholder = { Text("mm/dd/yyyy", fontSize = 12.sp, color = TextMuted) },
            trailingIcon = { IconButton(onClick = onPick) { Icon(Icons.Default.DateRange, null) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
private fun DocUploadRow(
    label: String, key: String,
    s: StudentFormState,
    onPick: (String) -> Unit
) {
    val uploaded = s.documents.containsKey(key)
    val uploading = s.uploadingDocKey == key
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = TextDark, modifier = Modifier.padding(bottom = 4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { onPick(key) },
                enabled = !uploading,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) { Text(if (uploaded) "Replace" else "Choose File", fontSize = 11.sp) }
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    uploading -> "Uploading…"
                    uploaded -> "Uploaded ✓"
                    else -> "No file chosen"
                },
                fontSize = 11.sp,
                color = if (uploaded) Color(0xFF16A34A) else TextMuted
            )
        }
        if (uploading) {
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth().height(3.dp))
        }
    }
}
