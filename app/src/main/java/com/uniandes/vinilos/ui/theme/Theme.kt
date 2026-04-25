package com.uniandes.vinilos.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RedAccent,
    secondary = DarkGrayText,
    tertiary = GrayMedium,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = WhiteSurface,
    onBackground = WhiteSurface,
    onSurface = WhiteSurface
)

private val LightColorScheme = lightColorScheme(
    primary = RedAccent,
    secondary = GrayMedium,
    tertiary = GrayLight,
    background = Cream,
    surface = WhiteSurface,
    onPrimary = WhiteSurface,
    onBackground = BlackPrimary,
    onSurface = BlackPrimary
)

@Composable
fun VinilosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // ← desactivado
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