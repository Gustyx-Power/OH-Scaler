package id.xms.ohscaler.egg.feature.upscaler

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.ai.ExportFormat
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkGlassSurface
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSecondary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
import id.xms.ohscaler.egg.core.designsystem.LightGlassSurface
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.designsystem.LightSecondary
import id.xms.ohscaler.egg.core.designsystem.LightTextMuted
import id.xms.ohscaler.egg.core.designsystem.LightTextPrimary
import id.xms.ohscaler.egg.core.designsystem.LightTextSecondary
import id.xms.ohscaler.egg.core.ui.FrostedGlassCard
import id.xms.ohscaler.egg.core.ui.PillProgressSlider
import id.xms.ohscaler.egg.core.ui.ResolutionBadge
import id.xms.ohscaler.egg.core.ui.SegmentedScaleSelector
import id.xms.ohscaler.egg.core.ui.SlideToProcessButton
import kotlin.math.roundToInt

@Composable
fun ProStudioScreen(
    sourceBitmap: Bitmap,
    uiState: UpscalerUiState,
    onNavigateBack: () -> Unit,
    onScaleChange: (Float) -> Unit,
    onSharpnessChange: (Float) -> Unit,
    onAntiAliasingChange: (Float) -> Unit,
    onPresetChange: (TuningPreset) -> Unit,
    onDeblurRadiusChange: (Float) -> Unit,
    onClarityStrengthChange: (Float) -> Unit,
    onFaceAwareToggle: (Boolean) -> Unit,
    onAntiHaloToggle: (Boolean) -> Unit,
    onExportFormatChange: (ExportFormat) -> Unit,
    onExportQualityChange: (Int) -> Unit,
    onTabChange: (ProStudioTab) -> Unit,
    onStartUpscale: () -> Unit,
    onSelectNewImage: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current

    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondaryColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val textMutedColor = if (isDarkTheme) DarkTextMuted else LightTextMuted
    val glassBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val primaryAccent = if (isDarkTheme) DarkPrimary else LightPrimary
    val secondaryAccent = if (isDarkTheme) DarkSecondary else LightSecondary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Bar
        StudioTopBar(
            title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.studio_pro_title),
            iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_tuning_studio_custom,
            onNavigateBack = onNavigateBack,
            onSelectNewImage = onSelectNewImage,
            isDarkTheme = isDarkTheme
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Viewport Stage
        StudioViewportStage(
            sourceBitmap = sourceBitmap,
            scaleFactor = uiState.scaleFactor,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Pro Multi-Tab Navigation Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(if (isDarkTheme) DarkGlassSurface else LightGlassSurface)
                .border(1.dp, glassBorder, CircleShape)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                ProStudioTab.ScaleAndModel to stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tab_scale),
                ProStudioTab.DeblurAndFace to stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tab_deblur),
                ProStudioTab.DetailAndAA to stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tab_tuning),
                ProStudioTab.ExportAndFormat to stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tab_export)
            ).forEach { (tab, label) ->
                val isSelected = uiState.proStudioTab == tab
                val animBg by animateColorAsState(
                    targetValue = if (isSelected) primaryAccent else Color.Transparent,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "tabBg"
                )
                val animTextColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                    } else textSecondaryColor,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(CircleShape)
                        .background(animBg)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabChange(tab)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = animTextColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Tab Content Panel
        FrostedGlassCard(
            isDarkTheme = isDarkTheme,
            contentPadding = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (uiState.proStudioTab) {
                    // TAB 1: Scale & Engine Architecture
                    ProStudioTab.ScaleAndModel -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scale Multiplier",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            ResolutionBadge(
                                srcWidth = sourceBitmap.width,
                                srcHeight = sourceBitmap.height,
                                scaleFactor = uiState.scaleFactor,
                                isDarkTheme = isDarkTheme
                            )
                        }

                        SegmentedScaleSelector(
                            selectedScale = uiState.scaleFactor,
                            onScaleSelected = onScaleChange,
                            scales = listOf(1.5f, 2.0f, 3.0f, 4.0f, 8.0f, 16.0f),
                            labels = listOf("1.5×", "2×", "3×", "4×", "8×", "16×"),
                            isDarkTheme = isDarkTheme
                        )

                        // Engine Architecture Spec Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkTheme) Color(0x1F22D3EE) else Color(0x140284C7))
                                .border(1.dp, glassBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Memory,
                                contentDescription = null,
                                tint = primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Real-ESRGAN Compact ONNX Engine",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleColor
                                )
                                Text(
                                    text = if (uiState.scaleFactor >= 16f) "2-Pass Cascaded (4× → 4×) • Tile Checkpointed" else "Single-Tile Stream • Zero-OOM Shield",
                                    fontSize = 10.5.sp,
                                    color = textMutedColor
                                )
                            }
                        }
                    }

                    // TAB 2: Deblur & Face-Aware
                    ProStudioTab.DeblurAndFace -> {
                        val radiusProgress = ((uiState.deblurRadius - 2f) / 18f).coerceIn(0f, 1f)
                        PillProgressSlider(
                            value = radiusProgress,
                            onValueChange = { norm ->
                                onDeblurRadiusChange(2f + norm * 18f)
                            },
                            label = "Deblur Spread Radius: ${uiState.deblurRadius.roundToInt()} px",
                            accentColor = primaryAccent,
                            isDarkTheme = isDarkTheme
                        )

                        val clarityProgress = (uiState.clarityStrength / 1.5f).coerceIn(0f, 1f)
                        PillProgressSlider(
                            value = clarityProgress,
                            onValueChange = { norm ->
                                onClarityStrengthChange(norm * 1.5f)
                            },
                            label = "Clarity & Detail Recovery: ${(uiState.clarityStrength * 100).roundToInt()}%",
                            accentColor = secondaryAccent,
                            isDarkTheme = isDarkTheme
                        )

                        // Face Aware Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkTheme) Color(0x1A1E293B) else Color(0x12F1F5F9))
                                .border(1.dp, glassBorder, RoundedCornerShape(12.dp))
                                .clickable { onFaceAwareToggle(!uiState.faceAwareEnabled) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Face,
                                    contentDescription = null,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Face-Aware Portrait Clarity",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = titleColor
                                )
                            }
                            Switch(
                                checked = uiState.faceAwareEnabled,
                                onCheckedChange = onFaceAwareToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = primaryAccent
                                )
                            )
                        }

                        // Anti-Halo Switch
                        val isAntiHaloActive = uiState.antiHaloClamp < 90f
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDarkTheme) Color(0x1A1E293B) else Color(0x12F1F5F9))
                                .border(1.dp, glassBorder, RoundedCornerShape(12.dp))
                                .clickable { onAntiHaloToggle(!isAntiHaloActive) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Shield,
                                    contentDescription = null,
                                    tint = secondaryAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Anti-Halo Shield Clamping",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = titleColor
                                )
                            }
                            Switch(
                                checked = isAntiHaloActive,
                                onCheckedChange = onAntiHaloToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = secondaryAccent
                                )
                            )
                        }
                    }

                    // TAB 3: Detail & AA
                    ProStudioTab.DetailAndAA -> {
                        // Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                TuningPreset.Balanced to id.xms.ohscaler.egg.core.designsystem.R.string.preset_balanced,
                                TuningPreset.SmoothPortrait to id.xms.ohscaler.egg.core.designsystem.R.string.preset_smooth,
                                TuningPreset.CrispGraphic to id.xms.ohscaler.egg.core.designsystem.R.string.preset_crisp,
                                TuningPreset.Custom to id.xms.ohscaler.egg.core.designsystem.R.string.preset_custom
                            ).forEach { (preset, labelRes) ->
                                val isSelected = uiState.tuningPreset == preset
                                val animBg by animateColorAsState(
                                    targetValue = if (isSelected) primaryAccent else if (isDarkTheme) Color(0x24FFFFFF) else Color(0x14334155),
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                    label = "proPresetBg"
                                )
                                val animTextColor by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                                    } else textSecondaryColor,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                    label = "proPresetText"
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(animBg)
                                        .border(width = 1.dp, color = if (isSelected) Color.Transparent else glassBorder, shape = CircleShape)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onPresetChange(preset)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(labelRes),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = animTextColor
                                    )
                                }
                            }
                        }

                        PillProgressSlider(
                            value = uiState.antiAliasing,
                            onValueChange = onAntiAliasingChange,
                            label = "Anti-Aliasing Smoothing (SSAA)",
                            accentColor = secondaryAccent,
                            isDarkTheme = isDarkTheme
                        )

                        PillProgressSlider(
                            value = uiState.sharpness,
                            onValueChange = onSharpnessChange,
                            label = "Sharpness Intensity",
                            accentColor = primaryAccent,
                            isDarkTheme = isDarkTheme
                        )
                    }

                    // TAB 4: Export & Format
                    ProStudioTab.ExportAndFormat -> {
                        Text(
                            text = "Output Image Format",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                ExportFormat.PNG to "PNG (Lossless)",
                                ExportFormat.WEBP to "WebP (Modern)",
                                ExportFormat.JPEG to "JPEG (Compact)"
                            ).forEach { (format, label) ->
                                val isSelected = uiState.exportFormat == format
                                val animBg by animateColorAsState(
                                    targetValue = if (isSelected) primaryAccent else if (isDarkTheme) Color(0x24FFFFFF) else Color(0x14334155),
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                    label = "formatBg"
                                )
                                val animTextColor by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                                    } else textSecondaryColor,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                                    label = "formatText"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(animBg)
                                        .border(1.dp, if (isSelected) Color.Transparent else glassBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onExportFormatChange(format)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = animTextColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (uiState.exportFormat != ExportFormat.PNG) {
                            val qualityNorm = (uiState.exportQuality - 10) / 90f
                            PillProgressSlider(
                                value = qualityNorm,
                                onValueChange = { norm ->
                                    val q = (10 + norm * 90).roundToInt()
                                    onExportQualityChange(q)
                                },
                                label = "Compression Quality: ${uiState.exportQuality}%",
                                accentColor = primaryAccent,
                                isDarkTheme = isDarkTheme
                            )
                        } else {
                            Text(
                                text = "PNG preserves 100% lossless pixel fidelity without compression artifacts.",
                                fontSize = 11.sp,
                                color = textMutedColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Action Bar
        SlideToProcessButton(
            text = "Slide to Process Image",
            onConfirmed = onStartUpscale,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
