package com.school.manager.ui.update

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ForceUpdateDialog(
    message: String,
    onUpdate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* non-dismissible */ },
        title = { Text("Update Required") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text("Update Now", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
