package id.xms.ohscaler.egg.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Android 17 / Expressive Pill Progress Slider.
 * Capsule-shaped track with filled progress pill, displaying label and percentage cleanly.
 */
@Composable
fun PillProgressSlider(
    value: Float, // 0f..1f
    onValueChange: (Float) -> Unit,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current

    val trackBg = if (isDarkTheme) Color(0x24FFFFFF) else Color(0x14334155)
    val trackBorder = if (isDarkTheme) Color(0x1AFFFFFF) else Color(0x1F334155)
    val textBaseColor = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val pct = (value * 100f).roundToInt()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(CircleShape)
            .background(trackBg)
            .border(1.dp, trackBorder, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onValueChange(fraction)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                    onValueChange(fraction)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidth = maxWidth

        // Active Progress Pill
        Box(
            modifier = Modifier
                .width(totalWidth * value.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(CircleShape)
                .background(accentColor.copy(alpha = if (isDarkTheme) 0.85f else 0.75f))
        )

        // Labels inside track: Label on left, percentage on right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textBaseColor
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "$pct%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textBaseColor
                )
            }
        }
    }
}
