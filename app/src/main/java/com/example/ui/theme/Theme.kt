package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ObsidianColorScheme = darkColorScheme(
    primary = NeonIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = ElectricCyan,
    onSecondary = Color(0xFF08090D),
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    error = CrimsonWarning,
    onError = Color.White,
    background = ObsidianBackground,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceHighlight,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianOutline,
    outlineVariant = Color(0xFF2E384D)
)

val CleanLightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = LightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECFEFF),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = EmeraldSuccess,
    onTertiary = Color.White,
    error = CrimsonWarning,
    onError = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = LightOutline,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun PDFOmniTheme(
    darkTheme: Boolean = true, // Default Obsidian Dark Mode as mandated
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ObsidianColorScheme else CleanLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PDFOmniTheme(darkTheme = darkTheme, content = content)
}
