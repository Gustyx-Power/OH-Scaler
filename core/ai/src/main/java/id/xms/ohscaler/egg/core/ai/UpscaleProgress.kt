package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap

sealed interface UpscaleProgress {
    data object Idle : UpscaleProgress

    data class ReadingPixels(
        val sourceWidth: Int,
        val sourceHeight: Int
    ) : UpscaleProgress

    data class Processing(
        val currentTile: Int,
        val totalTiles: Int,
        val progressFloat: Float
    ) : UpscaleProgress

    data class Stitching(
        val progressFloat: Float
    ) : UpscaleProgress

    data class Completed(
        val resultBitmap: Bitmap,
        val executionTimeMs: Long
    ) : UpscaleProgress

    data class Error(
        val errorMessage: String,
        val exception: Throwable? = null
    ) : UpscaleProgress
}
