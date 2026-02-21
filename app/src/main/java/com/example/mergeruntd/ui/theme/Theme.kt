package com.example.mergeruntd.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
    darkColorScheme(
        primary = CyanPrimary,
        onPrimary = SurfaceLight,
        surface = ColorSchemeTokens.DarkSurface,
        onSurface = ColorSchemeTokens.DarkOnSurface,
        surfaceVariant = ColorSchemeTokens.DarkSurfaceVariant,
        outline = ColorSchemeTokens.DarkOutline,
        error = ErrorRed,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = CyanPrimary,
        onPrimary = SurfaceLight,
        primaryContainer = CyanPrimaryContainer,
        onPrimaryContainer = TextPrimary,
        secondary = TextSecondary,
        tertiary = TextSecondary,
        surface = SurfaceLight,
        background = SurfaceMuted,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceMuted,
        outline = OutlineLight,
        error = ErrorRed,
    )

@Composable
fun mergeruntdTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}

private object ColorSchemeTokens {
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF121212)
    val DarkOnSurface = androidx.compose.ui.graphics.Color(0xFFE3E3E3)
    val DarkSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF1F1F1F)
    val DarkOutline = androidx.compose.ui.graphics.Color(0xFF3C4043)
}
