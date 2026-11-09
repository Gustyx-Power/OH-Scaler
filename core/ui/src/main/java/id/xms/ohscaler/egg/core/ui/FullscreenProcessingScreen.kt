package id.xms.ohscaler.egg.core.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSecondary
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun FullscreenProcessingScreen(
    progress: UpscaleProgress,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    sourceWidth: Int = 0,
    sourceHeight: Int = 0,
    scaleFactor: Float = 2.0f,
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
        "Est. ~$estSec detik lagi"
    } else {
        "Menyiapkan pipeline AI on-device…"
    }

    // Resolusi Asli & Target
    val actualSrcW = if (sourceWidth > 0) sourceWidth else if (progress is UpscaleProgress.ReadingPixels) progress.sourceWidth else 496
    val actualSrcH = if (sourceHeight > 0) sourceHeight else if (progress is UpscaleProgress.ReadingPixels) progress.sourceHeight else 603
    val targetW = (actualSrcW * scaleFactor).roundToInt()
    val targetH = (actualSrcH * scaleFactor).roundToInt()
    val scaleLabel = if (scaleFactor % 1f == 0f) "${scaleFactor.toInt()}×" else "${scaleFactor}×"

    // Infinite animations for data flow and pulse glow
    val transition = rememberInfiniteTransition(label = "flowAnimation")
    val flowProgress1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "flowPulse1"
    )
    val flowProgress2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "flowPulse2"
    )
    val hardwareGlow by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hardwareGlow"
    )
    val shimmerOffset by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    val primaryColor = DarkPrimary // Cyan #22D3EE
    val secondaryColor = DarkSecondary // Coral #F472B6
    val bgColor = Color(0xFF090C15) // Solid Obsidian

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 18.dp)
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
            // 2. Center Section: Hierarchical Hardware & Data Flow Animation
            // ================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Node: Original Image (Foto Asli)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x261E293B))
                        .border(1.dp, Color(0x4D22D3EE), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2B22D3EE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = "Foto Asli",
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "FOTO ASLI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "$actualSrcW × $actualSrcH px",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x1F22D3EE))
                                .border(1.dp, Color(0x3322D3EE), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "RAW INPUT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }

                // Fluid Connector 1 (Top -> Middle Hardware Cluster)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(30.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val startX = w / 2f
                    val startY = 0f
                    val endY = h

                    // 4 target X coordinates corresponding to CPU, GPU, NPU, RAM centers
                    val xTargets = listOf(w * 0.125f, w * 0.375f, w * 0.625f, w * 0.875f)

                    xTargets.forEach { endX ->
                        val path = Path().apply {
                            moveTo(startX, startY)
                            cubicTo(
                                startX, startY + h * 0.5f,
                                endX, startY + h * 0.5f,
                                endX, endY
                            )
                        }
                        // Track line
                        drawPath(
                            path = path,
                            color = Color(0x3322D3EE),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Animated Photon Pulse
                        val t = flowProgress1
                        // Approximate cubic bezier point
                        val oneMinusT = 1f - t
                        val pX = (oneMinusT * oneMinusT * oneMinusT * startX) +
                                (3f * oneMinusT * oneMinusT * t * startX) +
                                (3f * oneMinusT * t * t * endX) +
                                (t * t * t * endX)
                        val pY = (oneMinusT * oneMinusT * oneMinusT * startY) +
                                (3f * oneMinusT * oneMinusT * t * (startY + h * 0.5f)) +
                                (3f * oneMinusT * t * t * (startY + h * 0.5f)) +
                                (t * t * t * endY)

                        drawCircle(
                            color = primaryColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.35f),
                            radius = 7.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                    }
                }

                // Middle Cluster: Heterogeneous Hardware (CPU, GPU, NPU, RAM)
                Column(
                    modifier = Modifier.fillMaxWidth(0.96f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Memory,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "HETEROGENEOUS AI CORE ACCELERATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.6.sp
                        )
                    }

                    // 4 Hardware Units in a Fluid Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // CPU
                        HardwareNodeCard(
                            name = "CPU",
                            role = "ARM NEON",
                            subRole = "YUV & Slicing",
                            icon = Icons.Rounded.Memory,
                            accentColor = Color(0xFF38BDF8),
                            isActive = true,
                            glowAlpha = hardwareGlow,
                            modifier = Modifier.weight(1f)
                        )

                        // GPU
                        HardwareNodeCard(
                            name = "GPU",
                            role = "Vulkan Ops",
                            subRole = "Tensor Conv",
                            icon = Icons.Rounded.Bolt,
                            accentColor = Color(0xFFFBBF24),
                            isActive = true,
                            glowAlpha = hardwareGlow,
                            modifier = Modifier.weight(1f)
                        )

                        // NPU
                        HardwareNodeCard(
                            name = "NPU",
                            role = "Neural Core",
                            subRole = "Inference AI",
                            icon = Icons.Rounded.AutoAwesome,
                            accentColor = Color(0xFFF472B6),
                            isActive = true,
                            glowAlpha = hardwareGlow,
                            modifier = Modifier.weight(1f)
                        )

                        // RAM
                        HardwareNodeCard(
                            name = "RAM",
                            role = "Zero-OOM",
                            subRole = "Tile Stream",
                            icon = Icons.Rounded.Storage,
                            accentColor = Color(0xFF34D399),
                            isActive = true,
                            glowAlpha = hardwareGlow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Fluid Connector 2 (Middle Hardware Cluster -> Target Output Node)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(30.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val endX = w / 2f
                    val endY = h
                    val startY = 0f

                    val xSources = listOf(w * 0.125f, w * 0.375f, w * 0.625f, w * 0.875f)

                    xSources.forEach { startX ->
                        val path = Path().apply {
                            moveTo(startX, startY)
                            cubicTo(
                                startX, startY + h * 0.5f,
                                endX, startY + h * 0.5f,
                                endX, endY
                            )
                        }
                        // Track line
                        drawPath(
                            path = path,
                            color = Color(0x33F472B6),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Animated Photon Pulse
                        val t = flowProgress2
                        val oneMinusT = 1f - t
                        val pX = (oneMinusT * oneMinusT * oneMinusT * startX) +
                                (3f * oneMinusT * oneMinusT * t * startX) +
                                (3f * oneMinusT * t * t * endX) +
                                (t * t * t * endX)
                        val pY = (oneMinusT * oneMinusT * oneMinusT * startY) +
                                (3f * oneMinusT * oneMinusT * t * (startY + h * 0.5f)) +
                                (3f * oneMinusT * t * t * (startY + h * 0.5f)) +
                                (t * t * t * endY)

                        drawCircle(
                            color = secondaryColor,
                            radius = 3.5.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                        drawCircle(
                            color = secondaryColor.copy(alpha = 0.35f),
                            radius = 7.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                    }
                }

                // Bottom Node: Target Image (Hasil AI)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x261E293B))
                        .border(1.dp, Color(0x66F472B6), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x2BF472B6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoFixHigh,
                                    contentDescription = "Hasil AI",
                                    tint = secondaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "HASIL AI ($scaleLabel)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryColor,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "$targetW × $targetH px",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                                    )
                                )
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ENHANCED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ================================================================
            // 3. Lower Section: Modified Material Capsule Progress Bar
            // ================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row: Tile Badge & Bold Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tile Pill Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1F1E293B))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GridView,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (totalTiles > 0) "Tile $currentTile dari $totalTiles" else "Inisialisasi Model AI…",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    // Bold Percentage
                    Text(
                        text = "${(progressFraction * 100).roundToInt()}%",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                }

                // Custom Capsule Material Progress Bar
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction.coerceIn(0.03f, 1f),
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                    label = "capsuleProgress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(CircleShape)
                        .background(Color(0x2B1E293B))
                        .border(1.dp, Color(0x2EFFFFFF), CircleShape)
                ) {
                    // Filled Gradient Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF06B6D4), // Cyan
                                        Color(0xFF6366F1), // Indigo
                                        Color(0xFFEC4899)  // Pink
                                    )
                                )
                            )
                    ) {
                        // Fluid Shimmer Light Beam Pass
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val beamWidth = size.width * 0.4f
                            val beamX = size.width * shimmerOffset
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.4f),
                                        Color.Transparent
                                    ),
                                    startX = beamX - beamWidth / 2f,
                                    endX = beamX + beamWidth / 2f
                                )
                            )
                        }

                        // Leading Head Glow Dot
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }

                // Sub-Row: Estimated Time & Hardware Stream Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = estimatedRemainingText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Zero-OOM Protected",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
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
                    .padding(horizontal = 24.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Batalkan",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Batalkan Proses",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }
    }
}

@Composable
private fun HardwareNodeCard(
    name: String,
    role: String,
    subRole: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isActive: Boolean,
    glowAlpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(Color(0x241E293B))
            .border(
                1.dp,
                if (isActive) accentColor.copy(alpha = 0.3f + 0.35f * glowAlpha) else Color(0x26FFFFFF),
                RoundedCornerShape(13.dp)
            )
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = name,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = role,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            Text(
                text = subRole,
                fontSize = 7.5.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
