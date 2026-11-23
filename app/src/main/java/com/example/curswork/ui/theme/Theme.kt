package com.example.curswork.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF2B1700),
    primaryContainer = Color(0xFF5D3F00),
    onPrimaryContainer = Color(0xFFFFDDB3),
    secondary = Color(0xFF90CAF9),
    background = Color(0xFF121418),
    surface = Color(0xFF1A1D24),
    surfaceVariant = Color(0xFF252A34)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE68A00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0B2),
    onPrimaryContainer = Color(0xFF3E2200),
    secondary = Color(0xFF1976D2),
    background = Color(0xFFF6F7FB),
    surface = Color.White,
    surfaceVariant = Color(0xFFECEFF4)
)

@Composable
fun CursWorkTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}