package com.school.manager.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.exams.ExamViewModel

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val AccentBlue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamMarksScreen(
    onNavigateBack: () -> Unit = {},
    onOpenGradebook: (String) -> Unit = {},
    viewModel: ExamViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Schedule & Marks", color = Color.White,
                    fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B1730))
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(PageBg)) {

            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search exams...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null,
                    tint = TextMuted, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().background(Color(0xFFF8FAFC))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text("EXAM", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.6f))
                        Text("CLASS", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("MARKS", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(0.8f))
                        Text("ACTION", color = TextMuted, fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.width(72.dp))
                    }
                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    if (state.isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AccentBlue)
                        }
                    } else if (state.exams.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No exams created",
                                    color = TextDark, fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(4.dp))
                                Text("Create an exam first",
                                    color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(0.dp)) {
                            items(state.exams, key = { it.id }) { e ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(e.name, color = TextDark, fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1.6f), maxLines = 1)
                                    Text(e.className.ifBlank { "—" }, color = TextMuted,
                                        fontSize = 11.sp, modifier = Modifier.weight(1f),
                                        maxLines = 1)
                                    Text("${e.maxMarks}", color = TextDark,
                                        fontSize = 11.sp, modifier = Modifier.weight(0.8f))
                                    Box(Modifier.width(72.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = AccentBlue.copy(alpha = 0.12f)
                                        ) {
                                            Row(
                                                Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Enter", color = AccentBlue,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
