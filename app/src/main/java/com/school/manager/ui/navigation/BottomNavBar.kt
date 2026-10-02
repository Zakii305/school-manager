package com.school.manager.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Drawer controller — exposes open/close lambdas via CompositionLocal.
 * Read `LocalDrawerController.current` in a composable scope (not inside onClick).
 */
data class DrawerController(
    val open: () -> Unit,
    val close: () -> Unit
)

val LocalDrawerController = staticCompositionLocalOf<DrawerController?> { null }
