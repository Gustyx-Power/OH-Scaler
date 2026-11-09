package id.xms.ohscaler.egg.feature.upscaler

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestorePage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.designsystem.DarkPrimary
import id.xms.ohscaler.egg.core.designsystem.LightPrimary
import id.xms.ohscaler.egg.core.ui.FullscreenProcessingScreen

@Composable
fun HomeScreen(
    viewModel: UpscalerViewModel,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
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

    // Handle system back navigation gracefully
    BackHandler(enabled = uiState.currentScreen != ScreenState.HomeHub && uiState.currentScreen != ScreenState.Onboarding) {
        when (uiState.currentScreen) {
            ScreenState.ResultPreview -> viewModel.navigateToHome()
            ScreenState.StudioWorkspace -> viewModel.navigateToHome()
            ScreenState.Processing -> viewModel.cancelUpscale()
            else -> Unit
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                ScreenState.Onboarding -> {
                    OnboardingScreen(
                        onComplete = { viewModel.completeOnboarding() },
                        isDarkTheme = isDarkTheme
                    )
                }

                ScreenState.Processing -> {
                    FullscreenProcessingScreen(
                        progress = uiState.progress,
                        onCancel = { viewModel.cancelUpscale() },
                        sourceWidth = uiState.sourceBitmap?.width ?: 0,
                        sourceHeight = uiState.sourceBitmap?.height ?: 0,
                        scaleFactor = uiState.scaleFactor,
                        isDarkTheme = isDarkTheme
                    )
                }

                ScreenState.ResultPreview -> {
                    if (uiState.upscaledBitmap != null && uiState.sourceBitmap != null) {
                        ResultScreen(
                            sourceBitmap = uiState.sourceBitmap!!,
                            upscaledBitmap = uiState.upscaledBitmap!!,
                            scaleFactor = uiState.scaleFactor,
                            isSaving = uiState.isSaving,
                            onSave = { onDone -> viewModel.saveUpscaledImage(onDone) },
                            onReset = { viewModel.reset() },
                            isDarkTheme = isDarkTheme
                        )
                    } else {
                        viewModel.navigateToHome()
                    }
                }

                ScreenState.StudioWorkspace -> {
                    val bitmap = uiState.sourceBitmap
                    if (bitmap != null) {
                        when (uiState.activeFeatureMode) {
                            FeatureMode.SuperResolution -> {
                                SuperResStudioScreen(
                                    sourceBitmap = bitmap,
                                    uiState = uiState,
                                    onNavigateBack = { viewModel.navigateToHome() },
                                    onScaleChange = { viewModel.setScaleFactor(it) },
                                    onSharpnessChange = { viewModel.setSharpness(it) },
                                    onAntiAliasingChange = { viewModel.setAntiAliasing(it) },
                                    onPresetChange = { viewModel.applyPreset(it) },
                                    onStartUpscale = { viewModel.startUpscale() },
                                    onSelectNewImage = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    isDarkTheme = isDarkTheme
                                )
                            }
                            FeatureMode.DeblurRestore -> {
                                DeblurStudioScreen(
                                    sourceBitmap = bitmap,
                                    uiState = uiState,
                                    onNavigateBack = { viewModel.navigateToHome() },
                                    onScaleChange = { viewModel.setScaleFactor(it) },
                                    onDeblurRadiusChange = { viewModel.setDeblurRadius(it) },
                                    onClarityStrengthChange = { viewModel.setClarityStrength(it) },
                                    onDeblurPresetChange = { viewModel.applyDeblurPreset(it) },
                                    onFaceAwareToggle = { viewModel.setFaceAwareEnabled(it) },
                                    onAntiHaloToggle = { enabled ->
                                        viewModel.setAntiHaloClamp(if (enabled) 30f else 255f)
                                    },
                                    onStartUpscale = { viewModel.startUpscale() },
                                    onSelectNewImage = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    isDarkTheme = isDarkTheme
                                )
                            }
                            FeatureMode.ProStudio -> {
                                ProStudioScreen(
                                    sourceBitmap = bitmap,
                                    uiState = uiState,
                                    onNavigateBack = { viewModel.navigateToHome() },
                                    onScaleChange = { viewModel.setScaleFactor(it) },
                                    onSharpnessChange = { viewModel.setSharpness(it) },
                                    onAntiAliasingChange = { viewModel.setAntiAliasing(it) },
                                    onPresetChange = { viewModel.applyPreset(it) },
                                    onDeblurRadiusChange = { viewModel.setDeblurRadius(it) },
                                    onClarityStrengthChange = { viewModel.setClarityStrength(it) },
                                    onFaceAwareToggle = { viewModel.setFaceAwareEnabled(it) },
                                    onAntiHaloToggle = { enabled ->
                                        viewModel.setAntiHaloClamp(if (enabled) 30f else 255f)
                                    },
                                    onExportFormatChange = { viewModel.setExportFormat(it) },
                                    onExportQualityChange = { viewModel.setExportQuality(it) },
                                    onTabChange = { viewModel.setProStudioTab(it) },
                                    onStartUpscale = { viewModel.startUpscale() },
                                    onSelectNewImage = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        }
                    } else {
                        viewModel.navigateToHome()
                    }
                }

                ScreenState.HomeHub -> {
                    HomeHubScreen(
                        uiState = uiState,
                        onSelectFeature = { feature ->
                            viewModel.selectFeatureMode(feature)
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onQuickPick = {
                            viewModel.selectFeatureMode(FeatureMode.SuperResolution)
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onSelectRecent = { uri ->
                            viewModel.onImageSelected(uri)
                        },
                        onOpenTutorial = {
                            viewModel.reopenOnboarding()
                        },
                        onToggleTheme = {
                            viewModel.toggleTheme()
                        },
                        isDarkTheme = isDarkTheme
                    )
                }
            }

        }

        // Interrupted Session Recovery Dialog
        uiState.pendingCheckpoint?.let { checkpoint ->
            AlertDialog(
                onDismissRequest = { viewModel.discardCheckpoint() },
                containerColor = if (isDarkTheme) Color(0xFF151821) else Color.White,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.RestorePage,
                        contentDescription = null,
                        tint = if (isDarkTheme) DarkPrimary else LightPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Sesi Peningkatan Terhenti",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkTheme) Color.White else Color(0xFF0F172A)
                    )
                },
                text = {
                    Text(
                        text = "Ditemukan proses peningkatan foto sebelumnya yang belum selesai (${checkpoint.scaleFactor.toInt()}× scale). Apakah Anda ingin melanjutkan proses ini?",
                        fontSize = 14.sp,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resumeCheckpoint(checkpoint) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) DarkPrimary else LightPrimary,
                            contentColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
                        )
                    ) {
                        Text("Lanjutkan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.discardCheckpoint() }
                    ) {
                        Text("Buang Sesi")
                    }
                }
            )
        }
    }
}
