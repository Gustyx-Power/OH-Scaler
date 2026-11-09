package id.xms.ohscaler.egg.core.ai

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import kotlin.coroutines.coroutineContext

class OnnxUpscalerEngine(
    private val fallbackEngine: UpscalerEngine,
    private val tileProcessor: TileProcessor = TileProcessor(tileSize = 256, overlapPadding = 16)
) : UpscalerEngine {

    override val name: String = "Real-ESRGAN Compact ONNX"
    override val isNeuralActive: Boolean get() = isModelLoaded

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    val isModelLoaded: Boolean
        get() = ortSession != null

    fun loadModel(bytes: ByteArray): Boolean {
        return try {
            val env = OrtEnvironment.getEnvironment()
            val options = OrtSession.SessionOptions().apply {
                val cores = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)
                setIntraOpNumThreads(cores)
                setInterOpNumThreads(cores)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }
            ortEnv = env
            ortSession = env.createSession(bytes, options)
            android.util.Log.d("OHScaler", "ONNX Session created successfully (Multi-thread CPU/NEON/AVX2).")
            true
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Failed to create ONNX session", e)
            closeSession()
            false
        }
    }

    private fun closeSession() {
        try {
            ortSession?.close()
        } catch (_: Exception) {}
        ortSession = null
    }

    override suspend fun upscale(
        sourceBitmap: Bitmap,
        config: TuningConfig,
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap = withContext(Dispatchers.Default) {
        val session = ortSession
        val env = ortEnv

        if (session == null || env == null) {
            return@withContext fallbackEngine.upscale(sourceBitmap, config, onProgress)
        }

        val startTime = System.currentTimeMillis()
        android.util.Log.d(
            "OHScaler",
            "OnnxUpscalerEngine.upscale starting! ${sourceBitmap.width}x${sourceBitmap.height}, " +
                    "scale=${config.scaleFactor}x, sharpness=${config.sharpness}, AA=${config.antiAliasing}, " +
                    "deblur=${config.preDeblurEnabled} (r=${config.deblurRadius}, c=${config.clarityStrength})"
        )
        onProgress(UpscaleProgress.ReadingPixels(sourceBitmap.width, sourceBitmap.height))

        try {
            val faceRects = if (config.faceAwareEnabled) {
                FaceDetectorHelper.detectFaces(sourceBitmap)
            } else {
                emptyList()
            }

            // Stage 1: Optional Pre-Deblurring (Crucial for out-of-focus portraits)
            val inputToUse = if (config.preDeblurEnabled && config.clarityStrength > 0.01f) {
                MultiScaleDeblur.applyDeepDeblur(
                    sourceBitmap,
                    deblurRadius = config.deblurRadius,
                    clarityStrength = config.clarityStrength,
                    antiHaloClamp = config.antiHaloClamp,
                    faceRects = faceRects
                )
            } else {
                sourceBitmap
            }

            // Direct 1x mode: return enhanced image without super-resolution enlargement
            if (config.scaleFactor <= 1.05f) {
                val tuned = ImageEnhancer.applyDetailTuning(
                    inputToUse,
                    sharpness = config.sharpness,
                    antiAliasing = config.antiAliasing
                )
                val duration = System.currentTimeMillis() - startTime
                onProgress(UpscaleProgress.Completed(tuned, duration))
                if (inputToUse != sourceBitmap && !inputToUse.isRecycled && inputToUse != tuned) {
                    inputToUse.recycle()
                }
                return@withContext tuned
            }

            // Stage 2: Neural AI Super-Resolution (Single-pass vs 2-Pass Cascaded for 16x)
            val result = if (config.scaleFactor >= 15.9f) {
                // 16x Cascaded: Pass 1 (4x) then Pass 2 (4x)
                val pass1Config = config.copy(scaleFactor = 4.0f, sharpness = 0.0f, antiAliasing = 0.0f)
                val pass1 = tileProcessor.processTiled(
                    sourceBitmap = inputToUse,
                    scaleFactor = 4.0f,
                    onProgress = { current, total, progress ->
                        onProgress(UpscaleProgress.Processing(current, total * 2, progress * 0.5f))
                    },
                    transformTile = { tile ->
                        coroutineContext.ensureActive()
                        runTileInference(env, session, tile, pass1Config)
                    }
                )

                val pass2 = tileProcessor.processTiled(
                    sourceBitmap = pass1,
                    scaleFactor = 4.0f,
                    onProgress = { current, total, progress ->
                        onProgress(UpscaleProgress.Processing(total + current, total * 2, 0.5f + progress * 0.5f))
                    },
                    transformTile = { tile ->
                        coroutineContext.ensureActive()
                        runTileInference(env, session, tile, config.copy(scaleFactor = 4.0f))
                    }
                )

                if (!pass1.isRecycled) pass1.recycle()
                pass2
            } else {
                // Standard Single-Pass Tiling (1.5x, 2x, 3x, 4x, 8x)
                tileProcessor.processTiled(
                    sourceBitmap = inputToUse,
                    scaleFactor = config.scaleFactor,
                    onProgress = { current, total, progress ->
                        onProgress(UpscaleProgress.Processing(current, total, progress))
                    },
                    transformTile = { tile ->
                        coroutineContext.ensureActive()
                        runTileInference(env, session, tile, config)
                    }
                )
            }

            if (inputToUse != sourceBitmap && !inputToUse.isRecycled) {
                inputToUse.recycle()
            }

            onProgress(UpscaleProgress.Stitching(1.0f))
            val duration = System.currentTimeMillis() - startTime
            android.util.Log.d(
                "OHScaler",
                "Neural upscale COMPLETED in ${duration}ms! Result: ${result.width}x${result.height}"
            )
            onProgress(UpscaleProgress.Completed(result, duration))
            result
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Neural inference failed, falling back to bicubic", e)
            onProgress(UpscaleProgress.Error("Neural inference fallback: ${e.localizedMessage}", e))
            fallbackEngine.upscale(sourceBitmap, config, onProgress)
        }
    }

    private fun runTileInference(
        env: OrtEnvironment,
        session: OrtSession,
        tile: Bitmap,
        config: TuningConfig
    ): Bitmap {
        val w = tile.width
        val h = tile.height
        val pixels = IntArray(w * h)
        tile.getPixels(pixels, 0, w, 0, 0, w, h)

        val channelSize = h * w
        val floatBuffer = FloatBuffer.allocate(1 * 3 * channelSize)
        val array = floatBuffer.array()

        val rOffset = 0
        val gOffset = channelSize
        val bOffset = 2 * channelSize

        for (i in 0 until channelSize) {
            val p = pixels[i]
            array[rOffset + i] = ((p shr 16) and 0xFF) / 255.0f
            array[gOffset + i] = ((p shr 8) and 0xFF) / 255.0f
            array[bOffset + i] = (p and 0xFF) / 255.0f
        }
        floatBuffer.rewind()

        val inputShape = longArrayOf(1, 3, h.toLong(), w.toLong())
        val inputTensor = OnnxTensor.createTensor(env, floatBuffer, inputShape)

        val inputName = session.inputNames.iterator().next()
        val results = session.run(mapOf(inputName to inputTensor))
        inputTensor.close()

        val outputTensor = results[0] as OnnxTensor
        val outShape = outputTensor.info.shape
        val outH = outShape[2].toInt()
        val outW = outShape[3].toInt()

        val outBuffer = outputTensor.floatBuffer
        val outArray = FloatArray(outBuffer.remaining())
        outBuffer.get(outArray)
        results.close()

        val outChannelSize = outW * outH
        val outPixels = IntArray(outChannelSize)
        val outROffset = 0
        val outGOffset = outChannelSize
        val outBOffset = 2 * outChannelSize

        for (i in 0 until outChannelSize) {
            val r = (outArray[outROffset + i].coerceIn(0f, 1f) * 255f).toInt()
            val g = (outArray[outGOffset + i].coerceIn(0f, 1f) * 255f).toInt()
            val b = (outArray[outBOffset + i].coerceIn(0f, 1f) * 255f).toInt()
            outPixels[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }

        val rawTile4x = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        rawTile4x.setPixels(outPixels, 0, outW, 0, 0, outW, outH)

        // Target scaling:
        // Real-ESRGAN Compact native output is 4x.
        // For non-4x (e.g. 1.5x, 2.0x, 3.0x, 8.0x), rescale tile:
        val targetScale = if (config.scaleFactor >= 15.9f) 4.0f else config.scaleFactor
        val targetW = (w * targetScale).toInt().coerceAtLeast(1)
        val targetH = (h * targetScale).toInt().coerceAtLeast(1)

        val scaledTile = if (targetW != outW || targetH != outH) {
            val scaled = Bitmap.createScaledBitmap(rawTile4x, targetW, targetH, true)
            if (scaled != rawTile4x && !rawTile4x.isRecycled) {
                rawTile4x.recycle()
            }
            scaled
        } else {
            rawTile4x
        }

        return if (config.sharpness > 0.01f || config.antiAliasing > 0.01f) {
            val tuned = ImageEnhancer.applyDetailTuning(
                bitmap = scaledTile,
                sharpness = config.sharpness,
                antiAliasing = config.antiAliasing
            )
            if (tuned != scaledTile && !scaledTile.isRecycled) {
                scaledTile.recycle()
            }
            tuned
        } else {
            scaledTile
        }
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
