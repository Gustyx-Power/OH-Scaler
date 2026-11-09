package id.xms.ohscaler.egg.feature.upscaler

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.CyanAccent
import id.xms.ohscaler.egg.core.designsystem.CyanGlow
import id.xms.ohscaler.egg.core.designsystem.ElectricViolet
import id.xms.ohscaler.egg.core.designsystem.ObsidianBlack
import id.xms.ohscaler.egg.core.designsystem.SurfaceDark
import id.xms.ohscaler.egg.core.designsystem.SurfaceVariantDark
import id.xms.ohscaler.egg.core.designsystem.TextMuted
import id.xms.ohscaler.egg.core.designsystem.TextPrimary
import id.xms.ohscaler.egg.core.designsystem.TextSecondary
import id.xms.ohscaler.egg.core.ui.ResolutionBadge
import kotlin.math.roundToInt

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
    modifier: Modifier = Modifier
) {
    val titleRes = when (uiState.activeFeatureMode) {
        FeatureMode.SuperResolution -> id.xms.ohscaler.egg.core.designsystem.R.string.tool_super_res_title
        FeatureMode.DeblurRestore -> id.xms.ohscaler.egg.core.designsystem.R.string.tool_deblur_title
        FeatureMode.ProStudio -> id.xms.ohscaler.egg.core.designsystem.R.string.tool_pro_studio_title
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Studio Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.studio_title),
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Image Preview & Resolution Info Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = sourceBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            ResolutionBadge(
                srcWidth = sourceBitmap.width,
                srcHeight = sourceBitmap.height,
                scaleFactor = uiState.scaleFactor
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Scale Factor Selector Chips
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.scale_factor_title),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val scales = listOf(1.5f, 2.0f, 3.0f, 4.0f, 8.0f)
                val labels = listOf("1.5×", "2×", "3×", "4×", "8×")

                scales.zip(labels).forEach { (scale, label) ->
                    val isSelected = uiState.scaleFactor == scale
                    FilterChip(
                        selected = isSelected,
                        onClick = { onScaleChange(scale) },
                        label = {
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.height(44.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricViolet,
                            selectedLabelColor = TextPrimary,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) ElectricViolet else BorderDark
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tuning & Custom Controls Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceDark)
                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.tuning_panel_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Presets Row
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.preset_title),
                fontSize = 12.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

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
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPresetChange(preset) },
                        label = {
                            Text(
                                text = stringResource(labelRes),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.height(38.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAccent,
                            selectedLabelColor = ObsidianBlack,
                            containerColor = SurfaceVariantDark,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sharpness Intensity Slider
            val sharpnessPct = (uiState.sharpness * 100f).roundToInt()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.slider_sharpness, sharpnessPct),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "$sharpnessPct%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricViolet
                )
            }
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.slider_sharpness_desc),
                fontSize = 11.sp,
                color = TextMuted
            )
            Slider(
                value = uiState.sharpness,
                onValueChange = onSharpnessChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = ElectricViolet,
                    activeTrackColor = ElectricViolet,
                    inactiveTrackColor = SurfaceVariantDark
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Anti-Aliasing (De-Jaggies) Slider
            val aaPct = (uiState.antiAliasing * 100f).roundToInt()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.slider_anti_aliasing, aaPct),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "$aaPct%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanGlow
                )
            }
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.slider_anti_aliasing_desc),
                fontSize = 11.sp,
                color = TextMuted
            )
            Slider(
                value = uiState.antiAliasing,
                onValueChange = onAntiAliasingChange,
                valueRange = 0f..1f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = CyanGlow,
                    activeTrackColor = CyanAccent,
                    inactiveTrackColor = SurfaceVariantDark
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Action Button: Process with AI
        Button(
            onClick = onStartUpscale,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricViolet
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_process_ai),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Change Photo Button
        OutlinedButton(
            onClick = onSelectNewImage,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
        ) {
            Icon(
                imageVector = Icons.Rounded.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_change_photo),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
