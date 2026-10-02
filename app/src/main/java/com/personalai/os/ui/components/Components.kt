package com.personalai.os.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.personalai.os.ui.theme.AppShapes
import com.personalai.os.ui.theme.AppTheme
import com.personalai.os.ui.theme.AppType
import kotlin.math.roundToInt

// ---------------------------------------------------------------- text helpers

/** "head-agent" -> "Head agent", "SUCCESS" -> "Success". */
fun String.humanize(): String =
    replace('_', ' ').replace('-', ' ').trim().lowercase()
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

fun permissionLabel(permission: String): String = when (permission) {
    "use_cloud_ai" -> "Use cloud AI"
    "access_files" -> "Access files"
    "access_hr_database" -> "Access HR database"
    else -> permission.humanize()
}

// ---------------------------------------------------------------------- cards

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = AppShapes.card,
    content: @Composable ColumnScope.() -> Unit
) {
    val c = AppTheme.colors
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.tappable(pressedScale = 0.985f, onClick = onClick) else Modifier)
            .clip(shape)
            .background(c.card)
            .border(1.dp, c.hairline, shape),
        content = content
    )
}

/** iOS-style inset group: one card, rows separated by hairlines. */
@Composable
fun GroupedCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.card)
            .background(c.card)
            .border(1.dp, c.hairline, AppShapes.card),
        content = content
    )
}

@Composable
fun RowDivider(startInset: androidx.compose.ui.unit.Dp = 16.dp) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = startInset)
            .height(1.dp)
            .background(AppTheme.colors.hairline)
    )
}

@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val c = AppTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick
                ) else Modifier
            )
            .background(if (pressed) c.raised else Color.Transparent)
            .heightIn(min = 60.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = c.text)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.textMuted, modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

// --------------------------------------------------------------------- header

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    val c = AppTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        if (onBack != null) {
            IconAction(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBack,
                modifier = Modifier.offset(x = (-12).dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = AppType.largeTitle, color = c.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = c.textMuted, modifier = Modifier.padding(top = 2.dp))
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            }
        }
    }
}

@Composable
fun IconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.colors.text,
    enabled: Boolean = true
) {
    Box(
        modifier.size(44.dp).tappable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp), tint = tint)
    }
}

// -------------------------------------------------------------------- buttons

enum class ButtonKind { Primary, Secondary, Destructive, Quiet }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: ButtonKind = ButtonKind.Primary,
    enabled: Boolean = true,
    compact: Boolean = false,
    icon: ImageVector? = null
) {
    val c = AppTheme.colors
    val container = when (kind) {
        ButtonKind.Primary -> if (enabled) c.accent else c.raised
        ButtonKind.Secondary -> c.raised
        ButtonKind.Destructive -> c.danger.copy(alpha = 0.14f)
        ButtonKind.Quiet -> Color.Transparent
    }
    val content = when (kind) {
        ButtonKind.Primary -> if (enabled) c.onAccent else c.textMuted.copy(alpha = 0.7f)
        ButtonKind.Secondary -> c.text
        ButtonKind.Destructive -> c.danger
        ButtonKind.Quiet -> c.textMuted
    }
    Box(
        modifier
            .heightIn(min = if (compact) 40.dp else 48.dp)
            .tappable(enabled = enabled, onClick = onClick)
            .clip(AppShapes.control)
            .background(container)
            .padding(horizontal = if (compact) 16.dp else 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = content)
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.titleSmall, color = content, maxLines = 1)
        }
    }
}

// ---------------------------------------------------------------------- pills

enum class Tone { Neutral, Accent, Warning, Danger }

@Composable
fun toneColor(tone: Tone): Color {
    val c = AppTheme.colors
    return when (tone) {
        Tone.Neutral -> c.textMuted
        Tone.Accent -> c.accent
        Tone.Warning -> c.warning
        Tone.Danger -> c.danger
    }
}

@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier, tone: Tone = Tone.Neutral, showDot: Boolean = false) {
    val color = toneColor(tone)
    Row(
        modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(Modifier.size(6.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

// ------------------------------------------------------------------ segmented

/** Sliding thumb, critically damped spring. Instant when animations are off. */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = AppTheme.colors
    val reduced = rememberReducedMotion()
    val index = options.indexOf(selected).coerceAtLeast(0)
    val thumbColor = if (c.isDark) Color(0xFF2E3836) else c.card

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(AppShapes.control)
            .background(c.raised)
            .padding(3.dp)
    ) {
        val segmentWidth = maxWidth / options.size
        val targetPx = with(LocalDensity.current) { (segmentWidth * index).toPx() }
        val spec: AnimationSpec<Float> = if (reduced) snap<Float>() else spring<Float>(dampingRatio = 1f, stiffness = 500f)
        val offsetPx by animateFloatAsState(targetPx, spec, label = "segmentThumb")

        Box(
            Modifier
                .offset { IntOffset(offsetPx.roundToInt(), 0) }
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(AppShapes.inset)
                .background(thumbColor)
        )
        Row(Modifier.fillMaxSize()) {
            options.forEach { option ->
                val isSelected = option == selected
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = isSelected,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.RadioButton,
                            onClick = { onSelect(option) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label(option),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) c.text else c.textMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------- empty state

/** An empty screen is an invitation: say what's missing and offer one next step. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val c = AppTheme.colors
    Column(
        modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(c.raised), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(26.dp), tint = c.textMuted)
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.text, modifier = Modifier.padding(top = 16.dp), textAlign = TextAlign.Center)
        Text(
            body, style = MaterialTheme.typography.bodyMedium, color = c.textMuted, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp).widthIn(max = 300.dp)
        )
        if (actionLabel != null && onAction != null) {
            AppButton(actionLabel, onAction, Modifier.padding(top = 20.dp), compact = true)
        }
    }
}

@Composable
fun SkeletonBlock(modifier: Modifier = Modifier) {
    Box(modifier.clip(AppShapes.card).background(AppTheme.colors.raised))
}

// ------------------------------------------------------------- inputs / switch

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false
) {
    val c = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        shape = AppShapes.control,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = c.text, unfocusedTextColor = c.text,
            focusedBorderColor = c.accent, unfocusedBorderColor = c.hairline,
            focusedLabelColor = c.accent, unfocusedLabelColor = c.textMuted,
            cursorColor = c.accent,
            focusedContainerColor = c.raised.copy(alpha = 0.5f),
            unfocusedContainerColor = Color.Transparent
        )
    )
}

@Composable
fun appSwitchColors(): SwitchColors {
    val c = AppTheme.colors
    return SwitchDefaults.colors(
        checkedThumbColor = c.onAccent, checkedTrackColor = c.accent, checkedBorderColor = Color.Transparent,
        uncheckedThumbColor = if (c.isDark) c.textMuted else Color.White,
        uncheckedTrackColor = c.raised, uncheckedBorderColor = c.hairline
    )
}
