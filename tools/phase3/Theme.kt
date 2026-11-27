package com.lomipe.devhub.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Night = darkColorScheme(
    primary = Color(0xFF79C0FF), onPrimary = Color(0xFF0D1117),
    primaryContainer = Color(0xFF1F6FEB), onPrimaryContainer = Color.White,
    secondary = Color(0xFF3FB950), onSecondary = Color(0xFF0D1117),
    secondaryContainer = Color(0xFF1C3326), onSecondaryContainer = Color(0xFF7EE787),
    tertiary = Color(0xFFD29922), background = Color(0xFF0D1117),
    onBackground = Color(0xFFE6EDF3), surface = Color(0xFF161B22),
    onSurface = Color(0xFFE6EDF3), surfaceVariant = Color(0xFF21262D),
    onSurfaceVariant = Color(0xFF9EAAB7), outline = Color(0xFF484F58),
    outlineVariant = Color(0xFF30363D), error = Color(0xFFFF7B72),
    surfaceContainer = Color(0xFF161B22), surfaceContainerLow = Color(0xFF12171E),
    surfaceContainerHigh = Color(0xFF21262D)
)
private val Day = lightColorScheme(
    primary = Color(0xFF0969DA), onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF4FF), onPrimaryContainer = Color(0xFF0550AE),
    secondary = Color(0xFF1A7F37), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDAFBE1), onSecondaryContainer = Color(0xFF116329),
    tertiary = Color(0xFF9A6700), background = Color(0xFFF6F8FA),
    onBackground = Color(0xFF1F2328), surface = Color.White,
    onSurface = Color(0xFF1F2328), surfaceVariant = Color(0xFFEAEFF4),
    onSurfaceVariant = Color(0xFF59636E), outline = Color(0xFF8C959F),
    outlineVariant = Color(0xFFD0D7DE), error = Color(0xFFCF222E),
    surfaceContainer = Color.White, surfaceContainerLow = Color(0xFFF6F8FA),
    surfaceContainerHigh = Color(0xFFEAEFF4)
)
@Composable
fun LomipeTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    MaterialTheme(colorScheme = if (dark) Night else Day,
        shapes = Shapes(extraSmall = RoundedCornerShape(4.dp), small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp)), content = content)
}
