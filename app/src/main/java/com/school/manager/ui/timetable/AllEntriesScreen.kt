package com.school.manager.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.components.SchoolTopBar
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.ui.theme.TealSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllEntriesScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onNotifications: () -> Unit,
    viewModel: AllEntriesViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SchoolTopBar(
                title = "All Entries",
                onMenuClick = onBack,
                onSearchClick = onSearch,
                onNotificationsClick = onNotifications,
                onBackClick = onBack
            )
        }
    ) { padding ->
        if (s.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IndigoPrimary)
            }
            return@Scaffold
        }

        Column(
            Modifier.fillMaxSize().padding(padding).padding(12.dp)
        ) {
            Text("All Timetable Entries by Class",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = IndigoPrimary,
                modifier = Modifier.padding(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    // Header
                    Row(Modifier
                        .fillMaxWidth()
                        .background(IndigoPrimary)
                        .padding(vertical = 10.dp)) {
                        HeaderCell("Day", 0.8f)
                        HeaderCell("P", 0.4f)
                        HeaderCell("Subject (Class)", 2f)
                        HeaderCell("Teacher", 1.4f)
                    }

                    if (s.rows.isEmpty()) {
                        Box(Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center) {
                            Text("No entries yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        s.rows.forEachIndexed { i, r ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (i % 2 == 0) MaterialTheme.colorScheme.surface
                                        else TealSecondary.copy(alpha = 0.06f)
                                    )
                                    .padding(vertical = 8.dp)
                            ) {
                                BodyCell(r.day.take(3), 0.8f, bold = true)
                                BodyCell("${r.period}", 0.4f)
                                BodyCell(r.subjectClass, 2f)
                                BodyCell(r.teacherName, 1.4f)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RowScopeHeader() {}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(text: String, weight: Float) {
    Text(text,
        Modifier.weight(weight).padding(horizontal = 6.dp),
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold)
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BodyCell(
    text: String, weight: Float, bold: Boolean = false
) {
    Text(text,
        Modifier.weight(weight).padding(horizontal = 6.dp),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 2)
}
