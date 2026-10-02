package com.school.manager.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.school.manager.R

private val LoginBg = Color(0xFF0B1730)
private val CardWhite = Color.White
private val TextDark = Color(0xFF0F172A)
private val TextMuted = Color(0xFF64748B)
private val PrimaryBlue = Color(0xFF2563EB)
private val FieldBg = Color(0xFFF1F5F9)
private val TabBg = Color(0xFFF1F5F9)
private val TabActive = Color.White
private val TabActiveText = Color(0xFF2563EB)
private val ErrorRed = Color(0xFFEF4444)

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onForgotPassword: () -> Unit,
    onSignup: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedEmail by viewModel.savedEmail.collectAsState()
    val savedPassword by viewModel.savedPassword.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var remember by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Admin") }
    var prefilled by remember { mutableStateOf(false) }

    LaunchedEffect(savedEmail, savedPassword) {
        if (!prefilled && savedEmail.isNotBlank()) {
            email = savedEmail
            password = savedPassword
            remember = true
            prefilled = true
        }
    }

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess && uiState.userRole != null) {
            onLoginSuccess(uiState.userRole!!)
            viewModel.resetLoginState()
        }
    }

    Box(
        Modifier.fillMaxSize().background(LoginBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo
                    Image(
                        painter = painterResource(R.drawable.ic_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(84.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "School Manager",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Sign in to your account",
                        color = TextMuted,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(22.dp))

                    // Role tabs
                    RoleTabs(
                        selected = selectedRole,
                        onSelect = { selectedRole = it }
                    )

                    Spacer(Modifier.height(20.dp))

                    // Email label + field
                    Column(Modifier.fillMaxWidth()) {
                        Text("Email Address", color = TextDark,
                            fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("you@school.edu.pk", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = FieldBg,
                                unfocusedContainerColor = FieldBg,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                cursorColor = PrimaryBlue
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Password label + field
                    Column(Modifier.fillMaxWidth()) {
                        Text("Password", color = TextDark,
                            fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle",
                                        tint = TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = FieldBg,
                                unfocusedContainerColor = FieldBg,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedTextColor = TextDark,
                                unfocusedTextColor = TextDark,
                                cursorColor = PrimaryBlue
                            )
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Forgot password right-aligned
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            "Forgot password?",
                            color = PrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { onForgotPassword() }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Remember me
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = remember,
                            onCheckedChange = { remember = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryBlue,
                                uncheckedColor = TextMuted,
                                checkmarkColor = Color.White
                            )
                        )
                        Text("Remember me", color = TextMuted, fontSize = 12.sp)
                    }

                    // Error
                    uiState.error?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, color = ErrorRed, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(14.dp))

                    // Sign In button
                    Button(
                        onClick = { viewModel.login(email, password, remember) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Create account
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Don't have an account? ", color = TextMuted, fontSize = 12.sp)
                        Text(
                            "Create Account",
                            color = PrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSignup() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleTabs(selected: String, onSelect: (String) -> Unit) {
    val roles = listOf("Admin", "Teacher", "Parent")
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = TabBg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            roles.forEach { role ->
                val active = role == selected
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (active) TabActive else Color.Transparent)
                        .clickable { onSelect(role) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        role,
                        color = if (active) TabActiveText else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
