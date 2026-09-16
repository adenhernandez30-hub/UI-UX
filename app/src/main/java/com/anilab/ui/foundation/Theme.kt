package com.anilab.ui.foundation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AniLabDarkColors = darkColorScheme(
    primary = Color(0xFF9C8CFF),
    onPrimary = Color(0xFF241A52),
    primaryContainer = Color(0xFF3A2F67),
    onPrimaryContainer = Color(0xFFE9E3FF),
    background = Color(0xFF0D0D10),
    surface = Color(0xFF131318),
    surfaceContainer = Color(0xFF1A1A20),
    onBackground = Color(0xFFF1EFF4),
    onSurface = Color(0xFFF1EFF4),
    onSurfaceVariant = Color(0xFFBDB9C4)
)

@Composable
fun AniLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AniLabDarkColors,
        content = content
    )
}
