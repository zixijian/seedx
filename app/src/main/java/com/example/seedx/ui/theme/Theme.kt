package com.example.seedx.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SolarizedBlue,
    secondary = SolarizedCyan,
    tertiary = SolarizedGreen,
    background = SolarizedBase03,
    surface = SolarizedBase02,
    onPrimary = SolarizedBase3,
    onSecondary = SolarizedBase3,
    onTertiary = SolarizedBase3,
    onBackground = SolarizedBase0,
    onSurface = SolarizedBase1,
)

// We focus on Solarized Dark as requested
private val LightColorScheme = DarkColorScheme

@Composable
fun SeedXTheme(
    darkTheme: Boolean = true, // Force dark theme as requested
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
