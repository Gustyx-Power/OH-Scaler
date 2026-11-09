package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary

/**
 * Android 17 style segmented capsule scale selector with dual-theme frosted glass.
 */
@Composable
fun SegmentedScaleSelector(
    selectedScale: Float,
    onScaleSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
    scales: List<Float> = listOf(1.5f, 2.0f, 3.0f, 4.0f, 8.0f),
    labels: List<String> = listOf("1.5×", "2×", "3×", "4×", "8×"),
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current

    val containerBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val containerBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val activePillBg = if (isDarkTheme) DarkPrimary else LightPrimary
    val activeTextColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val inactiveTextColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(containerBg)
            .border(1.dp, containerBorder, CircleShape)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            scales.zip(labels).forEach { (scale, label) ->
                val isSelected = selectedScale == scale

                val animBgColor by animateColorAsState(
                    targetValue = if (isSelected) activePillBg else Color.Transparent,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "scaleBg"
                )

                val animTextColor by animateColorAsState(
                    targetValue = if (isSelected) activeTextColor else inactiveTextColor,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "scaleText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(CircleShape)
                        .background(animBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                            onClick = {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onScaleSelected(scale)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = animTextColor
                    )
                }
            }
        }
    }
}
