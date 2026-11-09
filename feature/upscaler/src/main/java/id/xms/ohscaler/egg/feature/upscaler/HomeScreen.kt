package id.xms.ohscaler.egg.feature.upscaler

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.ai.EnhancementMode
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.designsystem.BorderDark
import id.xms.ohscaler.egg.core.designsystem.CyanAccent
import id.xms.ohscaler.egg.core.designsystem.CyanGlow
import id.xms.ohscaler.egg.core.designsystem.ElectricViolet
import id.xms.ohscaler.egg.core.designsystem.ElectricVioletLight
import id.xms.ohscaler.egg.core.designsystem.ObsidianBlack
import id.xms.ohscaler.egg.core.designsystem.SurfaceDark
import id.xms.ohscaler.egg.core.designsystem.SurfaceVariantDark
import id.xms.ohscaler.egg.core.designsystem.TextMuted
import id.xms.ohscaler.egg.core.designsystem.TextPrimary
import id.xms.ohscaler.egg.core.designsystem.TextSecondary
import id.xms.ohscaler.egg.core.ui.ProcessingProgressCard
import id.xms.ohscaler.egg.core.ui.ResolutionBadge

@Composable
fun HomeScreen(
    viewModel: UpscalerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Android 12+ System Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onImageSelected(uri)
        }
    }

    val isProcessing = uiState.progress is UpscaleProgress.ReadingPixels ||
            uiState.progress is UpscaleProgress.Processing ||
            uiState.progress is UpscaleProgress.Stitching

    Scaffold(
        containerColor = ObsidianBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.upscaledBitmap != null && uiState.sourceBitmap != null) {
                // Show Result Screen with Split Comparison Slider
                ResultScreen(
                    sourceBitmap = uiState.sourceBitmap!!,
                    upscaledBitmap = uiState.upscaledBitmap!!,
                    scaleFactor = uiState.scaleFactor,
                    isSaving = uiState.isSaving,
                    onSave = { onDone -> viewModel.saveUpscaledImage(onDone) },
                    onReset = { viewModel.reset() }
                )
            } else {
                // Home Configuration View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.app_name),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.app_tagline),
                                fontSize = 12.sp,
                                color = CyanAccent
                            )
                        }

                        // Offline Pill Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceVariantDark)
                                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudOff,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "100% Offline",
                                fontSize = 11.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Hero Feature Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(SurfaceDark, SurfaceVariantDark)
                                )
                            )
                            .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = ElectricVioletLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.home_hero_title),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.home_hero_desc),
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // AI Engine Status Indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (uiState.isEngineReady) Color(0x1F10B981) else Color(0x1FFFFFFF))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (uiState.isEngineReady) Color(0xFF10B981) else TextMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isEngineReady) {
                                        stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.engine_status_active)
                                    } else {
                                        stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.engine_status_loading)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (uiState.isEngineReady) Color(0xFF10B981) else TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Action Area: Image Picker Dropzone or Image Preview
                    val sourceBitmap = uiState.sourceBitmap
                    if (sourceBitmap == null) {
                        // Empty State: Tap to pick photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceDark)
                                .border(1.5.dp, BorderDark, RoundedCornerShape(24.dp))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0x227C3AED)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = ElectricVioletLight,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_select_image),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "JPEG, PNG, WebP (Scoped Storage)",
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Image Loaded State
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceDark)
                                .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Thumbnail Preview
                            Image(
                                bitmap = sourceBitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Fit
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Resolution Badge
                            ResolutionBadge(
                                srcWidth = sourceBitmap.width,
                                srcHeight = sourceBitmap.height,
                                scaleFactor = uiState.scaleFactor
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Scale Factor Selector Chips
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.scale_factor_title),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.scaleFactor == 2,
                                    onClick = { viewModel.setScaleFactor(2) },
                                    label = {
                                        Text(
                                            text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.scale_2x),
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricViolet,
                                        selectedLabelColor = TextPrimary,
                                        containerColor = SurfaceVariantDark,
                                        labelColor = TextSecondary
                                    )
                                )
                                FilterChip(
                                    selected = uiState.scaleFactor == 4,
                                    onClick = { viewModel.setScaleFactor(4) },
                                    label = {
                                        Text(
                                            text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.scale_4x),
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricViolet,
                                        selectedLabelColor = TextPrimary,
                                        containerColor = SurfaceVariantDark,
                                        labelColor = TextSecondary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Enhancement & Deblur Mode Selector Chips
                            Text(
                                text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.enhancement_mode_title),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilterChip(
                                    selected = uiState.enhancementMode == EnhancementMode.Natural,
                                    onClick = { viewModel.setEnhancementMode(EnhancementMode.Natural) },
                                    label = {
                                        Text(
                                            text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.mode_natural),
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent,
                                        selectedLabelColor = ObsidianBlack,
                                        containerColor = SurfaceVariantDark,
                                        labelColor = TextSecondary
                                    )
                                )
                                FilterChip(
                                    selected = uiState.enhancementMode == EnhancementMode.UltraDeblur,
                                    onClick = { viewModel.setEnhancementMode(EnhancementMode.UltraDeblur) },
                                    label = {
                                        Text(
                                            text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.mode_ultra_deblur),
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanAccent,
                                        selectedLabelColor = ObsidianBlack,
                                        containerColor = SurfaceVariantDark,
                                        labelColor = TextSecondary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action Buttons
                            Button(
                                onClick = { viewModel.startUpscale() },
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
                                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_upscale_now),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Text(
                                    text = stringResource(id.xms.ohscaler.egg.core.designsystem.R.string.action_change_photo),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Real-Time Processing Overlay Modal
            AnimatedVisibility(
                visible = isProcessing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xDD0D0F17))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ProcessingProgressCard(
                        progress = uiState.progress,
                        onCancel = { viewModel.cancelUpscale() }
                    )
                }
            }
        }
    }
}
