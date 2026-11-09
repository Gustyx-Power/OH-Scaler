package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FallbackUpscalerEngine(
    private val tileProcessor: TileProcessor = TileProcessor(tileSize = 512, overlapPadding = 16)
) : UpscalerEngine {

    override val name: String = "Bicubic & Deblur Fallback"

    override suspend fun upscale(
        sourceBitmap: Bitmap,
        config: TuningConfig,
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        onProgress(UpscaleProgress.ReadingPixels(sourceBitmap.width, sourceBitmap.height))

        val faceRects = if (config.faceAwareEnabled) {
            FaceDetectorHelper.detectFaces(sourceBitmap)
        } else {
            emptyList()
        }

        val inputToUse = if (config.preDeblurEnabled && config.clarityStrength > 0.01f) {
            MultiScaleDeblur.applyDeepDeblur(
                sourceBitmap,
                config.deblurRadius,
                config.clarityStrength,
                config.antiHaloClamp,
                faceRects
            )
        } else {
            sourceBitmap
        }

        if (config.scaleFactor <= 1.05f) {
            val tuned = ImageEnhancer.applyDetailTuning(
                inputToUse,
                config.sharpness,
                config.antiAliasing
            )
            val duration = System.currentTimeMillis() - startTime
            onProgress(UpscaleProgress.Completed(tuned, duration))
            if (inputToUse != sourceBitmap && !inputToUse.isRecycled && inputToUse != tuned) {
                inputToUse.recycle()
            }
            return@withContext tuned
        }

        val result = tileProcessor.processTiled(
            sourceBitmap = inputToUse,
            scaleFactor = config.scaleFactor,
            onProgress = { current, total, progress ->
                onProgress(UpscaleProgress.Processing(current, total, progress))
            },
            transformTile = { tile ->
                val targetW = (tile.width * config.scaleFactor).toInt().coerceAtLeast(1)
                val targetH = (tile.height * config.scaleFactor).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(tile, targetW, targetH, true)
                ImageEnhancer.applyDetailTuning(
                    bitmap = scaled,
                    sharpness = config.sharpness,
                    antiAliasing = config.antiAliasing
                )
            }
        )

        if (inputToUse != sourceBitmap && !inputToUse.isRecycled) {
            inputToUse.recycle()
        }

        onProgress(UpscaleProgress.Stitching(1.0f))
        val duration = System.currentTimeMillis() - startTime
        onProgress(UpscaleProgress.Completed(result, duration))
        result
    }

    override suspend fun upscale(
        sourceBitmap: Bitmap,
        scaleFactor: Int,
        enhancementMode: EnhancementMode,
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap {
        val sharpness = if (enhancementMode == EnhancementMode.UltraDeblur) 0.85f else 0.4f
        val aa = if (enhancementMode == EnhancementMode.UltraDeblur) 0.3f else 0.6f
        val config = TuningConfig(
            scaleFactor = scaleFactor.toFloat(),
            sharpness = sharpness,
            antiAliasing = aa,
            preDeblurEnabled = enhancementMode == EnhancementMode.UltraDeblur
        )
        return upscale(sourceBitmap, config, onProgress)
    }
}
