package com.wc2026stickers.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WorldCupGreen = Color(0xFF00843D)
private val WorldCupGold = Color(0xFFFCC200)
private val DarkBackground = Color(0xFF1A1A2E)

private val LightColors = lightColorScheme(
    primary = WorldCupGreen,
    secondary = WorldCupGold,
    background = Color(0xFFF5F5F5),
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CAF84),
    secondary = WorldCupGold,
    background = DarkBackground,
    surface = Color(0xFF16213E)
)

@Composable
fun WC2026Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
