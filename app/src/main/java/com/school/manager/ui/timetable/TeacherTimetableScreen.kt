package com.school.manager.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.components.SchoolTopBar
import com.school.manager.ui.theme.IndigoLight
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.ui.theme.SuccessGreen
import com.school.manager.ui.theme.TealSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherTimetableScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onNotifications: () -> Unit,
    viewModel: TeacherTimetableViewModel = hiltViewModel()
) {
    val s by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            SchoolTopBar(
                title = "My Timetable",
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
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(IndigoPrimary, IndigoLight)))
                    .padding(16.dp)
            ) {
                Column {
                    Text("Teacher's Timetable",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall)
                    Text(s.teacherName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.horizontalScroll(rememberScrollState())) {
                Box(Modifier.size(width = 62.dp, height = 40.dp),
                    contentAlignment = Alignment.Center) {
                    Text("Period",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold)
                }
                for (day in WEEK_DAYS) {
                    Box(Modifier.size(width = 118.dp, height = 40.dp),
                        contentAlignment = Alignment.Center) {
                        Text(day.take(3),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary)
                    }
                }
            }

            for (p in 1..MAX_PERIODS) {
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    Box(Modifier.size(width = 62.dp, height = 76.dp),
                        contentAlignment = Alignment.Center) {
                        Text("%02d".format(p),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold)
                    }
                    for (day in WEEK_DAYS) {
                        val cell = s.cells.firstOrNull { it.day == day && it.period == p }
                        Box(Modifier.size(width = 118.dp, height = 76.dp).padding(3.dp)) {
                            TeacherCell(cell)
                        }
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun TeacherCell(cell: TeacherCell?) {
    Card(
        Modifier.fillMaxSize(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (cell != null) TealSecondary.copy(alpha = 0.15f)
                             else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Box(Modifier.fillMaxSize().padding(6.dp), contentAlignment = Alignment.Center) {
            if (cell != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Text(cell.subjectName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = IndigoPrimary,
                        maxLines = 2)
                    Spacer(Modifier.height(2.dp))
                    Text(cell.className,
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
