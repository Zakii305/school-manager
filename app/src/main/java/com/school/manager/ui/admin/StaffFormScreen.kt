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
import androidx.compose.material.icons.filled.Save
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
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val LineGrey = Color(0xFFE2E8F0)
private val BluePrimary = Color(0xFF2563EB)

private val DEPARTMENTS = listOf(
    "Teaching", "Administration", "Accounts",
    "Support Staff", "Security", "Cleaning",
    "Transport", "Library", "IT"
)
private val STATUSES = listOf("active", "inactive", "on_leave", "resigned")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffFormScreen(
    staffId: String,
    onNavigateBack: () -> Unit,
    viewModel: StaffFormViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val context = LocalContext.current

    var deptExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var showHireDatePicker by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.pickPhoto(it, context.contentResolver) } }

    LaunchedEffect(staffId) { viewModel.load(staffId) }
    LaunchedEffect(s.saved) { if (s.saved) onNavigateBack() }

    val fmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.isEdit) "Edit Staff Member" else "Add Staff Member",
                    color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IndigoPrimary)
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).background(PageBg)
                .verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Row2 {
                        F("Staff ID *", s.staffId, viewModel::onStaffId, Modifier.weight(1f))
                        F("Full Name *", s.fullName, viewModel::onFullName, Modifier.weight(1f))
                    }

                    // Photo + Designation
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Photo", fontSize = 12.sp,
                                fontWeight = FontWeight.Medium, color = TextDark,
                                modifier = Modifier.padding(bottom = 4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(70.dp)
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
                                        Image(bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize())
                                    } else {
                                        Icon(Icons.Default.Person, null,
                                            tint = TextMuted, modifier = Modifier.size(28.dp))
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    OutlinedButton(
                                        onClick = { photoPicker.launch("image/*") },
                                        enabled = !s.photoUploading,
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 10.dp, vertical = 4.dp)
                                    ) { Text("Choose File", fontSize = 11.sp) }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        when {
                                            s.photoUploading -> "Uploading…"
                                            s.photoUrl != null -> "Uploaded ✓"
                                            else -> "No file chosen"
                                        },
                                        fontSize = 10.sp,
                                        color = if (s.photoUrl != null) Color(0xFF16A34A) else TextMuted
                                    )
                                    if (s.photoUploading) {
                                        Spacer(Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            Modifier.fillMaxWidth().height(3.dp))
                                    }
                                }
                            }
                        }
                        F("Designation", s.designation,
                            viewModel::onDesignation, Modifier.weight(1f))
                    }

                    Row2 {
                        DropdownField("Department", s.department, DEPARTMENTS,
                            deptExpanded, { deptExpanded = it }, viewModel::onDepartment,
                            Modifier.weight(1f))
                        F("Phone", s.phone, viewModel::onPhone, Modifier.weight(1f))
                    }

                    Row2 {
                        F("Email", s.email, viewModel::onEmail, Modifier.weight(1f))
                        F("CNIC", s.cnic, viewModel::onCnic, Modifier.weight(1f))
                    }

                    Row2 {
                        F("Monthly Salary *", s.monthlySalary, viewModel::onSalary, Modifier.weight(1f))
                        Column(Modifier.weight(1f)) {
                            Text("Hire Date", fontSize = 12.sp,
                                fontWeight = FontWeight.Medium, color = TextDark,
                                modifier = Modifier.padding(bottom = 4.dp))
                            OutlinedTextField(
                                value = s.hireDate?.let { fmt.format(Date(it)) } ?: "",
                                onValueChange = {}, readOnly = true,
                                placeholder = { Text("dd/MM/yyyy",
                                    fontSize = 12.sp, color = TextMuted) },
                                trailingIcon = {
                                    IconButton(onClick = { showHireDatePicker = true }) {
                                        Icon(Icons.Default.DateRange, null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    DropdownField("Status", s.status, STATUSES,
                        statusExpanded, { statusExpanded = it }, viewModel::onStatus,
                        Modifier.fillMaxWidth())

                    s.error?.let { Text(it, color = ErrorRed, fontSize = 12.sp) }

                    Spacer(Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.save() },
                            enabled = !s.isSaving && !s.photoUploading,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.height(46.dp)
                        ) {
                            if (s.isSaving) {
                                CircularProgressIndicator(Modifier.size(18.dp),
                                    color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Save, null,
                                    modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save", fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedButton(
                            onClick = onNavigateBack,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(46.dp)
                        ) { Text("Cancel") }
                    }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (showHireDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showHireDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.onHireDate(it) }
                    showHireDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showHireDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun Row2(content: @Composable RowScope.() -> Unit) {
    Row(Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content)
}

@Composable
private fun F(
    label: String, value: String,
    onChange: (String) -> Unit,
    modifier: Modifier
) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = TextDark, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String, value: String,
    options: List<String>,
    expanded: Boolean, onExpand: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier
) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = TextDark, modifier = Modifier.padding(bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpand) {
            OutlinedTextField(
                value = value.ifBlank { "— Select —" },
                onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(10.dp)
            )
            ExposedDropdownMenu(expanded, onDismissRequest = { onExpand(false) }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt.replaceFirstChar { it.uppercase() },
                            fontSize = 13.sp) },
                        onClick = { onSelect(opt); onExpand(false) })
                }
            }
        }
    }
}

