package com.example.solnex.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SolNexPrimary,
    onPrimary = SolNexOnPrimary,
    primaryContainer = SolNexPrimaryContainer,
    onPrimaryContainer = SolNexOnPrimaryContainer,
    secondary = SolNexSecondary,
    onSecondary = SolNexOnSecondary,
    secondaryContainer = SolNexSecondaryContainer,
    onSecondaryContainer = SolNexOnSecondaryContainer,
    tertiary = SolNexTertiary,
    onTertiary = SolNexOnTertiary,
    tertiaryContainer = SolNexTertiaryContainer,
    onTertiaryContainer = SolNexOnTertiaryContainer,
    background = SolNexBackground,
    onBackground = SolNexOnBackground,
    surface = SolNexSurface,
    onSurface = SolNexOnSurface,
    surfaceVariant = SolNexSurfaceVariant,
    onSurfaceVariant = SolNexOnSurfaceVariant,
    outline = SolNexOutline,
    outlineVariant = SolNexOutlineVariant,
    error = SolNexError,
    onError = SolNexOnError,
    errorContainer = SolNexErrorContainer,
    onErrorContainer = SolNexOnErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = SolNexPrimaryContainer,
    onPrimary = SolNexPrimary,
    primaryContainer = SolNexPrimary,
    onPrimaryContainer = SolNexPrimaryContainer,
    secondary = SolNexSecondaryContainer,
    onSecondary = SolNexSecondary,
    secondaryContainer = SolNexSecondary,
    onSecondaryContainer = SolNexSecondaryContainer,
    background = Color(0xFF0F1F17),
    onBackground = SolNexBackground,
    surface = Color(0xFF1A2E22),
    onSurface = SolNexBackground,
    surfaceVariant = Color(0xFF2A3D30),
    onSurfaceVariant = SolNexOnSurfaceVariant,
    error = SolNexError,
    onError = SolNexOnError,
)

@Composable
fun SolNexTheme(
    content: @Composable () -> Unit
) {
    // Always use the light brand theme — dark mode is intentionally disabled
    // to keep the SolNex green palette consistent across all devices.
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}