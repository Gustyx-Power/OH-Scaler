package id.xms.ohscaler.egg.feature.upscaler

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import id.xms.ohscaler.egg.core.ui.SegmentedScaleSelector
import id.xms.ohscaler.egg.core.ui.SlideToProcessButton
import kotlin.math.roundToInt

/**
 * Modern Mobile Photo Editor Console for OH-Scaler.
 * Viewport stage stays prominent and interactive with zoom/pan, while controls
 * are gathered in an Android 17 frosted glass console with a sticky action footer.
 */
@Composable
fun StudioScreen(
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

    val titleRes = when (uiState.activeFeatureMode) {
        FeatureMode.SuperResolution -> id.xms.ohscaler.egg.core.designsystem.R.string.tool_super_res_title
        FeatureMode.DeblurRestore -> id.xms.ohscaler.egg.core.designsystem.R.string.tool_deblur_title
        FeatureMode.ProStudio -> id.xms.ohscaler.egg.core.designsystem.R.string.tool_pro_studio_title
    }

    val iconRes = when (uiState.activeFeatureMode) {
        FeatureMode.SuperResolution -> id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_super_resolution
        FeatureMode.DeblurRestore -> id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_lens_restore
        FeatureMode.ProStudio -> id.xms.ohscaler.egg.core.designsystem.R.drawable.ic_tuning_studio_custom
    }

    val titleColor = if (isDarkTheme) DarkTextPrimary else LightTextPrimary
    val textSecondaryColor = if (isDarkTheme) DarkTextSecondary else LightTextSecondary
    val mutedColor = if (isDarkTheme) DarkTextMuted else LightTextMuted
    val glassBg = if (isDarkTheme) DarkGlassSurface else LightGlassSurface
    val glassBorder = if (isDarkTheme) DarkGlassBorder else LightGlassBorder
    val primaryAccent = if (isDarkTheme) DarkPrimary else LightPrimary
    val secondaryAccent = if (isDarkTheme) DarkSecondary else LightSecondary

    // Interactive Zoom / Pan State for Canvas Stage
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (zoomScale * zoomChange).coerceIn(1f, 5f)
        zoomScale = newScale
        if (newScale > 1f) {
            panOffset += panChange
        } else {
            panOffset = Offset.Zero
        }
    }

    val targetWidth = (sourceBitmap.width * uiState.scaleFactor).toInt()
    val targetHeight = (sourceBitmap.height * uiState.scaleFactor).toInt()
    val scaleLabel = if (uiState.scaleFactor % 1f == 0f) "${uiState.scaleFactor.toInt()}×" else "${uiState.scaleFactor}×"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ====================================================================
        // 1. Top Bar: Back, Mode Tag Capsule, Change Photo
        // ====================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(glassBg)
                    .border(1.dp, glassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = titleColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Mode Tag Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
                    .clip(CircleShape)
                    .background(glassBg)
                    .border(1.dp, glassBorder, CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(titleRes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }

            IconButton(
                onClick = onSelectNewImage,
                modifier = Modifier
                    .shadow(if (isDarkTheme) 0.dp else 4.dp, CircleShape, spotColor = Color(0x1F1E293B))
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(glassBg)
                    .border(1.dp, glassBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddPhotoAlternate,
                    contentDescription = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_change_photo),
                    tint = titleColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ====================================================================
        // 2. Interactive Canvas Viewport Stage (weight(1f))
        // ====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 180.dp)
                .shadow(if (isDarkTheme) 0.dp else 6.dp, RoundedCornerShape(22.dp), spotColor = Color(0x1F1E293B))
                .clip(RoundedCornerShape(22.dp))
                .background(if (isDarkTheme) Color(0xFF0F121A) else Color(0xFFE2E8F0))
                .border(1.dp, glassBorder, RoundedCornerShape(22.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (zoomScale > 1.05f) {
                                zoomScale = 1f
                                panOffset = Offset.Zero
                            } else {
                                zoomScale = 2.5f
                            }
                        }
                    )
                }
                .transformable(state = transformableState),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = sourceBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoomScale
                        scaleY = zoomScale
                        translationX = panOffset.x
                        translationY = panOffset.y
                    },
                contentScale = ContentScale.Fit
            )

            // Top-Left: Original Spec Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xB3151821) else Color(0xCCFFFFFF))
                    .border(1.dp, if (isDarkTheme) Color(0x33FFFFFF) else Color(0x40CBD5E1), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${sourceBitmap.width} × ${sourceBitmap.height} px • Original",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF334155)
                )
            }

            // Top-Right: Zoom Reset Pill Button (Visible when zoomed)
            if (zoomScale > 1.05f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(CircleShape)
                        .background(if (isDarkTheme) Color(0xD9151821) else Color(0xEBFFFFFF))
                        .border(1.dp, if (isDarkTheme) Color(0x40FFFFFF) else Color(0x60CBD5E1), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                zoomScale = 1f
                                panOffset = Offset.Zero
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ZoomOutMap,
                            contentDescription = "Reset Zoom",
                            tint = primaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Reset 1×",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryAccent
                        )
                    }
                }
            }

            // Bottom-Right: Target Output Resolution Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xD9151821) else Color(0xEBFFFFFF))
                    .border(1.dp, if (isDarkTheme) Color(0x40FFFFFF) else Color(0x60CBD5E1), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(primaryAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = scaleLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                        )
                    }
                    Text(
                        text = "Target: ${targetWidth} × ${targetHeight}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ====================================================================
        // 3. Frosted Glass Editor Control Deck
        // ====================================================================
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
                // Section A: Scale Multiplier
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
                        text = "TARGET RESOLUTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccent,
                        letterSpacing = 0.5.sp
                    )
                }

                SegmentedScaleSelector(
                    selectedScale = uiState.scaleFactor,
                    onScaleSelected = onScaleChange,
                    isDarkTheme = isDarkTheme
                )

                // Section B: Detail Tuning Presets & Sliders
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

                // Presets Pill Selector
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

                // Pill Sliders (Sharpness & Anti-Aliasing)
                PillProgressSlider(
                    value = uiState.sharpness,
                    onValueChange = onSharpnessChange,
                    label = "Sharpness Intensity",
                    accentColor = primaryAccent,
                    isDarkTheme = isDarkTheme
                )

                PillProgressSlider(
                    value = uiState.antiAliasing,
                    onValueChange = onAntiAliasingChange,
                    label = "Anti-Aliasing Smoothing",
                    accentColor = secondaryAccent,
                    isDarkTheme = isDarkTheme
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ====================================================================
        // 4. Sticky Action Bar: Slide-to-Process Pill Slider (matching Photo 2)
        // ====================================================================
        SlideToProcessButton(
            text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_process_ai),
            onConfirmed = onStartUpscale,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
