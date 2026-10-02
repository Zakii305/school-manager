package com.school.manager.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val Violet = Color(0xFF4A148C)

data class GradeBand(val id: String, val label: String, val minPct: Double, val maxPct: Double, val remarks: String)

data class GradeState(val isLoading: Boolean = true, val bands: List<GradeBand> = emptyList())

@HiltViewModel
class GradeSettingsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _ui = MutableStateFlow(GradeState())
    val uiState = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val snap = firestore.collection("grade_settings").get().await()
                var bands = snap.documents.map { d ->
                    GradeBand(
                        id = d.id,
                        label = d.getString("label") ?: "-",
                        minPct = d.getDouble("minPct") ?: 0.0,
                        maxPct = d.getDouble("maxPct") ?: 0.0,
                        remarks = d.getString("remarks") ?: ""
                    )
                }.sortedByDescending { it.minPct }

                if (bands.isEmpty()) {
                    // Seed defaults on first run
                    bands = listOf(
                        GradeBand("A+", "A+", 90.0, 100.0, "Outstanding"),
                        GradeBand("A", "A", 80.0, 89.99, "Excellent"),
                        GradeBand("B", "B", 70.0, 79.99, "Very Good"),
                        GradeBand("C", "C", 60.0, 69.99, "Good"),
                        GradeBand("D", "D", 50.0, 59.99, "Satisfactory"),
                        GradeBand("F", "F", 0.0, 49.99, "Fail")
                    )
                }
                _ui.update { it.copy(isLoading = false, bands = bands) }
            } catch (_: Exception) { _ui.update { it.copy(isLoading = false) } }
        }
    }

    fun addBand(label: String, minPct: Double, maxPct: Double, remarks: String) {
        viewModelScope.launch {
            try {
                firestore.collection("grade_settings").add(mapOf(
                    "label" to label, "minPct" to minPct, "maxPct" to maxPct, "remarks" to remarks
                )).await()
                load()
            } catch (_: Exception) { }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSettingsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: GradeSettingsViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    var newLabel by remember { mutableStateOf("") }
    var newMin by remember { mutableStateOf("") }
    var newMax by remember { mutableStateOf("") }
    var newRemarks by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grade Settings", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg).padding(12.dp)) {
            Text("Grade Bands", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Define grade bands — every marks entry calculates grade from percentage",
                color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))

            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(14.dp)) {
                        listOf("GRADE" to 1f, "MIN %" to 1f, "MAX %" to 1f, "REMARKS" to 1.4f).forEach {
                            Text(it.first, color = TextMuted, fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(it.second))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    LazyColumn(Modifier.heightIn(max = 400.dp)) {
                        items(s.bands, key = { it.id }) { b ->
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(b.label, color = TextDark, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(b.minPct.toString(), color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text(b.maxPct.toString(), color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text(b.remarks, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Add a Grade Band", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = newLabel, onValueChange = { newLabel = it },
                label = { Text("Grade Label") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = newMin, onValueChange = { newMin = it },
                    label = { Text("Min %") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
                OutlinedTextField(value = newMax, onValueChange = { newMax = it },
                    label = { Text("Max %") }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp), singleLine = true)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = newRemarks, onValueChange = { newRemarks = it },
                label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp), singleLine = true)
            Spacer(Modifier.height(10.dp))
            Button(onClick = {
                val min = newMin.toDoubleOrNull() ?: 0.0
                val max = newMax.toDoubleOrNull() ?: 0.0
                if (newLabel.isNotBlank()) {
                    viewModel.addBand(newLabel, min, max, newRemarks)
                    newLabel = ""; newMin = ""; newMax = ""; newRemarks = ""
                }
            }, modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(10.dp)) {
                Text("+ Add Grade Band", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
