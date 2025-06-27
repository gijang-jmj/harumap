package com.jmj.harumap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5EC8E5),
    onPrimary = Color.Black,
    background = Color(0xFF121212),  // 머티리얼 다크 기본
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    error = Color(0xFFF97066),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF5EC8E5),     // 민트 블루
    onPrimary = Color.White,
    background = Color(0xFFF9FAFB),  // 연한 회백색
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFF97066),       // 소프트 레드
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