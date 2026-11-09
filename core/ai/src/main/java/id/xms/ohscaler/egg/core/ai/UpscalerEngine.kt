package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap

interface UpscalerEngine {
    val name: String

    suspend fun upscale(
        sourceBitmap: Bitmap,
        scaleFactor: Int,
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap
}
