package id.xms.ohscaler.egg.core.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.CyanAccent
import id.xms.ohscaler.egg.core.designsystem.ElectricViolet
import id.xms.ohscaler.egg.core.designsystem.GlassOverlay
import id.xms.ohscaler.egg.core.designsystem.ObsidianBlack
import id.xms.ohscaler.egg.core.designsystem.TextMuted
import id.xms.ohscaler.egg.core.designsystem.TextPrimary
import kotlin.math.roundToInt

@Composable
fun ComparisonSlider(
    beforeBitmap: Bitmap,
    afterBitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var sliderRatio by remember { mutableFloatStateOf(0.5f) }
    var previousCrossingMidpoint by remember { mutableStateOf(false) }

    // Synchronized Zoom & Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset = if (scale > 1f) offset + offsetChange else Offset.Zero
    }

    val beforeImageBitmap = remember(beforeBitmap) { beforeBitmap.asImageBitmap() }
    val afterImageBitmap = remember(afterBitmap) { afterBitmap.asImageBitmap() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .transformable(state = transformState)
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        val dividerX = (sliderRatio * containerWidth).coerceIn(0f, containerWidth)

        // Dual Canvas Rendering with Hardware-Accelerated Path Clipping
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate destination rect to maintain aspect ratio (CenterCrop / Fit)
            val imgAspect = afterBitmap.width.toFloat() / afterBitmap.height.toFloat()
            val canvasAspect = canvasWidth / canvasHeight

            val dstWidth: Float
            val dstHeight: Float
            val dstX: Float
            val dstY: Float

            if (imgAspect > canvasAspect) {
                dstWidth = canvasWidth
                dstHeight = canvasWidth / imgAspect
                dstX = 0f
                dstY = (canvasHeight - dstHeight) / 2f
            } else {
                dstHeight = canvasHeight
                dstWidth = canvasHeight * imgAspect
                dstX = (canvasWidth - dstWidth) / 2f
                dstY = 0f
            }

            val imgDstSize = Size(dstWidth, dstHeight)
            val imgDstOffset = Offset(dstX, dstY)

            // 1. Draw "Before" Image on the left half (clipped to [0, dividerX])
            val leftClipPath = Path().apply {
                addRect(Rect(0f, 0f, dividerX, canvasHeight))
            }
            clipPath(leftClipPath) {
                drawImage(
                    image = beforeImageBitmap,
                    dstOffset = IntOffset(imgDstOffset.x.roundToInt(), imgDstOffset.y.roundToInt()),
                    dstSize = androidx.compose.ui.unit.IntSize(imgDstSize.width.roundToInt(), imgDstSize.height.roundToInt())
                )
            }

            // 2. Draw "After" Image on the right half (clipped to [dividerX, canvasWidth])
            val rightClipPath = Path().apply {
                addRect(Rect(dividerX, 0f, canvasWidth, canvasHeight))
            }
            clipPath(rightClipPath) {
                drawImage(
                    image = afterImageBitmap,
                    dstOffset = IntOffset(imgDstOffset.x.roundToInt(), imgDstOffset.y.roundToInt()),
                    dstSize = androidx.compose.ui.unit.IntSize(imgDstSize.width.roundToInt(), imgDstSize.height.roundToInt())
                )
            }

            // 3. Draw Divider Line
            drawLine(
                color = ElectricViolet,
                start = Offset(dividerX, 0f),
                end = Offset(dividerX, canvasHeight),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Floating Tags: "BEFORE" on left, "AFTER" on right
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GlassOverlay)
                .border(1.dp, BorderDark, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tag_before),
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GlassOverlay)
                .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tag_after),
                color = CyanAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Interactive Handle (48dp touch target)
        val handleSize = 48.dp
        val handleSizePx = with(density) { handleSize.toPx() }
        val handleOffsetX = (dividerX - (handleSizePx / 2f)).coerceIn(
            0f,
            containerWidth - handleSizePx
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(handleOffsetX.roundToInt(), (containerHeight / 2f - handleSizePx / 2f).roundToInt()) }
                .size(handleSize)
                .clip(CircleShape)
                .background(ObsidianBlack)
                .border(2.dp, ElectricViolet, CircleShape)
                .pointerInput(containerWidth) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        val newRatio = (sliderRatio + (dragAmount / containerWidth)).coerceIn(0.02f, 0.98f)

                        // Haptic tick feedback at center (50%)
                        val isMidpoint = (newRatio in 0.48f..0.52f)
                        if (isMidpoint && !previousCrossingMidpoint) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        previousCrossingMidpoint = isMidpoint
                        sliderRatio = newRatio
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.desc_drag_slider),
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
