package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Force solid elegant dark obsidian scheme across all scenarios to guarantee eye-level comfort.
private val SadaDarkColorScheme = darkColorScheme(
    primary = RealNeonTeal,
    secondary = EmeraldGreen,
    tertiary = DeepForestGlow,
    background = DarkObsidianBg,
    surface = ObsidianSurface,
    onBackground = DimWhite,
    onSurface = DimWhite,
    primaryContainer = DeepForestGlow,
    onPrimaryContainer = RealNeonTeal,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = SoftCyberGray,
    outline = MutedSlate
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // We override defaults to lock in the ultra-premium comfortable Dark Eye-Safe scheme (Emerald Obsidian)
    MaterialTheme(
        colorScheme = SadaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
