package com.school.manager.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

/**
 * Show any error/status from a Flow<String?> as a snackbar.
 * Clear it with the second lambda so it doesn't re-fire.
 */
@Composable
fun ErrorSnackbarEffect(
    message: String?,
    hostState: SnackbarHostState,
    onShown: () -> Unit
) {
    LaunchedEffect(message) {
        message?.let {
            hostState.showSnackbar(
                message = it,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            onShown()
        }
    }
}

@Composable
fun RetrySnackbarEffect(
    message: String?,
    hostState: SnackbarHostState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        message?.let {
            val result = hostState.showSnackbar(
                message = it,
                actionLabel = "Retry",
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) onRetry()
            onDismiss()
        }
    }
}
