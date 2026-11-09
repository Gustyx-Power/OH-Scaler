package id.xms.ohscaler.egg.core.ai

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.nio.FloatBuffer
import kotlin.coroutines.coroutineContext

/**
 * OnnxUpscalerEngine runs super-resolution models (e.g. Real-ESRGAN Compact / ESRCNN)
 * on Android hardware accelerators via ONNX Runtime Mobile and NNAPI / CPU XNNPACK.
 */
class OnnxUpscalerEngine(
    private val modelBytes: ByteArray? = null,
    private val tileProcessor: TileProcessor = TileProcessor(tileSize = 256, overlapPadding = 16),
    private val fallbackEngine: FallbackUpscalerEngine = FallbackUpscalerEngine(tileProcessor)
) : UpscalerEngine {

    override val name: String = "ONNX Neural Super-Resolution (Real-ESRGAN / NNAPI)"

    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    init {
        initSession()
    }

    private fun initSession() {
        if (modelBytes == null || modelBytes.isEmpty()) return
        try {
            val env = OrtEnvironment.getEnvironment()
            val options = OrtSession.SessionOptions().apply {
                try {
                    addNnapi()
                } catch (_: Exception) {
                    // NNAPI not available on some emulators, fallback to CPU
                }
                setIntraOpNumThreads(4)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)
            }
            ortEnv = env
            ortSession = env.createSession(modelBytes, options)
        } catch (_: Exception) {
            ortSession = null
        }
    }

    override suspend fun upscale(
        sourceBitmap: Bitmap,
        scaleFactor: Int,
        onProgress: (UpscaleProgress) -> Unit
    ): Bitmap = withContext(Dispatchers.Default) {
        val session = ortSession
        val env = ortEnv

        if (session == null || env == null) {
            // Gracefully use high-precision fallback engine if model is not yet loaded
            return@withContext fallbackEngine.upscale(sourceBitmap, scaleFactor, onProgress)
        }

        val startTime = System.currentTimeMillis()
        onProgress(UpscaleProgress.ReadingPixels(sourceBitmap.width, sourceBitmap.height))

        try {
            val result = tileProcessor.processTiled(
                sourceBitmap = sourceBitmap,
                scaleFactor = scaleFactor,
                onProgress = { current, total, progress ->
                    onProgress(UpscaleProgress.Processing(current, total, progress))
                },
                transformTile = { tile ->
                    coroutineContext.ensureActive()
                    runTileInference(env, session, tile, scaleFactor)
                }
            )

            onProgress(UpscaleProgress.Stitching(1.0f))
            val duration = System.currentTimeMillis() - startTime
            onProgress(UpscaleProgress.Completed(result, duration))
            result
        } catch (e: Exception) {
            onProgress(UpscaleProgress.Error("Inference failed: ${e.localizedMessage}", e))
            fallbackEngine.upscale(sourceBitmap, scaleFactor, onProgress)
        }
    }

    private fun runTileInference(
        env: OrtEnvironment,
        session: OrtSession,
        tile: Bitmap,
        scaleFactor: Int
    ): Bitmap {
        val w = tile.width
        val h = tile.height
        val pixels = IntArray(w * h)
        tile.getPixels(pixels, 0, w, 0, 0, w, h)

        // Convert to NCHW float tensor [1, 3, H, W]
        val floatBuffer = FloatBuffer.allocate(1 * 3 * h * w)
        val channelSize = h * w

        val rOffset = 0
        val gOffset = channelSize
        val bOffset = 2 * channelSize

        val array = floatBuffer.array()
        for (i in 0 until channelSize) {
            val pixel = pixels[i]
            array[rOffset + i] = Color.red(pixel) / 255.0f
            array[gOffset + i] = Color.green(pixel) / 255.0f
            array[bOffset + i] = Color.blue(pixel) / 255.0f
        }

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

        val outPixels = IntArray(outW * outH)
        val outChannelSize = outW * outH
        val outROffset = 0
        val outGOffset = outChannelSize
        val outBOffset = 2 * outChannelSize

        for (i in 0 until outChannelSize) {
            val r = (outArray[outROffset + i].coerceIn(0f, 1f) * 255f).toInt()
            val g = (outArray[outGOffset + i].coerceIn(0f, 1f) * 255f).toInt()
            val b = (outArray[outBOffset + i].coerceIn(0f, 1f) * 255f).toInt()
            outPixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }

        val outBitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        outBitmap.setPixels(outPixels, 0, outW, 0, 0, outW, outH)
        return outBitmap
    }
}
