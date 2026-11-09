package id.xms.ohscaler.egg.feature.upscaler

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.designsystem.ObsidianBlack
import id.xms.ohscaler.egg.core.ui.ProcessingProgressCard

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

    // Handle system back navigation gracefully
    BackHandler(enabled = uiState.currentScreen != ScreenState.HomeHub) {
        when (uiState.currentScreen) {
            ScreenState.ResultPreview -> viewModel.navigateToHome()
            ScreenState.StudioWorkspace -> viewModel.navigateToHome()
            ScreenState.HomeHub -> Unit
        }
    }

    Scaffold(
        containerColor = ObsidianBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.currentScreen == ScreenState.ResultPreview &&
                        uiState.upscaledBitmap != null &&
                        uiState.sourceBitmap != null -> {
                    ResultScreen(
                        sourceBitmap = uiState.sourceBitmap!!,
                        upscaledBitmap = uiState.upscaledBitmap!!,
                        scaleFactor = uiState.scaleFactor,
                        isSaving = uiState.isSaving,
                        onSave = { onDone -> viewModel.saveUpscaledImage(onDone) },
                        onReset = { viewModel.reset() }
                    )
                }

                uiState.currentScreen == ScreenState.StudioWorkspace &&
                        uiState.sourceBitmap != null -> {
                    StudioScreen(
                        sourceBitmap = uiState.sourceBitmap!!,
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
                        }
                    )
                }

                else -> {
                    HomeHubScreen(
                        uiState = uiState,
                        onSelectFeature = { feature ->
                            viewModel.selectFeatureMode(feature)
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onSelectRecent = { uri ->
                            viewModel.onImageSelected(uri)
                        }
                    )
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
