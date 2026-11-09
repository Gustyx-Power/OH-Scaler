package id.xms.ohscaler.egg.feature.upscaler

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.xms.ohscaler.egg.core.ai.EnhancementMode
import id.xms.ohscaler.egg.core.ai.FallbackUpscalerEngine
import id.xms.ohscaler.egg.core.ai.OnnxUpscalerEngine
import id.xms.ohscaler.egg.core.ai.TuningConfig
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.storage.AppPreferences
import id.xms.ohscaler.egg.core.storage.AppThemeMode
import id.xms.ohscaler.egg.core.storage.ImageDecoderHelper
import id.xms.ohscaler.egg.core.storage.ScopedStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ScreenState {
    Onboarding,
    HomeHub,
    StudioWorkspace,
    ResultPreview
}

enum class FeatureMode {
    SuperResolution,
    DeblurRestore,
    ProStudio
}

enum class TuningPreset {
    Balanced,
    SmoothPortrait,
    CrispGraphic,
    Custom
}

data class UpscalerUiState(
    val currentScreen: ScreenState = ScreenState.HomeHub,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val activeFeatureMode: FeatureMode = FeatureMode.SuperResolution,
    val sourceUri: Uri? = null,
    val sourceBitmap: Bitmap? = null,
    val upscaledBitmap: Bitmap? = null,
    val scaleFactor: Float = 2.0f,
    val sharpness: Float = 0.5f,
    val antiAliasing: Float = 0.5f,
    val tuningPreset: TuningPreset = TuningPreset.Balanced,
    val enhancementMode: EnhancementMode = EnhancementMode.Natural,
    val isEngineReady: Boolean = false,
    val progress: UpscaleProgress = UpscaleProgress.Idle,
    val isSaving: Boolean = false,
    val savedUri: Uri? = null,
    val recentCreations: List<Uri> = emptyList(),
    val errorMessage: String? = null
)

class UpscalerViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AppPreferences(application)
    private val storageManager = ScopedStorageManager(application)
    private val onnxEngine = OnnxUpscalerEngine(
        fallbackEngine = FallbackUpscalerEngine()
    )

    private val _uiState = MutableStateFlow(
        UpscalerUiState(
            currentScreen = if (preferences.isOnboardingCompleted()) ScreenState.HomeHub else ScreenState.Onboarding,
            themeMode = preferences.getThemeMode()
        )
    )
    val uiState: StateFlow<UpscalerUiState> = _uiState.asStateFlow()

    private var upscaleJob: Job? = null

    init {
        loadModelAsync()
        loadRecentCreations()
    }

    private fun loadModelAsync() {
        viewModelScope.launch {
            val ready = withContext(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    val modelName = "models/realesr-general-x4v3.onnx"
                    context.assets.open(modelName).use { inputStream ->
                        val bytes = inputStream.readBytes()
                        onnxEngine.loadModel(bytes)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("OHScaler", "Error loading ONNX model from assets", e)
                    false
                }
            }
            _uiState.update { it.copy(isEngineReady = ready) }
            android.util.Log.d("OHScaler", "ONNX Model loaded into engine. isEngineReady=$ready")
        }
    }

    fun toggleTheme() {
        val current = _uiState.value.themeMode
        val newMode = if (current == AppThemeMode.LIGHT) AppThemeMode.DARK else AppThemeMode.LIGHT
        preferences.setThemeMode(newMode)
        _uiState.update { it.copy(themeMode = newMode) }
    }

    fun completeOnboarding() {
        preferences.setOnboardingCompleted(true)
        _uiState.update { it.copy(currentScreen = ScreenState.HomeHub) }
    }

    fun reopenOnboarding() {
        _uiState.update { it.copy(currentScreen = ScreenState.Onboarding) }
    }

    fun loadRecentCreations() {
        viewModelScope.launch {
            val recent = storageManager.loadRecentCreations(limit = 10)
            _uiState.update { it.copy(recentCreations = recent) }
        }
    }

    fun selectFeatureMode(mode: FeatureMode) {
        val (sharpness, antiAliasing, preset) = when (mode) {
            FeatureMode.SuperResolution -> Triple(0.4f, 0.6f, TuningPreset.Balanced)
            FeatureMode.DeblurRestore -> Triple(0.85f, 0.3f, TuningPreset.CrispGraphic)
            FeatureMode.ProStudio -> Triple(0.5f, 0.5f, TuningPreset.Custom)
        }
        _uiState.update {
            it.copy(
                activeFeatureMode = mode,
                sharpness = sharpness,
                antiAliasing = antiAliasing,
                tuningPreset = preset
            )
        }
    }

    fun navigateToHome() {
        _uiState.update { it.copy(currentScreen = ScreenState.HomeHub) }
    }

    fun onImageSelected(uri: Uri) {
        android.util.Log.d("OHScaler", "onImageSelected called with: $uri")
        viewModelScope.launch {
            try {
                val bitmap = ImageDecoderHelper.decodeBitmapFromUri(getApplication(), uri)
                android.util.Log.d("OHScaler", "Decoded bitmap: $bitmap (${bitmap?.width}x${bitmap?.height})")
                _uiState.update {
                    it.copy(
                        sourceUri = uri,
                        sourceBitmap = bitmap,
                        upscaledBitmap = null,
                        progress = UpscaleProgress.Idle,
                        savedUri = null,
                        errorMessage = null,
                        currentScreen = ScreenState.StudioWorkspace
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("OHScaler", "Error decoding bitmap from uri: $uri", e)
            }
        }
    }

    fun setScaleFactor(scale: Float) {
        _uiState.update { it.copy(scaleFactor = scale) }
    }

    fun setScaleFactor(scale: Int) {
        setScaleFactor(scale.toFloat())
    }

    fun setSharpness(value: Float) {
        _uiState.update { it.copy(sharpness = value.coerceIn(0f, 1f), tuningPreset = TuningPreset.Custom) }
    }

    fun setAntiAliasing(value: Float) {
        _uiState.update { it.copy(antiAliasing = value.coerceIn(0f, 1f), tuningPreset = TuningPreset.Custom) }
    }

    fun applyPreset(preset: TuningPreset) {
        _uiState.update {
            when (preset) {
                TuningPreset.Balanced -> it.copy(tuningPreset = preset, sharpness = 0.5f, antiAliasing = 0.5f)
                TuningPreset.SmoothPortrait -> it.copy(tuningPreset = preset, sharpness = 0.3f, antiAliasing = 0.8f)
                TuningPreset.CrispGraphic -> it.copy(tuningPreset = preset, sharpness = 0.85f, antiAliasing = 0.3f)
                TuningPreset.Custom -> it.copy(tuningPreset = preset)
            }
        }
    }

    fun setEnhancementMode(mode: EnhancementMode) {
        _uiState.update {
            val sharpness = if (mode == EnhancementMode.UltraDeblur) 0.85f else 0.4f
            val aa = if (mode == EnhancementMode.UltraDeblur) 0.3f else 0.6f
            it.copy(enhancementMode = mode, sharpness = sharpness, antiAliasing = aa)
        }
    }

    fun startUpscale() {
        val bitmap = _uiState.value.sourceBitmap ?: return
        val config = TuningConfig(
            scaleFactor = _uiState.value.scaleFactor,
            sharpness = _uiState.value.sharpness,
            antiAliasing = _uiState.value.antiAliasing
        )
        android.util.Log.d(
            "OHScaler",
            "startUpscale called! bitmap=${bitmap.width}x${bitmap.height}, config=$config"
        )

        upscaleJob?.cancel()
        upscaleJob = viewModelScope.launch {
            try {
                val result = onnxEngine.upscale(
                    sourceBitmap = bitmap,
                    config = config,
                    onProgress = { progress ->
                        _uiState.update { it.copy(progress = progress) }
                    }
                )
                _uiState.update {
                    it.copy(
                        upscaledBitmap = result,
                        progress = UpscaleProgress.Completed(result, 0),
                        currentScreen = ScreenState.ResultPreview
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(progress = UpscaleProgress.Error(e.localizedMessage ?: "Unknown error", e))
                }
            }
        }
    }

    fun cancelUpscale() {
        upscaleJob?.cancel()
        upscaleJob = null
        _uiState.update { it.copy(progress = UpscaleProgress.Idle) }
    }

    fun saveUpscaledImage(onResult: (Boolean) -> Unit) {
        val bitmap = _uiState.value.upscaledBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val savedUri = storageManager.saveBitmapToGallery(bitmap)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    savedUri = savedUri
                )
            }
            if (savedUri != null) {
                loadRecentCreations()
            }
            onResult(savedUri != null)
        }
    }

    fun reset() {
        upscaleJob?.cancel()
        upscaleJob = null
        _uiState.update {
            it.copy(
                sourceUri = null,
                sourceBitmap = null,
                upscaledBitmap = null,
                progress = UpscaleProgress.Idle,
                savedUri = null,
                errorMessage = null,
                currentScreen = ScreenState.HomeHub
            )
        }
    }
}
