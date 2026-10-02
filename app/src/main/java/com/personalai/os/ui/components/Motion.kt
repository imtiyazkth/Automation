package com.personalai.os.ui.components

import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** True when the user turned system animations off. Motion becomes instant; feedback stays. */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * Press feedback on touch-down, not release. Critically damped spring (no bounce):
 * a press carries no momentum, so it shouldn't overshoot. Only touches graphicsLayer.
 */
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f
): Modifier = composed {
    val reduced = rememberReducedMotion()
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduced) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 1200f),
        label = "pressScale"
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Clickable with press feedback instead of a ripple. */
fun Modifier.tappable(
    enabled: Boolean = true,
    role: Role = Role.Button,
    pressedScale: Float = 0.97f,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this
        .pressScale(interaction, pressedScale)
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = onClick
        )
}

/**
 * Fades the edges of a scrolling area so content dissolves under the header/tab bar
 * instead of being cut by a hard line. Give the list at least [top]/[bottom] of
 * content padding so nothing is faded at rest.
 */
fun Modifier.fadeEdges(top: Dp = 12.dp, bottom: Dp = 12.dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val t = top.toPx()
        val b = bottom.toPx()
        if (t > 0f) {
            drawRect(
                brush = Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black, startY = 0f, endY = t),
                size = Size(size.width, t),
                blendMode = BlendMode.DstIn
            )
        }
        if (b > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Black, 1f to Color.Transparent,
                    startY = size.height - b, endY = size.height
                ),
                topLeft = Offset(0f, size.height - b),
                size = Size(size.width, b),
                blendMode = BlendMode.DstIn
            )
        }
    }
