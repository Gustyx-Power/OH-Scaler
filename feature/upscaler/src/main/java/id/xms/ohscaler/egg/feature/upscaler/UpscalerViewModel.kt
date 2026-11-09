package id.xms.ohscaler.egg.feature.upscaler

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.xms.ohscaler.egg.core.ai.FallbackUpscalerEngine
import id.xms.ohscaler.egg.core.ai.OnnxUpscalerEngine
import id.xms.ohscaler.egg.core.ai.UpscaleProgress
import id.xms.ohscaler.egg.core.ai.UpscalerEngine
import id.xms.ohscaler.egg.core.storage.ImageDecoderHelper
import id.xms.ohscaler.egg.core.storage.ScopedStorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UpscalerUiState(
    val sourceUri: Uri? = null,
    val sourceBitmap: Bitmap? = null,
    val upscaledBitmap: Bitmap? = null,
    val scaleFactor: Int = 2,
    val progress: UpscaleProgress = UpscaleProgress.Idle,
    val isSaving: Boolean = false,
    val savedUri: Uri? = null,
    val errorMessage: String? = null
)

class UpscalerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UpscalerUiState())
    val uiState: StateFlow<UpscalerUiState> = _uiState.asStateFlow()

    private val storageManager = ScopedStorageManager(application)
    private var upscalerEngine: UpscalerEngine = OnnxUpscalerEngine(
        fallbackEngine = FallbackUpscalerEngine()
    )

    private var upscaleJob: Job? = null

    fun onImageSelected(uri: Uri) {
        android.util.Log.d("OHScaler", "onImageSelected called with: $uri")
        viewModelScope.launch {
            try {
                val bitmap = ImageDecoderHelper.decodeBitmapFromUri(getApplication(), uri)
                android.util.Log.d("OHScaler", "Decoded bitmap: $bitmap (width=${bitmap?.width}, height=${bitmap?.height})")
                _uiState.update {
                    it.copy(
                        sourceUri = uri,
                        sourceBitmap = bitmap,
                        upscaledBitmap = null,
                        progress = UpscaleProgress.Idle,
                        savedUri = null,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("OHScaler", "Error decoding bitmap from uri: $uri", e)
            }
        }
    }

    fun setScaleFactor(scale: Int) {
        _uiState.update { it.copy(scaleFactor = scale) }
    }

    fun startUpscale() {
        val bitmap = _uiState.value.sourceBitmap ?: return
        val scale = _uiState.value.scaleFactor

        upscaleJob?.cancel()
        upscaleJob = viewModelScope.launch {
            try {
                val result = upscalerEngine.upscale(
                    sourceBitmap = bitmap,
                    scaleFactor = scale,
                    onProgress = { progress ->
                        _uiState.update { it.copy(progress = progress) }
                    }
                )
                _uiState.update {
                    it.copy(
                        upscaledBitmap = result,
                        progress = UpscaleProgress.Completed(result, 0)
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
            onResult(savedUri != null)
        }
    }

    fun reset() {
        upscaleJob?.cancel()
        upscaleJob = null
        _uiState.update {
            UpscalerUiState()
        }
    }
}
