package com.smartdrivo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.smartdrivo.app.viewmodel.ThemeMode

val DarkBackground = Color(0xFF000000)
val LightBackground = Color(0xFFFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00C853),
    onPrimary = Color.White,
    secondary = Color(0xFFFF6D00),
    onSecondary = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surface = Color(0xFF0D0D0D),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFAAAAAA),
    error = Color(0xFFD50000),
    onError = Color.White,
    outline = Color(0xFF444444)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00C853),
    onPrimary = Color.White,
    secondary = Color(0xFFFF6D00),
    onSecondary = Color.White,
    background = Color(0xFFFFFFFF),
    onBackground = Color.White,
    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF555555),
    error = Color(0xFFD50000),
    onError = Color.White,
    outline = Color(0xFFCCCCCC)
)

@Composable
fun SmartDrivoTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.SYSTEM -> if (isSystemInDarkTheme()) 
            DarkColorScheme else LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
