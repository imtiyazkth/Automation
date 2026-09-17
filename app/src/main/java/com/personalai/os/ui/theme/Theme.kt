package com.personalai.os.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = OsPrimary,
    onPrimary = OsOnSurface,
    secondary = OsAccent,
    background = OsBackground,
    surface = OsSurface,
    surfaceVariant = OsSurfaceVariant,
    onSurface = OsOnSurface,
    onSurfaceVariant = OsOnSurfaceMuted,
    outline = OsOutline,
    error = OsDanger
)

private val AutomationOsShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AutomationOsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = AutomationOsTypography,
        shapes = AutomationOsShapes,
        content = content
    )
}
