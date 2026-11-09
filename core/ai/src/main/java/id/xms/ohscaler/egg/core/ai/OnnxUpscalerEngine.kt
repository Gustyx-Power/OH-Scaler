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

/**
 * OnnxUpscalerEngine runs super-resolution models (e.g. Real-ESRGAN Compact v3)
 * on Android hardware accelerators via ONNX Runtime Mobile and CPU SIMD (NEON/AVX2).
 */
class OnnxUpscalerEngine(
    private var modelBytes: ByteArray? = null,
    private val tileProcessor: TileProcessor = TileProcessor(tileSize = 256, overlapPadding = 16),
    private val fallbackEngine: FallbackUpscalerEngine = FallbackUpscalerEngine(tileProcessor)
) : UpscalerEngine {

    override val name: String = "ONNX Neural Super-Resolution (Real-ESRGAN Compact v3)"

    override val isNeuralActive: Boolean
        get() = ortSession != null

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    init {
        initSession()
    }

    @Synchronized
    fun loadModel(bytes: ByteArray): Boolean {
        this.modelBytes = bytes
        closeSession()
        return initSession()
    }

    private fun initSession(): Boolean {
        val bytes = modelBytes ?: return false
        if (bytes.isEmpty()) return false
        return try {
            val env = OrtEnvironment.getEnvironment()
            val options = OrtSession.SessionOptions().apply {
                setIntraOpNumThreads(4)
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
            // Gracefully use fallback engine if model is not yet loaded
            return@withContext fallbackEngine.upscale(sourceBitmap, config, onProgress)
        }

        val startTime = System.currentTimeMillis()
        android.util.Log.d(
            "OHScaler",
            "OnnxUpscalerEngine.upscale starting! ${sourceBitmap.width}x${sourceBitmap.height}, " +
                    "scale=${config.scaleFactor}x, sharpness=${config.sharpness}, AA=${config.antiAliasing}"
        )
        onProgress(UpscaleProgress.ReadingPixels(sourceBitmap.width, sourceBitmap.height))

        try {
            val result = tileProcessor.processTiled(
                sourceBitmap = sourceBitmap,
                scaleFactor = config.scaleFactor,
                onProgress = { current, total, progress ->
                    android.util.Log.d("OHScaler", "Tile progress: $current/$total ($progress)")
                    onProgress(UpscaleProgress.Processing(current, total, progress))
                },
                transformTile = { tile ->
                    coroutineContext.ensureActive()
                    runTileInference(env, session, tile, config)
                }
            )

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

        // Convert to NCHW float tensor [1, 3, H, W] in [0.0, 1.0]
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
        val outShape = outputTensor.info.shape // [1, 3, H_out, W_out]
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
        // Real-ESRGAN Compact output is 4x.
        // For scaleFactor != 4.0f (e.g. 1.5x, 2.0x, 3.0x, 8.0x), rescale tile accordingly:
        val targetW = (w * config.scaleFactor).toInt().coerceAtLeast(1)
        val targetH = (h * config.scaleFactor).toInt().coerceAtLeast(1)

        val scaledTile = if (targetW != outW || targetH != outH) {
            val scaled = Bitmap.createScaledBitmap(rawTile4x, targetW, targetH, true)
            if (scaled != rawTile4x && !rawTile4x.isRecycled) {
                rawTile4x.recycle()
            }
            scaled
        } else {
            rawTile4x
        }

        // Apply custom detail tuning (Sharpness + Directional Anti-Aliasing)
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
}
