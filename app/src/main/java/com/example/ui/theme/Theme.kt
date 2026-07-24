package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanLiquid,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0284C7),
    onPrimaryContainer = Color.White,
    secondary = IndigoLiquid,
    onSecondary = Color.White,
    background = GlassBackgroundDark,
    onBackground = GlassTextPrimary,
    surface = Color(0xFF0F172A),
    onSurface = GlassTextPrimary,
    surfaceVariant = GlassSurfaceDark,
    onSurfaceVariant = GlassTextSecondary,
    outline = GlassSurfaceBorder
)

private val LightColorScheme = darkColorScheme(
    primary = CyanLiquid,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF0284C7),
    onPrimaryContainer = Color.White,
    secondary = IndigoLiquid,
    onSecondary = Color.White,
    background = GlassBackgroundDark,
    onBackground = GlassTextPrimary,
    surface = Color(0xFF0F172A),
    onSurface = GlassTextPrimary,
    surfaceVariant = GlassSurfaceDark,
    onSurfaceVariant = GlassTextSecondary,
    outline = GlassSurfaceBorder
)

@Composable
fun ScreenAwakeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    ScreenAwakeTheme(darkTheme = darkTheme, content = content)
}
