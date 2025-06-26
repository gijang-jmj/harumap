package com.jmj.harumap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7F56D9), // Deep purple
    onPrimary = Color.White,
    secondary = Color(0xFF9E77ED), // Soft purple
    onSecondary = Color.White,
    tertiary = Color(0xFF7F56D9), // Accent purple
    onTertiary = Color.White,
    background = Color(0xFF18181B), // Near black
    onBackground = Color(0xFFF4F4F5), // Light gray
    surface = Color(0xFF232326), // Slightly lighter than background
    onSurface = Color(0xFFF4F4F5),
    error = Color(0xFFF97066), // Soft red
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7F56D9), // Deep purple
    onPrimary = Color.White,
    secondary = Color(0xFF9E77ED), // Soft purple
    onSecondary = Color.White,
    tertiary = Color(0xFF7F56D9), // Accent purple
    onTertiary = Color.White,
    background = Color(0xFFF4F4F5), // Very light gray
    onBackground = Color(0xFF18181B), // Near black
    surface = Color(0xFFFFFFFF), // Pure white
    onSurface = Color(0xFF18181B),
    error = Color(0xFFF97066), // Soft red
    onError = Color.White
)

@Composable
fun HarumapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}