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
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.DarkGlassBorder
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkSecondary
import id.xms.ohscaler.egg.core.designsystem.DarkTextMuted
import id.xms.ohscaler.egg.core.designsystem.DarkTextPrimary
import id.xms.ohscaler.egg.core.designsystem.DarkTextSecondary
import id.xms.ohscaler.egg.core.designsystem.LightGlassBorder
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

@Composable
fun SuperResStudioScreen(
    sourceBitmap: Bitmap,
    uiState: UpscalerUiState,
    onNavigateBack: () -> Unit,
    onScaleChange: (Float) -> Unit,
    onSharpnessChange: (Float) -> Unit,
    onAntiAliasingChange: (Float) -> Unit,
    onPresetChange: (TuningPreset) -> Unit,
    onStartUpscale: () -> Unit,
    onSelectNewImage: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    val haptic = LocalHapticFeedback.current

    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondaryColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
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
            title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_super_res_title),
            iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_super_resolution,
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

        // 3. Super-Resolution Controls Deck
        FrostedGlassCard(
            isDarkTheme = isDarkTheme,
            contentPadding = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 290.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Resolution Badge Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ResolutionBadge(
                        srcWidth = sourceBitmap.width,
                        srcHeight = sourceBitmap.height,
                        scaleFactor = uiState.scaleFactor,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Section: Scale Multiplier (1.5x up to 16x)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.scale_factor_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Text(
                        text = if (uiState.scaleFactor >= 16f) "CASCADED 2-PASS AI" else "NEURAL UPSCALING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 0.5.sp
                    )
                }

                SegmentedScaleSelector(
                    selectedScale = uiState.scaleFactor,
                    onScaleSelected = onScaleChange,
                    scales = listOf(1.5f, 2.0f, 3.0f, 4.0f, 8.0f, 16.0f),
                    labels = listOf("1.5×", "2×", "3×", "4×", "8×", "16×"),
                    isDarkTheme = isDarkTheme
                )

                // Section: Detail & Edge Anti-Aliasing (SSAA)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tuning_panel_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )

                    if (uiState.tuningPreset != TuningPreset.Balanced) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = primaryAccent.copy(alpha = 0.2f)),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onPresetChange(TuningPreset.Balanced)
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RestartAlt,
                                contentDescription = "Reset",
                                tint = primaryAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Default",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryAccent
                            )
                        }
                    }
                }

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
                            label = "presetBg"
                        )
                        val animTextColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                            } else textSecondaryColor,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                            label = "presetText"
                        )

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(animBg)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color.Transparent else glassBorder,
                                    shape = CircleShape
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onPresetChange(preset)
                                    }
                                )
                                .padding(horizontal = 13.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animTextColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Sliders
                PillProgressSlider(
                    value = uiState.antiAliasing,
                    onValueChange = onAntiAliasingChange,
                    label = "Anti-Aliasing Smoothing (De-Jaggies)",
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
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Action Bar
        SlideToProcessButton(
            text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_process_ai),
            onConfirmed = onStartUpscale,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
