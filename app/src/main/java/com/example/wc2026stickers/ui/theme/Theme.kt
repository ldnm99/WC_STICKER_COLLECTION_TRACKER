package com.wc2026stickers.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WorldCupGreen = Color(0xFF00843D)
private val WorldCupGold = Color(0xFFFCC200)

private val LightColors = lightColorScheme(
    primary = WorldCupGreen,
    onPrimary = Color.White,
    secondary = WorldCupGold,
    onSecondary = Color(0xFF1A1A1A),
    tertiary = Color(0xFF005C2B),
    onTertiary = Color.White,
    background = Color(0xFFF5F5F5),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4CAF84),
    onPrimary = Color(0xFF003319),
    secondary = WorldCupGold,
    onSecondary = Color(0xFF1A1A1A),
    tertiary = Color(0xFF80E8AE),
    onTertiary = Color(0xFF003319),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
)

@Composable
fun WC2026Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}
