package com.school.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.school.manager.data.prefs.UserPreferences
import com.school.manager.ui.navigation.AppNavigation
import com.school.manager.ui.theme.SchoolManagerTheme
import com.school.manager.ui.update.ForceUpdateDialog
import com.school.manager.ui.update.UpdateChecker
import com.school.manager.util.FirestoreCollections
import com.school.manager.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class SessionState {
    data object Loading : SessionState()
    data object LoggedOut : SessionState()
    data class LoggedIn(val role: String) : SessionState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    prefs: UserPreferences,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    val followSystem: StateFlow<Boolean> = prefs.followSystem.stateIn(
        viewModelScope, SharingStarted.Eagerly, true
    )
    val darkMode: StateFlow<Boolean> = prefs.darkMode.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    private val _session = MutableStateFlow<SessionState>(SessionState.Loading)
    val session: StateFlow<SessionState> = _session.asStateFlow()

    init { restoreSession() }

    private fun restoreSession() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _session.value = SessionState.LoggedOut
            return
        }
        viewModelScope.launch {
            try {
                val doc = firestore.collection(FirestoreCollections.USERS)
                    .document(uid).get().await()
                val role = doc.getString("role") ?: "student"
                val schoolId = doc.getString("schoolId") ?: ""
                val name = doc.getString("name") ?: ""
                SessionManager.set(
                    SessionManager.Session(
                        uid = uid,
                        name = name,
                        email = doc.getString("email") ?: "",
                        role = role,
                        schoolId = schoolId,
                        isOwner = role == "owner"
                    )
                )
                val status = doc.getString("status") ?: "approved"
                if (status == "pending" || status == "rejected") {
                    auth.signOut()
                    _session.value = SessionState.LoggedOut
                    return@launch
                }
                _session.value = SessionState.LoggedIn(role)
            } catch (_: Exception) {
                _session.value = SessionState.LoggedOut
            }
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: MainViewModel = hiltViewModel()
            val followSystem by vm.followSystem.collectAsState()
            val manualDark by vm.darkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val useDark = if (followSystem) systemDark else manualDark
            val session by vm.session.collectAsState()
            val context = LocalContext.current
            var forceUpdateMsg by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                val result = UpdateChecker.check(context)
                if (result != null) forceUpdateMsg = result.second
            }

            SchoolManagerTheme(darkTheme = useDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (forceUpdateMsg != null) {
                        ForceUpdateDialog(
                            message = forceUpdateMsg!!,
                            onUpdate = { UpdateChecker.openPlayStore(context) }
                        )
                    } else {
                        AppNavigation(session = session)
                    }
                }
            }
        }
    }
}
