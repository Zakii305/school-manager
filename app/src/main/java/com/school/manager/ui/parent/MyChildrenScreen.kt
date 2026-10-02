package com.school.manager.ui.parent

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.IndigoPrimary
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val GreenText = Color(0xFF16A34A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyChildrenScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ParentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("My Children", color = Color.White,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { drawer?.open() }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu",
                            tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.reload() }) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid",
                            tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B1730)
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(PageBg)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = IndigoPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Scaffold
            }

            if (uiState.children.isEmpty()) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Person, contentDescription = null,
                        tint = TextMuted, modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No children linked",
                        color = TextDark, fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Ask the school admin to link your children",
                        color = TextMuted, fontSize = 12.sp)
                }
                return@Scaffold
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.children, key = { it.uid }) { child ->
                    ChildCard(child)
                }
            }
        }
    }
}

@Composable
private fun ChildCard(child: ChildInfo) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null,
                        tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.padding(start = 8.dp))
                Column {
                    Text(child.name, color = TextDark, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("Adm# ${child.uid.takeLast(3)}",
                        color = TextMuted, fontSize = 10.sp)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("This Month Attendance",
                        color = TextMuted, fontSize = 9.sp)
                    Text(
                        if (child.presentCount + child.absentCount > 0)
                            "${child.attendancePercent}%"
                        else "—",
                        color = if (child.attendancePercent >= 75) GreenText else TextDark,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pending Fees", color = TextMuted, fontSize = 9.sp)
                    Text("Rs ${child.pendingFees.toInt()}",
                        color = if (child.pendingFees > 0) Color(0xFFDC2626) else GreenText,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
