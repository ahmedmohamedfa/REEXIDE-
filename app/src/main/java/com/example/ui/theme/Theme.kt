package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.Black,
    primaryContainer = ElectricAccent,
    onPrimaryContainer = Color.White,
    secondary = CyanSecondary,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = AccentGreen,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color(0xFFE6EDF3),
    surface = DarkSurface,
    onSurface = Color(0xFFE6EDF3),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF8B949E),
    outline = DarkBorder,
    error = AccentRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDF4FF),
    onPrimaryContainer = Color(0xFF0969DA),
    secondary = Color(0xFF0550AE),
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = Color(0xFF1F2328),
    surface = LightSurface,
    onSurface = Color(0xFF1F2328),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF57606A),
    outline = LightBorder,
    error = Color(0xFFCF222E),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to IDE dark mode for code readability
    dynamicColor: Boolean = false, // Preserve crisp IDE obsidian branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
