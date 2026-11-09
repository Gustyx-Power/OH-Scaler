package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * FallbackUpscalerEngine provides high-quality interpolation super-resolution
 * utilizing the same tile-based processing pipeline as neural engines.
 * Ensures the app works reliably even without heavy external weight files.
 */
class FallbackUpscalerEngine(
    private val tileProcessor: TileProcessor = TileProcessor(tileSize = 384, overlapPadding = 16)
) : UpscalerEngine {

    override val name: String = "High-Precision Interpolation (Bicubic Fallback)"
    override val isNeuralActive: Boolean = false

    override suspend fun upscale(
        sourceBitmap: Bitmap,
        config: TuningConfig,
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        onProgress(UpscaleProgress.ReadingPixels(sourceBitmap.width, sourceBitmap.height))
        delay(60)

        val result = tileProcessor.processTiled(
            sourceBitmap = sourceBitmap,
            scaleFactor = config.scaleFactor,
            onProgress = { current, total, progress ->
                onProgress(UpscaleProgress.Processing(current, total, progress))
            },
            transformTile = { tile ->
                val outW = (tile.width * config.scaleFactor).toInt().coerceAtLeast(1)
                val outH = (tile.height * config.scaleFactor).toInt().coerceAtLeast(1)
                val scaledTile = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(scaledTile)
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

                val srcRect = Rect(0, 0, tile.width, tile.height)
                val dstRect = Rect(0, 0, outW, outH)
                canvas.drawBitmap(tile, srcRect, dstRect, paint)

                if (config.sharpness > 0.05f || config.antiAliasing > 0.05f) {
                    val enhanced = ImageEnhancer.applyDetailTuning(
                        bitmap = scaledTile,
                        sharpness = config.sharpness,
                        antiAliasing = config.antiAliasing
                    )
                    if (enhanced != scaledTile && !scaledTile.isRecycled) {
                        scaledTile.recycle()
                    }
                    enhanced
                } else {
                    scaledTile
                }
            }
        )

        onProgress(UpscaleProgress.Stitching(1.0f))
        val duration = System.currentTimeMillis() - startTime
        onProgress(UpscaleProgress.Completed(result, duration))

        result
    }
}
