package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OnyxDarkColorScheme = darkColorScheme(
    primary = OnyxRedPrimary,
    onPrimary = OnyxWhite,
    primaryContainer = OnyxRedContainer,
    onPrimaryContainer = OnyxWhite,
    secondary = OnyxRedVibrant,
    onSecondary = OnyxWhite,
    background = OnyxDarkBg,
    onBackground = OnyxWhite,
    surface = OnyxDarkSurface,
    onSurface = OnyxWhite,
    surfaceVariant = OnyxDarkSurfaceVariant,
    onSurfaceVariant = OnyxWhiteMuted,
    outline = OnyxBorderDark,
    error = Color(0xFFFF5252),
    onError = Color.White
)

private val OnyxLightColorScheme = lightColorScheme(
    primary = OnyxRedDeep,
    onPrimary = OnyxWhite,
    primaryContainer = Color(0xFFFFEBEF),
    onPrimaryContainer = OnyxRedDeep,
    secondary = OnyxRedPrimary,
    onSecondary = OnyxWhite,
    background = OnyxLightBg,
    onBackground = OnyxLightTextPrimary,
    surface = OnyxLightSurface,
    onSurface = OnyxLightTextPrimary,
    surfaceVariant = OnyxLightSurfaceVariant,
    onSurfaceVariant = OnyxLightTextSecondary,
    outline = OnyxBorderLight,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep Onyx branding red & white intact
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) OnyxDarkColorScheme else OnyxLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
