package com.personalai.os.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Legacy constants, kept so nothing else in the project breaks.
val OsPrimary = Color(0xFF1B4B43)
val OsPrimaryVariant = Color(0xFF0F332D)
val OsAccent = Color(0xFF37E29A)
val OsBackground = Color(0xFF0B0F0E)
val OsSurface = Color(0xFF141918)
val OsSurfaceVariant = Color(0xFF1D2422)
val OsOnSurface = Color(0xFFE7F1EE)
val OsOnSurfaceMuted = Color(0xFF9FB3AE)
val OsOutline = Color(0xFF2C3634)
val OsWarning = Color(0xFFE2A637)
val OsDanger = Color(0xFFE24C37)
val OsUserBubble = Color(0xFF1F5A50)
val OsAiBubble = Color(0xFF1A2120)

/**
 * The design tokens the UI actually uses. One accent (mint) per view;
 * warning and danger are reserved for status, never decoration.
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    val background: Color,   // screen
    val card: Color,         // cards and grouped lists
    val raised: Color,       // inset areas on a card: quotes, segmented track, pills
    val hairline: Color,     // 1dp borders and dividers
    val text: Color,
    val textMuted: Color,
    val accent: Color,
    val onAccent: Color,
    val warning: Color,
    val danger: Color
)

val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF0C0F0E),
    card = Color(0xFF151A19),
    raised = Color(0xFF1E2524),
    hairline = Color(0xFF26302E),
    text = Color(0xFFEDF3F1),
    textMuted = Color(0xFF93A6A1),
    accent = Color(0xFF3DDC97),
    onAccent = Color(0xFF04130D),
    warning = Color(0xFFE5A93B),
    danger = Color(0xFFEF6B5A)
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF3F5F4),
    card = Color(0xFFFFFFFF),
    raised = Color(0xFFECF0EF),
    hairline = Color(0xFFDFE5E3),
    text = Color(0xFF101413),
    textMuted = Color(0xFF5B6965),
    accent = Color(0xFF0B7A50),
    onAccent = Color(0xFFFFFFFF),
    warning = Color(0xFF9A6200),
    danger = Color(0xFFC23B2B)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
