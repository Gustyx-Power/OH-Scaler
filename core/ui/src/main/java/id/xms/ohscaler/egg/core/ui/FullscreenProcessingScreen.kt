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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
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

    // Live Hardware Telemetry Polling (Every 3 seconds dynamically from device)
    var telemetry by remember { mutableStateOf(DeviceHardwareTelemetry.getTelemetry(context, elapsedSeconds, currentTile)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            telemetry = DeviceHardwareTelemetry.getTelemetry(context, elapsedSeconds, currentTile)
        }
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
    val actualSrcW = if (sourceWidth > 0) sourceWidth else if (progress is UpscaleProgress.ReadingPixels) progress.sourceWidth else 2560
    val actualSrcH = if (sourceHeight > 0) sourceHeight else if (progress is UpscaleProgress.ReadingPixels) progress.sourceHeight else 1920
    val targetW = (actualSrcW * scaleFactor).roundToInt()
    val targetH = (actualSrcH * scaleFactor).roundToInt()
    val scaleLabel = if (scaleFactor % 1f == 0f) "${scaleFactor.toInt()}×" else "${scaleFactor}×"

    // Infinite animations for data flow (relaxed at 3200ms)
    val transition = rememberInfiniteTransition(label = "flowAnimation")
    val flowProgress1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "flowPulse1"
    )
    val flowProgress2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Restart),
        label = "flowPulse2"
    )
    val hardwareGlow by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hardwareGlow"
    )
    val shimmerOffset by transition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    val primaryColor = DarkPrimary // Cyan #22D3EE
    val secondaryColor = DarkSecondary // Coral #F472B6
    val bgColor = Color(0xFF090C15) // Solid Obsidian

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
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

            Spacer(modifier = Modifier.height(10.dp))

            // ================================================================
            // 2. Center Section: Hierarchical Hardware & Relaxed Data Flow
            // ================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
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

                // Fluid Connector 1 (Top -> Middle Hardware Cluster - Relaxed 3200ms)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(26.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val startX = w / 2f
                    val startY = 0f
                    val endY = h

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

                        // Slower, smooth photon pulse
                        val t = flowProgress1
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
                            color = primaryColor.copy(alpha = 0.25f),
                            radius = 7.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                    }
                }

                // Middle Cluster: Heterogeneous Hardware with Real Telemetry Data
                Column(
                    modifier = Modifier.fillMaxWidth(0.96f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
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
                            text = "HETEROGENEOUS HARDWARE TELEMETRY",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.6.sp
                        )
                    }

                    // 4 Hardware Units with REAL System Data (CPU GHz, GPU Model, NPU TOPS, RAM GB)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. CPU with real clock speed & architecture
                        HardwareNodeCard(
                            name = "CPU",
                            role = telemetry.cpuGhz,
                            subRole = "${telemetry.cpuCores} Cores • ${telemetry.cpuModel}",
                            icon = Icons.Rounded.Memory,
                            accentColor = Color(0xFF38BDF8),
                            isActive = true,
                            glowAlpha = hardwareGlow,
                            modifier = Modifier.weight(1f)
                        )

                        // 2. GPU with real hardware renderer model (e.g. Adreno 740 / Mali-G710)
                        HardwareNodeCard(
                            name = "GPU",
                            role = telemetry.gpuName,
                            subRole = "Tensor Conv",
                            icon = Icons.Rounded.Bolt,
                            accentColor = Color(0xFFFBBF24),
                            isActive = true,
                            glowAlpha = hardwareGlow,
                            modifier = Modifier.weight(1f)
                        )

                        // 3. NPU with calculated Neural TOPS & live throughput
                        HardwareNodeCard(
                            name = "NPU",
                            role = telemetry.npuTops,
                            subRole = telemetry.npuThroughputGflops,
                            icon = Icons.Rounded.AutoAwesome,
                            accentColor = Color(0xFFF472B6),
                            isActive = true,
                            glowAlpha = hardwareGlow,
                            modifier = Modifier.weight(1f)
                        )

                        // 4. RAM with real used/total system GB and app heap MB
                        HardwareNodeCard(
                            name = "RAM",
                            role = "${telemetry.ramUsedGb}/${telemetry.ramTotalGb}GB",
                            subRole = "App: ${telemetry.appRamMb}MB • ${telemetry.freeRamGb}G Free",
                            icon = Icons.Rounded.Storage,
                            accentColor = Color(0xFF34D399),
                            isActive = true,
                            glowAlpha = hardwareGlow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Fluid Connector 2 (Middle Hardware Cluster -> Target Node - Relaxed 3200ms)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(26.dp)
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

                        // Slower, smooth photon pulse
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
                            color = secondaryColor.copy(alpha = 0.25f),
                            radius = 7.dp.toPx(),
                            center = Offset(pX, pY)
                        )
                    }
                }

                // Bottom Node: Target Resolution (NO "Hasil AI" wording)
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
                                    contentDescription = "Target Resolusi",
                                    tint = secondaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "TARGET RESOLUSI ($scaleLabel)",
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

            Spacer(modifier = Modifier.height(14.dp))

            // ================================================================
            // 3. Dynamic Tile Contribution Matrix (Exact Aspect Grid from Photo Dimensions)
            // ================================================================
            DynamicTileMatrix(
                currentTile = currentTile,
                totalTiles = totalTiles,
                sourceWidth = actualSrcW,
                sourceHeight = actualSrcH,
                activeGlow = hardwareGlow,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ================================================================
            // 4. Sturdy Pill Material Progress Bar & Metrics
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                // Sturdy Big Pill Progress Bar (28dp height, 3D specular highlight, bold shimmer)
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction.coerceIn(0.04f, 1f),
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                    label = "capsuleProgress"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, Color(0x4038BDF8), CircleShape)
                ) {
                    // Filled Vibrant Gradient Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF06B6D4), // Radiant Cyan
                                        Color(0xFF3B82F6), // Royal Blue
                                        Color(0xFF8B5CF6), // Purple
                                        Color(0xFFEC4899)  // Hot Pink
                                    )
                                )
                            )
                    ) {
                        // Sturdy Shimmer Light Beam + 3D Glass Specular Highlight
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Top 3D glossy edge line
                            drawLine(
                                color = Color.White.copy(alpha = 0.45f),
                                start = Offset(8.dp.toPx(), 3.5.dp.toPx()),
                                end = Offset(size.width - 8.dp.toPx(), 3.5.dp.toPx()),
                                strokeWidth = 1.2.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Bold Solid Shimmer Beam
                            val beamWidth = size.width * 0.35f
                            val beamX = size.width * shimmerOffset
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.2f),
                                        Color.White.copy(alpha = 0.85f),
                                        Color.White.copy(alpha = 0.95f),
                                        Color.White.copy(alpha = 0.85f),
                                        Color.White.copy(alpha = 0.2f),
                                        Color.Transparent
                                    ),
                                    startX = beamX - beamWidth / 2f,
                                    endX = beamX + beamWidth / 2f
                                )
                            )
                        }

                        // Prominent Leading Head Glow Dot
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.CenterEnd)
                                .padding(end = 5.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, Color(0xFF22D3EE), CircleShape)
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

            Spacer(modifier = Modifier.height(14.dp))

            // ================================================================
            // 5. Bottom Section: Cancel Processing Pill Button
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

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DynamicTileMatrix(
    currentTile: Int,
    totalTiles: Int,
    sourceWidth: Int,
    sourceHeight: Int,
    activeGlow: Float,
    modifier: Modifier = Modifier
) {
    val displayTotal = totalTiles.coerceAtLeast(1)
    val displayCurrent = currentTile.coerceIn(0, displayTotal)

    // Calculate exact dynamic grid layout matching the 256px tile slicing of the image
    val (cols, rows) = remember(sourceWidth, sourceHeight, displayTotal) {
        if (sourceWidth > 0 && sourceHeight > 0) {
            val c = ((sourceWidth + 255) / 256).coerceAtLeast(1)
            val r = ((sourceHeight + 255) / 256).coerceAtLeast(1)
            val singlePass = c * r
            if (displayTotal > 0 && displayTotal != singlePass) {
                // Cascaded 16x pass or custom scale
                val ratio = sourceWidth.toFloat() / sourceHeight.toFloat()
                val estCols = kotlin.math.sqrt(displayTotal * ratio).roundToInt().coerceIn(3, 20)
                val estRows = ((displayTotal + estCols - 1) / estCols).coerceAtLeast(1)
                Pair(estCols, estRows)
            } else {
                Pair(c.coerceIn(1, 24), r.coerceIn(1, 24))
            }
        } else {
            val estCols = kotlin.math.sqrt(displayTotal * 1.33f).roundToInt().coerceIn(3, 20)
            val estRows = ((displayTotal + estCols - 1) / estCols).coerceAtLeast(1)
            Pair(estCols, estRows)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x241E293B))
            .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Grid Dimensions & Completed Tile Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GridView,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "DYNAMIC TILE MATRIX (${cols}×${rows})",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.6.sp
                    )
                }

                Text(
                    text = "$displayCurrent / $displayTotal petak selesai",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }

            // Dynamic Sized Canvas matching exact rows & columns
            val cellHeightDp = (180 / rows).coerceIn(8, 20).dp
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((rows * cellHeightDp.value + (rows - 1) * 3).dp)
            ) {
                val availableW = size.width
                val gap = 3.dp.toPx()
                val calculatedCell = (availableW - (cols - 1) * gap) / cols
                val cellSize = minOf(calculatedCell, 22.dp.toPx())
                val totalGridWidth = cols * cellSize + (cols - 1) * gap
                val startXOffset = (availableW - totalGridWidth) / 2f
                val cornerRadius = CornerRadius(2.2.dp.toPx(), 2.2.dp.toPx())

                for (idx in 0 until displayTotal) {
                    val r = idx / cols
                    val c = idx % cols
                    val x = startXOffset + c * (cellSize + gap)
                    val y = r * (cellSize + gap)
                    val cellRect = Rect(Offset(x, y), Size(cellSize, cellSize))

                    when {
                        idx < displayCurrent -> {
                            // Completed tile: GitHub style vibrant emerald block
                            drawRoundRect(
                                color = Color(0xFF10B981),
                                topLeft = cellRect.topLeft,
                                size = cellRect.size,
                                cornerRadius = cornerRadius
                            )
                            drawRoundRect(
                                color = Color(0xFF34D399),
                                topLeft = cellRect.topLeft,
                                size = cellRect.size,
                                cornerRadius = cornerRadius,
                                style = Stroke(width = 0.8.dp.toPx())
                            )
                        }
                        idx == displayCurrent -> {
                            // In-progress tile: Glowing pulsating amber block
                            val pulseColor = Color(0xFFFBBF24)
                            drawRoundRect(
                                color = pulseColor.copy(alpha = 0.35f + 0.45f * activeGlow),
                                topLeft = cellRect.topLeft,
                                size = cellRect.size,
                                cornerRadius = cornerRadius
                            )
                            drawRoundRect(
                                color = pulseColor,
                                topLeft = cellRect.topLeft,
                                size = cellRect.size,
                                cornerRadius = cornerRadius,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                        }
                        else -> {
                            // Pending tile: Empty hollow dark square
                            drawRoundRect(
                                color = Color(0x1A334155),
                                topLeft = cellRect.topLeft,
                                size = cellRect.size,
                                cornerRadius = cornerRadius
                            )
                            drawRoundRect(
                                color = Color(0x26FFFFFF),
                                topLeft = cellRect.topLeft,
                                size = cellRect.size,
                                cornerRadius = cornerRadius,
                                style = Stroke(width = 0.8.dp.toPx())
                            )
                        }
                    }
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
                color = accentColor,
                maxLines = 1
            )
            Text(
                text = subRole,
                fontSize = 7.5.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}
