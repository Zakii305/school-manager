package com.school.manager.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.components.ConfirmDialog
import com.school.manager.ui.theme.ErrorRed
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.ui.theme.SuccessGreen
import com.school.manager.ui.theme.SchoolTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val followSystem by viewModel.followSystem.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val selectedTheme by viewModel.appTheme.collectAsState()
    val pwd by viewModel.password.collectAsState()

    var showChangePwd by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(pwd.success) {
        if (pwd.success) {
            snackbar.showSnackbar("Password changed ✅")
            showChangePwd = false
            viewModel.resetPasswordState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Appearance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Choose your school style", Modifier.padding(start = 16.dp, top = 16.dp), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    SchoolTheme.entries.forEach { theme ->
                        val chosen = selectedTheme.equals(theme.name, ignoreCase = true)
                        SettingRow(
                            icon = { androidx.compose.foundation.layout.Box(Modifier.size(24.dp).background(theme.primary, RoundedCornerShape(8.dp))) },
                            title = theme.title,
                            subtitle = theme.description,
                            trailing = { androidx.compose.material3.RadioButton(selected = chosen, onClick = { viewModel.setAppTheme(theme.name) }) },
                            onClick = { viewModel.setAppTheme(theme.name) }
                        )
                        if (theme != SchoolTheme.entries.last()) HorizontalDivider()
                    }
                    HorizontalDivider()
                    SettingRow(
                        icon = { Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = "Follow System Theme",
                        subtitle = "Auto-adjust to phone",
                        trailing = {
                            Switch(
                                checked = followSystem,
                                onCheckedChange = viewModel::setFollowSystem,
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                    HorizontalDivider()
                    SettingRow(
                        icon = { Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        title = "Dark Mode",
                        subtitle = if (followSystem) "Controlled by system" else "Manual",
                        trailing = {
                            Switch(
                                checked = if (followSystem) false else darkMode,
                                onCheckedChange = viewModel::setDarkMode,
                                enabled = !followSystem,
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Account",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingRow(
                        icon = { Icon(Icons.Default.Person, contentDescription = null, tint = IndigoPrimary) },
                        title = "My Profile",
                        subtitle = "View and edit information",
                        trailing = {},
                        onClick = onNavigateToProfile
                    )
                    HorizontalDivider()
                    SettingRow(
                        icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IndigoPrimary) },
                        title = "Change Password",
                        subtitle = "Update your account password",
                        trailing = {},
                        onClick = { showChangePwd = true }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("About",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingRow(
                        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = IndigoPrimary) },
                        title = "Version",
                        subtitle = "1.0.0 (build 1)",
                        trailing = {}
                    )
                    HorizontalDivider()
                    SettingRow(
                        icon = { Icon(Icons.Default.Campaign, contentDescription = null, tint = IndigoPrimary) },
                        title = "Notifications",
                        subtitle = "Manage push alerts",
                        trailing = {}
                    )
                    HorizontalDivider()
                    SettingRow(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error) },
                        title = "Log Out",
                        subtitle = "Sign out of this device",
                        trailing = {},
                        onClick = { showLogoutConfirm = true },
                        titleColor = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showChangePwd) {
        AlertDialog(
            onDismissRequest = { if (!pwd.isSaving) { showChangePwd = false; viewModel.resetPasswordState() } },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pwd.current, onValueChange = viewModel::onCurrent,
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = pwd.newPass, onValueChange = viewModel::onNew,
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = pwd.confirm, onValueChange = viewModel::onConfirm,
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    pwd.error?.let {
                        Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.changePassword() },
                    enabled = !pwd.isSaving
                ) {
                    if (pwd.isSaving) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePwd = false; viewModel.resetPasswordState() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogoutConfirm) {
        ConfirmDialog(
            title = "Log out?",
            message = "You'll need to sign in again to use the app.",
            confirmLabel = "Log Out",
            isDestructive = true,
            onConfirm = onLogout,
            onDismiss = { showLogoutConfirm = false }
        )
    }
}

@Composable
private fun SettingRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.padding(start = 8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium, color = titleColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}
