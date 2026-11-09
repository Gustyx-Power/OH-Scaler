package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Slide-to-Action Pill Slider modeled after Photo 2 reference ("滑动以格式化").
 * Capsule track with sliding pill thumb containing double chevrons (`>>`).
 * Sliding to the right triggers the action with smooth spring physics.
 */
@Composable
fun SlideToProcessButton(
    text: String,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    val trackBg = if (isDarkTheme) Color(0xFF222530) else Color(0xFFE2E8F0)
    val trackBorder = if (isDarkTheme) Color(0x2BFFFFFF) else Color(0x2B334155)
    val thumbBg = if (isDarkTheme) Color(0xFF2563EB) else Color(0xFF355375) // Electric Blue / Slate Blue
    val textColor = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF475569)

    val thumbWidth = 72.dp
    val thumbHeight = 42.dp
    val trackHeight = 52.dp
    val margin = 5.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
            .clip(CircleShape)
            .background(trackBg)
            .border(1.2.dp, trackBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onConfirmed()
                }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val thumbWidthPx = with(density) { thumbWidth.toPx() }
        val marginPx = with(density) { margin.toPx() }
        val maxOffsetPx = (totalWidthPx - thumbWidthPx - marginPx * 2).coerceAtLeast(0f)

        val offsetX = remember { Animatable(0f) }

        val dragFraction = if (maxOffsetPx > 0f) (offsetX.value / maxOffsetPx).coerceIn(0f, 1f) else 0f
        val textAlpha = (1f - (dragFraction * 1.5f)).coerceIn(0f, 1f)

        // Centered Action Text (fades out as thumb approaches right)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = textAlpha),
                letterSpacing = 0.3.sp
            )
        }

        // Draggable Pill Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset((marginPx + offsetX.value).roundToInt(), 0) }
                .size(width = thumbWidth, height = thumbHeight)
                .clip(CircleShape)
                .background(thumbBg)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val target = (offsetX.value + delta).coerceIn(0f, maxOffsetPx)
                            offsetX.snapTo(target)
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX.value >= maxOffsetPx * 0.75f) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                offsetX.animateTo(maxOffsetPx, spring(dampingRatio = 0.8f, stiffness = 600f))
                                onConfirmed()
                                offsetX.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 400f))
                            } else {
                                offsetX.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = 500f))
                            }
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Double Chevron `>>` matching Photo 2 reference
            Canvas(modifier = Modifier.size(16.dp)) {
                val strokeWidth = 2.4.dp.toPx()
                val color = Color.White
                val w = size.width
                val h = size.height

                // Chevron 1
                drawLine(
                    color = color,
                    start = Offset(w * 0.12f, h * 0.22f),
                    end = Offset(w * 0.45f, h * 0.5f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.45f, h * 0.5f),
                    end = Offset(w * 0.12f, h * 0.78f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                // Chevron 2
                drawLine(
                    color = color,
                    start = Offset(w * 0.55f, h * 0.22f),
                    end = Offset(w * 0.88f, h * 0.5f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(w * 0.88f, h * 0.5f),
                    end = Offset(w * 0.55f, h * 0.78f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
