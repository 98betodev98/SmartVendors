package com.example.smartvendors.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SmartVendorsPrimary,
    secondary = SmartVendorsSecondary,
    background = SmartVendorsBackground,
    surface = SmartVendorsBackground,

    onPrimary = Color.White,
    onSecondary = SmartVendorsText,
    onBackground = SmartVendorsText,
    onSurface = SmartVendorsText,

    error = SmartVendorsError
)

private val DarkColorScheme = darkColorScheme(
    primary = SmartVendorsSecondary,
    secondary = SmartVendorsPrimary,
    error = SmartVendorsError
)

@Composable
fun SmartVendorsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}