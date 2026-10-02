package com.school.manager.ui.teacher

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.util.FirestoreCollections
import com.school.manager.ui.theme.IndigoPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private val PageBg = Color(0xFFF5F6FA)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)

data class MyClassRow(val className: String, val subject: String)

data class MyClassesUiState(
    val isLoading: Boolean = true,
    val rows: List<MyClassRow> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MyClassesViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _ui = MutableStateFlow(MyClassesUiState())
    val uiState: StateFlow<MyClassesUiState> = _ui.asStateFlow()

    init { load() }

    fun load() {
        val uid = auth.currentUser?.uid ?: return
        _ui.value = _ui.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val subs = firestore.collection(FirestoreCollections.SUBJECTS)
                    .whereEqualTo("teacherId", uid).get().await()
                val classSnap = firestore.collection(FirestoreCollections.CLASSES).get().await()
                val classMap = classSnap.documents.associate {
                    it.id to (it.getString("name") ?: "-")
                }
                val rows = subs.documents.map { d ->
                    MyClassRow(
                        className = classMap[d.getString("classId")] ?: "-",
                        subject = d.getString("name") ?: "-"
                    )
                }
                _ui.value = MyClassesUiState(isLoading = false, rows = rows)
            } catch (e: Exception) {
                _ui.value = MyClassesUiState(isLoading = false, error = e.message)
            }
        }
    }
}

@Composable
fun MyClassesScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MyClassesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawer = com.school.manager.ui.navigation.LocalDrawerController.current

    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).background(PageBg)
        ) {
            // Top bar
            Row(
                Modifier.fillMaxWidth().background(Color(0xFF0B1730))
                    .padding(horizontal = 6.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { drawer?.open() }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text("My Classes",
                        color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("Classes and subjects assigned to you",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                IconButton(onClick = { viewModel.load() }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = IndigoPrimary)
                }
                return@Scaffold
            }

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("My Classes & Subjects",
                            color = TextDark, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(14.dp))

                        // Header
                        Row(
                            Modifier.fillMaxWidth()
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text("CLASS", color = TextMuted, fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f))
                            Text("SUBJECT", color = TextMuted, fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0))

                        if (uiState.rows.isEmpty()) {
                            Box(
                                Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No classes assigned yet",
                                    color = TextMuted, fontSize = 13.sp)
                            }
                        } else {
                            uiState.rows.forEachIndexed { i, r ->
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(if (i % 2 == 0) CardWhite
                                        else Color(0xFFF8FAFC))
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Text(r.className, color = TextDark,
                                        fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Text(r.subject, color = TextDark,
                                        fontSize = 13.sp, modifier = Modifier.weight(1f))
                                }
                                HorizontalDivider(color = Color(0xFFE2E8F0))
                            }
                        }
                    }
                }
            }
        }
    }
}
