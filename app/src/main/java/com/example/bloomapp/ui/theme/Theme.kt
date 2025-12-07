package com.example.bloomapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Dark mode colors
private val DarkColorScheme = darkColorScheme(
    primary = green,
    onPrimary = black,
    secondary = grey,
    onSecondary = black,
    tertiary = Pink80,
    onTertiary = black,
    background = Color(0xFF121212),
    onBackground = grey,
    surface = Color(0xFF1E1E1E),
    onSurface = grey
)

// Light mode colors
private val LightColorScheme = lightColorScheme(
    primary = green,
    onPrimary = black,
    secondary = grey,
    onSecondary = black,
    tertiary = Pink80,
    onTertiary = black,
    background = Color(0xFFFFFFFF),
    onBackground = black,
    surface = grey,
    onSurface = black
)

@Composable
fun BloomAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Android 12+ dynamic color
    content: @Composable () -> Unit
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
