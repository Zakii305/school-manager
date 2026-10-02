package com.school.manager.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.IndigoPrimary

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val LineGrey = Color(0xFFE2E8F0)
private val ActiveGreen = Color(0xFF16A34A)
private val ActiveGreenBg = Color(0xFFDCFCE7)
private val BluePrimary = Color(0xFF2563EB)
private val RedDelete = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsListScreen(
    onNavigateBack: () -> Unit = {},
    onAddStudent: () -> Unit = {},
    onEditStudent: (String) -> Unit = {},
    onViewStudent: (String) -> Unit = {},
    viewModel: StudentsListViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    val snackbar = remember { SnackbarHostState() }

    var classExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var pageExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(s.infoMessage) {
        s.infoMessage?.let { snackbar.showSnackbar(it); viewModel.consumeInfo() }
    }
    LaunchedEffect(s.error) {
        s.error?.let { snackbar.showSnackbar(it); viewModel.consumeError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Student", color = Color.White, fontSize = 17.sp,
                    fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg)) {

            // ═══════ FILTERS ═══════
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) {
                Column(Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    OutlinedTextField(
                        value = s.query,
                        onValueChange = viewModel::onQuery,
                        placeholder = { Text("Search students...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, null,
                            tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        trailingIcon = if (s.query.isNotBlank()) {
                            { IconButton(onClick = { viewModel.onQuery("") }) {
                                Icon(Icons.Default.Close, null,
                                    tint = TextMuted, modifier = Modifier.size(16.dp))
                            } }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = classExpanded,
                            onExpandedChange = { classExpanded = !classExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = s.classFilter?.let { id ->
                                    s.classes.firstOrNull { it.first == id }?.second
                                } ?: "All Classes",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(classExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            ExposedDropdownMenu(classExpanded,
                                onDismissRequest = { classExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("All Classes", fontSize = 12.sp) },
                                    onClick = { viewModel.onClassFilter(null); classExpanded = false })
                                s.classes.forEach { (id, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label, fontSize = 12.sp) },
                                        onClick = { viewModel.onClassFilter(id); classExpanded = false })
                                }
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = !statusExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = s.statusFilter?.replaceFirstChar { it.uppercase() } ?: "All Status",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(statusExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            ExposedDropdownMenu(statusExpanded,
                                onDismissRequest = { statusExpanded = false }) {
                                listOf("All Status", "active", "left", "suspended").forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text(st.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                                        onClick = {
                                            viewModel.onStatusFilter(if (st == "All Status") null else st)
                                            statusExpanded = false
                                        })
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        ExposedDropdownMenuBox(
                            expanded = pageExpanded,
                            onExpandedChange = { pageExpanded = !pageExpanded },
                            modifier = Modifier.width(110.dp)
                        ) {
                            OutlinedTextField(
                                value = "${s.pageSize} / page",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(pageExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            ExposedDropdownMenu(pageExpanded,
                                onDismissRequest = { pageExpanded = false }) {
                                listOf(10, 25, 50, 100).forEach { n ->
                                    DropdownMenuItem(
                                        text = { Text("$n / page", fontSize = 12.sp) },
                                        onClick = { viewModel.onPageSize(n); pageExpanded = false })
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = onAddStudent,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Student", fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.load() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Refresh", fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Print, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Print", fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = {},
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF", fontSize = 11.sp)
                        }
                    }
                }
            }

            // ═══════ TABLE ═══════
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 10.dp)
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().weight(1f)
                        .horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(960.dp).fillMaxHeight()) {

                            Row(
                                Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val allSelected = s.paged.isNotEmpty() &&
                                    s.paged.all { it.id in s.selectedIds }
                                Checkbox(
                                    checked = allSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            val cur = s.selectedIds.toMutableSet()
                                            s.paged.forEach { cur.add(it.id) }
                                            cur.forEach { id ->
                                                if (id !in s.selectedIds) viewModel.toggleSelect(id)
                                            }
                                        } else {
                                            val cur = s.selectedIds.toMutableSet()
                                            s.paged.forEach { cur.remove(it.id) }
                                            // Toggle each selected to deselect
                                            s.selectedIds
                                                .filter { it in s.paged.map { p -> p.id } }
                                                .forEach { viewModel.toggleSelect(it) }
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("ADMISSION NO", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(100.dp))
                                Text("STUDENT", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(170.dp))
                                Text("CLASS / SECTION", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(150.dp))
                                Text("FATHER NAME", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(150.dp))
                                Text("CONTACT", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(130.dp))
                                Text("STATUS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(80.dp))
                                Text("ACTIONS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(140.dp))
                            }
                            HorizontalDivider(color = LineGrey)

                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = IndigoPrimary)
                                }
                                s.paged.isEmpty() -> Box(
                                    Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No students found", color = TextMuted, fontSize = 13.sp)
                                }
                                else -> Column(
                                    Modifier.fillMaxWidth().weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    s.paged.forEach { st ->
                                        Row(
                                            Modifier.fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = st.id in s.selectedIds,
                                                onCheckedChange = { viewModel.toggleSelect(st.id) },
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(st.admissionNo, color = TextDark, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(100.dp))
                                            Text(st.name, color = TextDark, fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium, maxLines = 1,
                                                modifier = Modifier.width(170.dp))
                                            Text(st.className, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(150.dp))
                                            Text(st.fatherName, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(150.dp))
                                            Text(st.phone, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(130.dp))
                                            Box(Modifier.width(80.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = when (st.status.lowercase()) {
                                                        "active" -> ActiveGreenBg
                                                        "left" -> Color(0xFFF1F5F9)
                                                        else -> Color(0xFFFEF3C7)
                                                    }
                                                ) {
                                                    Text(st.status.replaceFirstChar { it.uppercase() },
                                                        color = when (st.status.lowercase()) {
                                                            "active" -> ActiveGreen
                                                            "left" -> TextMuted
                                                            else -> Color(0xFFB45309)
                                                        },
                                                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        modifier = Modifier.padding(
                                                            horizontal = 6.dp, vertical = 3.dp))
                                                }
                                            }
                                            Row(Modifier.width(140.dp),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { onViewStudent(st.id) },
                                                    modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Visibility, null,
                                                        tint = BluePrimary, modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = { onEditStudent(st.id) },
                                                    modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Edit, null,
                                                        tint = Color(0xFFF59E0B),
                                                        modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = { viewModel.askDelete(st.id) },
                                                    modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Delete, null,
                                                        tint = RedDelete, modifier = Modifier.size(15.dp))
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = LineGrey)
                                    }
                                }
                            }
                        }
                    }

                    if (s.paged.isNotEmpty()) {
                        Row(Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.Center) {
                            Text("Showing 1-${s.paged.size} of ${s.total}",
                                fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }

    if (s.confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Student") },
            text = { Text("This will permanently remove the student record. Continue?") },
            confirmButton = {
                TextButton(onClick = { viewModel.doDelete() }) {
                    Text("Delete", color = RedDelete, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) { Text("Cancel") }
            }
        )
    }
}
