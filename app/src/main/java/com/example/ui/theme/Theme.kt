package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF0A0F1D),
    primaryContainer = SlateSurfaceVariant,
    onPrimaryContainer = CyberCyan,
    secondary = TerminalGreen,
    onSecondary = Color(0xFF0A0F1D),
    background = SlateBackground,
    onBackground = LightText,
    surface = SlateSurface,
    onSurface = LightText,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SecondaryText,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
