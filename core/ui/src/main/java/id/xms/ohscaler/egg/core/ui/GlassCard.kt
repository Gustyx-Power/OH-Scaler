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
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.SurfaceDark
import id.xms.ohscaler.egg.core.designsystem.SurfaceVariantDark

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = BorderDark,
    borderWidth: Dp = 1.dp,
    backgroundBrush: Brush? = null,
    backgroundColor: Color = SurfaceDark,
    contentPadding: Dp = 16.dp,
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

    val bgModifier = if (backgroundBrush != null) {
        Modifier.background(backgroundBrush)
    } else {
        Modifier.background(backgroundColor)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(clickableModifier)
            .then(bgModifier)
            .border(borderWidth, borderColor, shape)
            .padding(contentPadding),
        content = content
    )
}
