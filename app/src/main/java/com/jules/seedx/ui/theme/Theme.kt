package com.jules.seedx.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Solarized Dark colors
val SolarizedBase03 = Color(0xFF002b36)
val SolarizedBase02 = Color(0xFF073642)
val SolarizedBase01 = Color(0xFF586e75)
val SolarizedBase00 = Color(0xFF657b83)
val SolarizedBase0 = Color(0xFF839496)
val SolarizedBase1 = Color(0xFF93a1a1)
val SolarizedBase2 = Color(0xFFeee8d5)
val SolarizedBase3 = Color(0xFFfdf6e3)
val SolarizedYellow = Color(0xFFb58900)
val SolarizedOrange = Color(0xFFcb4b16)
val SolarizedRed = Color(0xFFdc322f)
val SolarizedMagenta = Color(0xFFd33682)
val SolarizedViolet = Color(0xFF6c71c4)
val SolarizedBlue = Color(0xFF268bd2)
val SolarizedCyan = Color(0xFF2aa198)
val SolarizedGreen = Color(0xFF859900)

private val ColorScheme = darkColorScheme(
    primary = SolarizedBlue,
    onPrimary = SolarizedBase3,
    primaryContainer = SolarizedBase02,
    onPrimaryContainer = SolarizedBase1,
    secondary = SolarizedCyan,
    onSecondary = SolarizedBase3,
    background = SolarizedBase03,
    onBackground = SolarizedBase0,
    surface = SolarizedBase02,
    onSurface = SolarizedBase1,
    error = SolarizedRed,
    onError = SolarizedBase3
)

@Composable
fun SeedXTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ColorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}
