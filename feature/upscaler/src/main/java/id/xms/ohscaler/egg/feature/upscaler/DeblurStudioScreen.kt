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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Shield
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
import id.xms.ohscaler.egg.core.ui.SegmentedScaleSelector
import id.xms.ohscaler.egg.core.ui.SlideToProcessButton
import kotlin.math.roundToInt

@Composable
fun DeblurStudioScreen(
    sourceBitmap: Bitmap,
    uiState: UpscalerUiState,
    onNavigateBack: () -> Unit,
    onScaleChange: (Float) -> Unit,
    onDeblurRadiusChange: (Float) -> Unit,
    onClarityStrengthChange: (Float) -> Unit,
    onDeblurPresetChange: (DeblurPreset) -> Unit,
    onFaceAwareToggle: (Boolean) -> Unit,
    onAntiHaloToggle: (Boolean) -> Unit,
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
            title = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tool_deblur_title),
            iconRes = id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_lens_restore,
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
            modifier = Modifier.weight(1f),
            customBadgeText = if (uiState.scaleFactor == 1.0f) "1× RESTORE" else "${uiState.scaleFactor.toInt()}× RESTORE"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Deblur & Lens Restoration Controls Deck
        FrostedGlassCard(
            isDarkTheme = isDarkTheme,
            contentPadding = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 310.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Restoration Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.deblur_mode_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                    Text(
                        text = "MULTI-SCALE YUV",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        DeblurPreset.Defocus to id.xms.ohscaler.egg.core.designsystem.R.string.deblur_mode_defocus,
                        DeblurPreset.Motion to id.xms.ohscaler.egg.core.designsystem.R.string.deblur_mode_motion,
                        DeblurPreset.SoftPortrait to id.xms.ohscaler.egg.core.designsystem.R.string.deblur_mode_portrait
                    ).forEach { (preset, labelRes) ->
                        val isSelected = uiState.deblurPreset == preset
                        val animBg by animateColorAsState(
                            targetValue = if (isSelected) primaryAccent else if (isDarkTheme) Color(0x24FFFFFF) else Color(0x14334155),
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                            label = "deblurPresetBg"
                        )
                        val animTextColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                            } else textSecondaryColor,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
                            label = "deblurPresetText"
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
                                        onDeblurPresetChange(preset)
                                    }
                                )
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animTextColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Deblur Radius Slider (2px - 20px)
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

                // Clarity & Recovery Slider (0% - 150%)
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

                // Face-Aware Clarity Capsule Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDarkTheme) Color(0x1F22D3EE) else Color(0x140284C7))
                        .border(1.dp, if (uiState.faceAwareEnabled) primaryAccent.copy(alpha = 0.4f) else glassBorder, RoundedCornerShape(14.dp))
                        .clickable { onFaceAwareToggle(!uiState.faceAwareEnabled) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Face,
                            contentDescription = null,
                            tint = primaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.face_aware_title),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor
                            )
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.face_aware_desc),
                                fontSize = 10.5.sp,
                                color = textMutedColor
                            )
                        }
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

                // Anti-Halo Shield Capsule Card
                val isAntiHaloActive = uiState.antiHaloClamp < 90f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDarkTheme) Color(0x1A1E293B) else Color(0x12F1F5F9))
                        .border(1.dp, if (isAntiHaloActive) secondaryAccent.copy(alpha = 0.4f) else glassBorder, RoundedCornerShape(14.dp))
                        .clickable { onAntiHaloToggle(!isAntiHaloActive) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = secondaryAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.anti_halo_title),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor
                            )
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.anti_halo_desc),
                                fontSize = 10.5.sp,
                                color = textMutedColor
                            )
                        }
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

                // Output Scale Multiplier (1x Restore, 2x, 4x)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Output Scale After Deblur",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                }

                SegmentedScaleSelector(
                    selectedScale = uiState.scaleFactor,
                    onScaleSelected = onScaleChange,
                    scales = listOf(1.0f, 2.0f, 4.0f),
                    labels = listOf("1× (Restore)", "2×", "4×"),
                    isDarkTheme = isDarkTheme
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Action Bar
        SlideToProcessButton(
            text = "Slide to Restore Photo",
            onConfirmed = onStartUpscale,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
