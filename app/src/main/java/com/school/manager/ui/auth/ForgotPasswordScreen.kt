package com.school.manager.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.ui.theme.AmberAccent
import com.school.manager.ui.theme.ErrorRed
import com.school.manager.ui.theme.IndigoDark
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.resetEmailSent) {
        // auto-navigate back after 3s on success
        if (state.resetEmailSent) {
            kotlinx.coroutines.delay(3000)
            viewModel.resetLoginState()
            onNavigateBack()
        }
    }

    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(IndigoPrimary, IndigoDark)))
    ) {
        IconButton(
            onClick = { viewModel.resetLoginState(); onNavigateBack() },
            modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        Card(
            Modifier.align(Alignment.Center).fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Reset Password",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("We'll email you a reset link",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f))

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = AmberAccent,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AmberAccent
                    )
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.sendPasswordReset(email) },
                    enabled = !state.isLoading && email.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent, contentColor = IndigoDark)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(22.dp), color = IndigoDark, strokeWidth = 2.dp)
                    } else Text("Send Reset Link", fontWeight = FontWeight.Bold)
                }

                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }
                if (state.resetEmailSent) {
                    Spacer(Modifier.height(12.dp))
                    Text("✅ Email sent! Check your inbox.", color = SuccessGreen,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
