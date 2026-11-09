package id.xms.ohscaler.egg.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface

/**
 * Android 17 Glass Card component.
 * Features a semi-translucent frosted glass fill, optional AGSL grain micro-texture, specular rim lighting border, and ripple feedback.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = DarkGlassBorder,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    backgroundBrush: Brush? = null,
    backgroundColor: Color = DarkGlassSurface,
    contentPadding: Dp = 16.dp,
    enableGrain: Boolean = false,
    grainIntensity: Float = 0.035f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = Color.White.copy(alpha = 0.15f)),
            onClick = onClick
        )
    } else {
        Modifier
    }

    val grainBrush = if (enableGrain && backgroundBrush == null) {
        rememberAndroid17GrainBrush(backgroundColor, grainIntensity)
    } else {
        null
    }

    val bgModifier = when {
        backgroundBrush != null -> Modifier.background(backgroundBrush)
        grainBrush != null -> Modifier.background(grainBrush)
        else -> Modifier.background(backgroundColor)
    }

    val borderModifier = if (borderBrush != null) {
        Modifier.border(borderWidth, borderBrush, shape)
    } else {
        Modifier.border(borderWidth, borderColor, shape)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(clickableModifier)
            .then(bgModifier)
            .then(borderModifier)
            .padding(contentPadding),
        content = content
    )
}
