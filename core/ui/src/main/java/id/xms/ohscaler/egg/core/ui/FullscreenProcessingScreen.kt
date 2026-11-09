package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSecondary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FullscreenProcessingScreen(
    progress: UpscaleProgress,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current

    // Live Elapsed Time Stopwatch
    var elapsedMillis by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = System.currentTimeMillis()
        while (true) {
            elapsedMillis = System.currentTimeMillis() - start
            delay(100)
        }
    }

    val elapsedSeconds = elapsedMillis / 1000f
    val elapsedFormatted = String.format(Locale.US, "%02d:%02ds", (elapsedMillis / 1000) / 60, (elapsedMillis / 1000) % 60)

    // Calculate tile numbers, progress fraction, and remaining time estimation
    val (currentTile, totalTiles, progressFraction) = when (progress) {
        is UpscaleProgress.ReadingPixels -> Triple(0, 0, 0.05f)
        is UpscaleProgress.Processing -> {
            val total = progress.totalTiles.coerceAtLeast(1)
            val current = progress.currentTile.coerceIn(0, total)
            Triple(current, total, current.toFloat() / total.toFloat())
        }
        is UpscaleProgress.Stitching -> Triple(1, 1, 0.95f)
        is UpscaleProgress.Completed -> Triple(1, 1, 1.0f)
        else -> Triple(0, 0, 0f)
    }

    val estimatedRemainingText = if (currentTile > 1 && totalTiles > currentTile) {
        val avgTimePerTile = elapsedSeconds / currentTile
        val remainingTiles = totalTiles - currentTile
        val estSec = (remainingTiles * avgTimePerTile).roundToInt().coerceAtLeast(1)
        "Est. ~${estSec}s remaining"
    } else {
        "Optimizing on-device pipeline…"
    }

    // Infinite animations for pulsing rings and radar scanline
    val transition = rememberInfiniteTransition(label = "neuralPulse")
    val pulseScale1 by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse1"
    )
    val pulseScale2 by transition.animateFloat(
        initialValue = 1.15f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse2"
    )
    val radarAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "radar"
    )

    val primaryColor = DarkPrimary // Cyan #22D3EE
    val secondaryColor = DarkSecondary // Coral #F472B6
    val bgColor = Color(0xFF090C15) // Solid Obsidian

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ================================================================
            // 1. Top Section: Telemetry & Elapsed Stopwatch Pill
            // ================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x1F22D3EE))
                        .border(1.dp, Color(0x4022D3EE), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-ESRGAN Compact ONNX",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Live Stopwatch Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0x2BFFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = elapsedFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // ================================================================
            // 2. Center Section: Pulsing Neural Radar Animation Stage
            // ================================================================
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Multi-layer radar canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val baseRadius = size.width / 2.3f

                    // Outer faint orbit
                    drawCircle(
                        color = Color(0x2422D3EE),
                        radius = baseRadius * pulseScale1,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Secondary intersecting orbit
                    drawCircle(
                        color = Color(0x24F472B6),
                        radius = baseRadius * pulseScale2 * 0.9f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Rotating Cyber Scanline Sweep
                    val rad = Math.toRadians(radarAngle.toDouble())
                    val endX = center.x + (baseRadius * 0.95f) * cos(rad).toFloat()
                    val endY = center.y + (baseRadius * 0.95f) * sin(rad).toFloat()

                    drawLine(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor, Color.Transparent),
                            center = center,
                            radius = baseRadius
                        ),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Inner Glowing Sphere
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.35f), Color(0x00000000))
                            )
                        )
                        .border(1.5.dp, primaryColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            // ================================================================
            // 3. Lower Section: Live Metrics, Progress Bar, Step Status
            // ================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title and Estimated Remaining
                Text(
                    text = if (totalTiles > 0) "Neural AI Processing" else "Preparing Model",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = estimatedRemainingText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Linear Gradient Progress Bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = primaryColor,
                    trackColor = Color(0x26FFFFFF),
                )

                // Tile Counter & Percentage Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (totalTiles > 0) "Tile $currentTile of $totalTiles" else "Analyzing image structure…",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progressFraction * 100).roundToInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Hardware Protection Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1A1E293B))
                        .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Single-Tile Stream • Zero-OOM RAM Protection",
                        fontSize = 11.5.sp,
                        color = Color(0xFFE2E8F0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ================================================================
            // 4. Bottom Section: Cancel Processing Pill Button
            // ================================================================
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x24FFFFFF))
                    .border(1.dp, Color(0x40FFFFFF), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onCancel()
                        }
                    )
                    .padding(horizontal = 24.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Cancel Processing",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}
