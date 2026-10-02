package com.school.manager.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.school.manager.ui.theme.IndigoPrimary
import com.school.manager.ui.theme.SuccessGreen
import com.school.manager.ui.theme.WarningOrange
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeePaymentScreen(
    feeId: String,
    feeAmount: Double = 5000.0,
    feeDesc: String = "Tuition Fee",
    onNavigateBack: () -> Unit,
    onPaymentSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var stage by remember { mutableStateOf("review") } // review | processing | success
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(stage) {
        if (stage == "processing") {
            delay(2200)
            stage = "success"
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Pay Fee") },
                navigationIcon = {
                    if (stage != "processing") {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IndigoPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            when (stage) {
                "review" -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = IndigoPrimary.copy(alpha = 0.08f))
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp)) {
                            Text("Amount Due", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rs ${feeAmount.toInt()}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary)
                            Spacer(Modifier.height(6.dp))
                            Text(feeDesc, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Text("Payment Method", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)

                    PaymentRow("Credit / Debit Card", "Visa, Mastercard",
                        selected = true) {}
                    PaymentRow("Razorpay UPI", "GPay, PhonePe, Paytm",
                        selected = false) {}
                    PaymentRow("Net Banking", "All major banks",
                        selected = false) {}

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = { stage = "processing" },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pay Rs ${feeAmount.toInt()}",
                            fontWeight = FontWeight.Bold)
                    }
                }

                "processing" -> Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = IndigoPrimary, strokeWidth = 4.dp)
                    Spacer(Modifier.height(20.dp))
                    Text("Processing payment…",
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Please don't close the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                "success" -> Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = SuccessGreen, modifier = Modifier.size(96.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Payment Successful!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Rs ${feeAmount.toInt()} paid successfully",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(20.dp))
                    Text("Transaction ID: TXN-${System.currentTimeMillis().toString().takeLast(8)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(40.dp))
                    Button(
                        onClick = {
                            onPaymentSuccess()
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Done", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) WarningOrange.copy(alpha = 0.10f)
            else MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
