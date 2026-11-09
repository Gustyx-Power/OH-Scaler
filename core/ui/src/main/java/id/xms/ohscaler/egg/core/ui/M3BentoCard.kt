package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.SpecularEdgeBottom
import id.xms.ohscaler.egg.core.designsystem.SpecularEdgeTop
import id.xms.ohscaler.egg.core.designsystem.SurfaceVariantDark

/**
 * Material 3 Expressive Bento Card Component (https://m3.material.io/components/cards)
 * Built with tonal elevation, specular edge lighting, and tactile press physics for a modern vibecoding aesthetic.
 */
@Composable
fun M3BentoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = SurfaceVariantDark,
    backgroundBrush: Brush? = null,
    borderBrush: Brush = Brush.verticalGradient(
        listOf(SpecularEdgeTop, SpecularEdgeBottom, BorderDark)
    ),
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile press spring animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "bentoCardPress"
    )

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(color = Color.White.copy(alpha = 0.18f)),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
    } else {
        Modifier
    }

    val bgModifier = when {
        glowColor != null -> Modifier.background(
            Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = 0.14f), backgroundColor),
                radius = 500f
            )
        )
        backgroundBrush != null -> Modifier.background(backgroundBrush)
        else -> Modifier.background(backgroundColor)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .then(bgModifier)
            .border(borderWidth, borderBrush, shape)
            .then(clickableModifier)
            .padding(contentPadding),
        content = content
    )
}
