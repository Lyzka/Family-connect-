package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HoloColorScheme = darkColorScheme(
    primary = HoloCyan,
    onPrimary = HoloBlack,
    secondary = HoloGold,
    onSecondary = HoloBlack,
    tertiary = HoloCyanBright,
    error = HoloRed,
    background = HoloBlack,
    onBackground = HoloWhite,
    surface = HoloDeepBlue,
    onSurface = HoloWhite,
    surfaceVariant = HoloCorridorBlue,
    onSurfaceVariant = HoloSubtleText,
    outline = HoloCyanBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HoloColorScheme,
        typography = Typography,
        content = content
    )
}
