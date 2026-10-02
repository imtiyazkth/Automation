package com.personalai.os.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

/** Radii follow hierarchy: big surfaces are rounder than small controls. */
object AppShapes {
    val card = RoundedCornerShape(20.dp)
    val control = RoundedCornerShape(14.dp)
    val inset = RoundedCornerShape(10.dp)
}

object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}

private val AutomationOsShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AutomationOsTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val c = if (darkTheme) DarkAppColors else LightAppColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = c.accent, onPrimary = c.onAccent, secondary = c.accent,
            background = c.background, onBackground = c.text,
            surface = c.card, onSurface = c.text,
            surfaceVariant = c.raised, onSurfaceVariant = c.textMuted,
            outline = c.hairline, outlineVariant = c.hairline, error = c.danger
        )
    } else {
        lightColorScheme(
            primary = c.accent, onPrimary = c.onAccent, secondary = c.accent,
            background = c.background, onBackground = c.text,
            surface = c.card, onSurface = c.text,
            surfaceVariant = c.raised, onSurfaceVariant = c.textMuted,
            outline = c.hairline, outlineVariant = c.hairline, error = c.danger
        )
    }
    CompositionLocalProvider(LocalAppColors provides c) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AutomationOsTypography,
            shapes = AutomationOsShapes,
            content = content
        )
    }
}
