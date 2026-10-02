package com.school.manager.ui.parent

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Parent portal entry — delegates to MyChildrenScreen which matches
 * the reference layout (grid of child cards).
 */
@Composable
fun ParentDashboard(
    onNavigateToChat: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ParentViewModel = hiltViewModel()
) {
    MyChildrenScreen(viewModel = viewModel)
}
