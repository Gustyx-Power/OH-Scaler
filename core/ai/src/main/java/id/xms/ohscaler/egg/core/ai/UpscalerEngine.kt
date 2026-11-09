package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap

interface UpscalerEngine {
    val name: String
    val isNeuralActive: Boolean get() = false

    suspend fun upscale(
        sourceBitmap: Bitmap,
        scaleFactor: Int,
        enhancementMode: EnhancementMode = EnhancementMode.Natural,
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap
}
