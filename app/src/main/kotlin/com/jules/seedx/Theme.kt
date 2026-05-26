package com.jules.seedx

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SolarizedDarkBackground = Color(0xFF002B36)
val SolarizedDarkSurface = Color(0xFF073642)
val SolarizedDarkPrimary = Color(0xFF268BD2)
val SolarizedDarkSecondary = Color(0xFF2AA198)
val SolarizedDarkText = Color(0xFF839496)
val SolarizedDarkOnPrimary = Color(0xFFEEE8D5)

private val DarkColorScheme = darkColorScheme(
    primary = SolarizedDarkPrimary,
    secondary = SolarizedDarkSecondary,
    background = SolarizedDarkBackground,
    surface = SolarizedDarkSurface,
    onPrimary = SolarizedDarkOnPrimary,
    onBackground = SolarizedDarkText,
    onSurface = SolarizedDarkText
)

@Composable
fun SeedXTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
