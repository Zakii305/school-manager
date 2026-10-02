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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.IndigoPrimary

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val LineGrey = Color(0xFFE2E8F0)
private val BluePrimary = Color(0xFF2563EB)
private val ActiveGreen = Color(0xFF16A34A)
private val ActiveGreenBg = Color(0xFFDCFCE7)
private val RedDelete = Color(0xFFDC2626)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListScreen(
    onNavigateBack: () -> Unit = {},
    onAddStaff: () -> Unit = {},
    onEditStaff: (String) -> Unit = {},
    viewModel: StaffListViewModel = hiltViewModel()
) {
    val s by viewModel.state.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current
    var deptExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teachers & Staff", color = Color.White, fontSize = 17.sp,
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

                    // Search — full width, single line
                    OutlinedTextField(
                        value = s.query,
                        onValueChange = viewModel::onQuery,
                        placeholder = { Text("Search staff...", fontSize = 12.sp, color = TextMuted) },
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

                    // Department + Add Staff
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = deptExpanded,
                            onExpandedChange = { deptExpanded = !deptExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = s.departmentFilter ?: "All Departments",
                                onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(deptExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            ExposedDropdownMenu(deptExpanded,
                                onDismissRequest = { deptExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("All Departments", fontSize = 12.sp) },
                                    onClick = { viewModel.onDepartmentFilter(null); deptExpanded = false })
                                s.departments.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d, fontSize = 12.sp) },
                                        onClick = { viewModel.onDepartmentFilter(d); deptExpanded = false })
                                }
                            }
                        }
                        Button(
                            onClick = onAddStaff,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Staff", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                maxLines = 1)
                        }
                    }

                    // Refresh
                    OutlinedButton(
                        onClick = { viewModel.load() },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Refresh", fontSize = 11.sp)
                    }
                }
            }

            // ═══════ TABLE — fills remaining height, H+V scroll ═══════
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 10.dp)
            ) {
                Column(Modifier.fillMaxSize()) {

                    // Horizontal-scrollable table body
                    Box(Modifier.fillMaxWidth().weight(1f)
                        .horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(820.dp).fillMaxHeight()) {

                            // Header pinned at top of column
                            Row(
                                Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("STAFF ID", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(90.dp))
                                Text("NAME", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(160.dp))
                                Text("DESIGNATION", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(140.dp))
                                Text("DEPARTMENT", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(140.dp))
                                Text("PHONE", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(130.dp))
                                Text("STATUS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(80.dp))
                                Text("ACTIONS", color = TextMuted, fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1,
                                    modifier = Modifier.width(80.dp))
                            }
                            HorizontalDivider(color = LineGrey)

                            // Rows — vertical scroll
                            when {
                                s.isLoading -> Box(Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = IndigoPrimary)
                                }
                                s.filtered.isEmpty() -> Box(
                                    Modifier.fillMaxWidth().padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No staff found", color = TextMuted, fontSize = 13.sp)
                                }
                                else -> Column(
                                    Modifier.fillMaxWidth().weight(1f)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    s.filtered.forEach { st ->
                                        Row(
                                            Modifier.fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(st.staffId.ifBlank { "-" }, color = TextDark,
                                                fontSize = 12.sp, maxLines = 1,
                                                modifier = Modifier.width(90.dp))
                                            Text(st.name, color = TextDark, fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium, maxLines = 1,
                                                modifier = Modifier.width(160.dp))
                                            Text(st.designation.ifBlank { "-" }, color = TextMuted,
                                                fontSize = 12.sp, maxLines = 1,
                                                modifier = Modifier.width(140.dp))
                                            Text(st.department.ifBlank { "-" }, color = TextMuted,
                                                fontSize = 12.sp, maxLines = 1,
                                                modifier = Modifier.width(140.dp))
                                            Text(st.phone, color = TextMuted, fontSize = 12.sp,
                                                maxLines = 1, modifier = Modifier.width(130.dp))
                                            Box(Modifier.width(80.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = when (st.status.lowercase()) {
                                                        "active" -> ActiveGreenBg
                                                        "inactive", "resigned" -> Color(0xFFF1F5F9)
                                                        else -> Color(0xFFFEF3C7)
                                                    }
                                                ) {
                                                    Text(
                                                        st.status.replaceFirstChar { it.uppercase() },
                                                        color = when (st.status.lowercase()) {
                                                            "active" -> ActiveGreen
                                                            "inactive", "resigned" -> TextMuted
                                                            else -> Color(0xFFB45309)
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        modifier = Modifier.padding(
                                                            horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                            Row(Modifier.width(80.dp),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { onEditStaff(st.id) },
                                                    modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Edit, null,
                                                        tint = Color(0xFFF59E0B),
                                                        modifier = Modifier.size(15.dp))
                                                }
                                                IconButton(onClick = { viewModel.askDelete(st.id) },
                                                    modifier = Modifier.size(28.dp)) {
                                                    Icon(Icons.Default.Delete, null,
                                                        tint = RedDelete,
                                                        modifier = Modifier.size(15.dp))
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = LineGrey)
                                    }
                                }
                            }
                        }
                    }

                    // Footer — pinned, outside horizontal scroll
                    if (s.filtered.isNotEmpty()) {
                        Row(
                            Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Showing 1-${s.filtered.size} of ${s.staff.size}",
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
            title = { Text("Delete Staff") },
            text = { Text("This will permanently remove the staff member. Continue?") },
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
