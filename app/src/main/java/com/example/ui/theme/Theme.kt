package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TechDarkColorScheme = darkColorScheme(
    primary = TechCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = TechCyan,
    secondary = TechGreen,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005328),
    onSecondaryContainer = TechGreen,
    tertiary = TechAmber,
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF634000),
    onTertiaryContainer = TechAmber,
    background = TechDarkBackground,
    onBackground = TechTextPrimary,
    surface = TechDarkSurface,
    onSurface = TechTextPrimary,
    surfaceVariant = TechDarkSurfaceVariant,
    onSurfaceVariant = TechTextSecondary,
    outline = TechDarkBorder,
    outlineVariant = Color(0xFF1E293B),
    error = TechCrimson,
    onError = Color.White
)

private val TechLightColorScheme = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF006D34),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF75FA9D),
    onSecondaryContainer = Color(0xFF00210B),
    tertiary = Color(0xFF865300),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark hardware monitoring mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TechDarkColorScheme else TechLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
