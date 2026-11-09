package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap

interface UpscalerEngine {
    val name: String
    val isNeuralActive: Boolean get() = false

    suspend fun upscale(
        sourceBitmap: Bitmap,
        config: TuningConfig = TuningConfig(),
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap

    suspend fun upscale(
        sourceBitmap: Bitmap,
        scaleFactor: Int,
        enhancementMode: EnhancementMode = EnhancementMode.Natural,
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap {
        val sharpness = if (enhancementMode == EnhancementMode.UltraDeblur) 0.85f else 0.40f
        val aa = if (enhancementMode == EnhancementMode.UltraDeblur) 0.35f else 0.65f
        return upscale(
            sourceBitmap = sourceBitmap,
            config = TuningConfig(
                scaleFactor = scaleFactor.toFloat(),
                sharpness = sharpness,
                antiAliasing = aa
            ),
            onProgress = onProgress
        )
    }
}
