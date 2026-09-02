package com.personalai.os.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = OsPrimary,
    secondary = OsAccent,
    background = OsBackground,
    surface = OsSurface,
    onSurface = OsOnSurface,
    error = OsDanger
)

@Composable
fun AutomationOsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
