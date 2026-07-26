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
    primary = EmeraldAccent,
    onPrimary = DarkBluePrimary,
    primaryContainer = DarkBlueSecondary,
    onPrimaryContainer = Color.White,
    secondary = EmeraldLight,
    onSecondary = DarkBluePrimary,
    background = DarkBlueBackground,
    onBackground = Color.White,
    surface = DarkBlueSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = DarkBluePrimary,
    secondary = EmeraldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xD1D1FAE5),
    onSecondaryContainer = EmeraldDark,
    tertiary = EmeraldAccent,
    background = LightBackground,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = CardBorderLight,
    error = ErrorRed
)

@Composable
fun SoftKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to prioritize brand dark blue/emerald colors
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

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
    SoftKeeperTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
