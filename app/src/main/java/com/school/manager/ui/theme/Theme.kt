package com.school.manager.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = IndigoLight,
    onPrimaryContainer = IndigoDark,
    secondary = TealSecondary,
    onSecondary = SurfaceLight,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    tertiary = AmberAccent,
    onTertiary = OnSurfaceLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = ErrorRed,
    onError = SurfaceLight
)

private val DarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = IndigoDark,
    primaryContainer = IndigoPrimary,
    onPrimaryContainer = IndigoLight,
    secondary = TealLight,
    onSecondary = TealDark,
    secondaryContainer = TealSecondary,
    onSecondaryContainer = TealLight,
    tertiary = AmberLight,
    onTertiary = AmberDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorRed,
    onError = SurfaceLight
)

enum class SchoolTheme(val title: String, val description: String, val primary: Color, val secondary: Color, val accent: Color) {
    CLASSIC("Royal Violet", "Confident violet with teal highlights", Color(0xFF51206F), Color(0xFF16877D), Color(0xFFFFB84D)),
    OCEAN("Ocean Blue", "Calm blue with aqua accents", Color(0xFF145DA0), Color(0xFF00A6A6), Color(0xFFFFC857)),
    FOREST("Evergreen", "Focused green with warm gold", Color(0xFF246B52), Color(0xFF558B2F), Color(0xFFE0A82E)),
    SLATE("Executive Slate", "Refined navy and cool silver", Color(0xFF29364B), Color(0xFF526A82), Color(0xFFB58A4A));

    companion object {
        fun from(value: String) = entries.firstOrNull { it.name.equals(value, true) } ?: CLASSIC
    }
}

@Composable
fun SchoolManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    schoolTheme: SchoolTheme = SchoolTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val lightScheme = lightColorScheme(
        primary = schoolTheme.primary, onPrimary = Color.White,
        primaryContainer = schoolTheme.primary.copy(alpha = .12f), onPrimaryContainer = schoolTheme.primary,
        secondary = schoolTheme.secondary, onSecondary = Color.White,
        secondaryContainer = schoolTheme.secondary.copy(alpha = .13f), onSecondaryContainer = schoolTheme.secondary,
        tertiary = schoolTheme.accent, onTertiary = Color(0xFF352500),
        background = Color(0xFFF6F7FA), onBackground = Color(0xFF1B1D23),
        surface = Color.White, onSurface = Color(0xFF1B1D23), error = ErrorRed
    )
    val darkScheme = darkColorScheme(
        primary = schoolTheme.primary.copy(red = (schoolTheme.primary.red + .22f).coerceAtMost(1f), green = (schoolTheme.primary.green + .22f).coerceAtMost(1f), blue = (schoolTheme.primary.blue + .22f).coerceAtMost(1f)),
        secondary = schoolTheme.secondary, tertiary = schoolTheme.accent,
        background = Color(0xFF11151B), surface = Color(0xFF1A2029), error = ErrorRed
    )
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme
        else -> lightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
