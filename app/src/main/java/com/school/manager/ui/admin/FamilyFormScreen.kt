package com.school.manager.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.ErrorRed
import com.school.manager.ui.theme.IndigoPrimary

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val BluePrimary = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyFormScreen(
    familyId: String,
    onNavigateBack: () -> Unit,
    viewModel: FamilyFormViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()

    LaunchedEffect(familyId) { viewModel.load(familyId) }
    LaunchedEffect(s.saved) { if (s.saved) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (s.isEdit) "Edit Family" else "Add Family",
                    color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = Color.White)
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
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Text(if (s.isEdit) "Edit Family" else "Add Family",
                        fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextDark)

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, null, tint = BluePrimary,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Once saved, link each sibling to this family from their Admission Form's Family field.",
                            fontSize = 11.sp, color = TextMuted)
                    }

                    Field("Family Name *", s.name, viewModel::onName,
                        hint = "e.g. Raza Family")
                    Field("Father Name", s.fatherName, viewModel::onFather)
                    Field("Father CNIC", s.fatherCnic, viewModel::onCnic,
                        hint = "XXXXX-XXXXXXX-X")
                    Field("Phone", s.phone, viewModel::onPhone)
                    Field("Address", s.address, viewModel::onAddress, lines = 3)
                    Field("Notes", s.notes, viewModel::onNotes, lines = 3)

                    s.error?.let { Text(it, color = ErrorRed, fontSize = 12.sp) }

                    Spacer(Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.save() },
                            enabled = !s.isSaving,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.height(46.dp)
                        ) {
                            if (s.isSaving) {
                                CircularProgressIndicator(Modifier.size(18.dp),
                                    color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(if (s.isEdit) "Save Changes" else "Save Family",
                                    fontWeight = FontWeight.Bold)
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
}

@Composable
private fun Field(
    label: String, value: String,
    onChange: (String) -> Unit,
    lines: Int = 1,
    hint: String? = null
) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            color = TextDark, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value, onValueChange = onChange,
            placeholder = hint?.let { { Text(it, fontSize = 12.sp, color = TextMuted) } },
            singleLine = lines == 1,
            minLines = lines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
    }
}
