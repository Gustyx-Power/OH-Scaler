package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap
import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

object MultiScaleDeblur {

    /**
     * Applies deep multi-scale luminance deblurring with anti-halo clamping.
     * Effectively recovers broad optical defocus blur (like portrait bokeh) and motion blur
     * by reconstructing edge gradients across multiple spatial scales on the Luminance (Y) channel.
     *
     * @param bitmap Input source bitmap
     * @param deblurRadius Base radius of blur spread (2.0f to 25.0f pixels)
     * @param clarityStrength Intensity of edge & micro-contrast recovery (0.0f to 1.5f)
     * @param antiHaloClamp Maximum luminance deviation to prevent white ring/halo artifacts (10.0f to 60.0f)
     * @param faceRects Optional detected facial bounding boxes for targeted eye & smile clarity boost
     */
    fun applyDeepDeblur(
        bitmap: Bitmap,
        deblurRadius: Float = 6.0f,
        clarityStrength: Float = 0.8f,
        antiHaloClamp: Float = 30.0f,
        faceRects: List<RectF> = emptyList()
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width < 4 || height < 4 || clarityStrength <= 0.01f) return bitmap

        val pixelCount = width * height
        val pixels = IntArray(pixelCount)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // 1. Convert RGB to Y, U, V channels
        val yChannel = FloatArray(pixelCount)
        val uChannel = FloatArray(pixelCount)
        val vChannel = FloatArray(pixelCount)

        for (i in 0 until pixelCount) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF

            yChannel[i] = 0.299f * r + 0.587f * g + 0.114f * b
            uChannel[i] = -0.14713f * r - 0.28886f * g + 0.436f * b
            vChannel[i] = 0.615f * r - 0.51499f * g - 0.10001f * b
        }

        // 2. Multi-Scale Separable Box Blurs (Approximating Gaussian Pyramids in O(N))
        val r1 = max(1, (deblurRadius * 0.4f).toInt())
        val r2 = max(2, (deblurRadius * 1.0f).toInt())
        val r3 = max(3, (deblurRadius * 2.2f).toInt())

        val blur1 = fastBoxBlur(yChannel, width, height, r1)
        val blur2 = fastBoxBlur(yChannel, width, height, r2)
        val blur3 = fastBoxBlur(yChannel, width, height, r3)

        // 3. Multi-Scale Edge Reconstruction with Anti-Halo Clamping and Face Boost
        val yEnhanced = FloatArray(pixelCount)
        val weights = floatArrayOf(1.3f, 1.0f, 0.6f)
        val coringThreshold = 1.5f

        for (y in 0 until height) {
            val rowOffset = y * width
            for (x in 0 until width) {
                val idx = rowOffset + x
                val origY = yChannel[idx]

                val diff1 = origY - blur1[idx]
                val diff2 = origY - blur2[idx]
                val diff3 = origY - blur3[idx]

                // Soft coring: suppress sensor noise below threshold
                val soft1 = softCore(diff1, coringThreshold) * weights[0]
                val soft2 = softCore(diff2, coringThreshold) * weights[1]
                val soft3 = softCore(diff3, coringThreshold) * weights[2]

                // Calculate face multiplier if pixel is inside any face ROI
                var faceMultiplier = 1.0f
                if (faceRects.isNotEmpty()) {
                    for (rect in faceRects) {
                        if (rect.contains(x.toFloat(), y.toFloat())) {
                            faceMultiplier = 1.4f // 40% boost for facial features (eyes, teeth)
                            break
                        }
                    }
                }

                val totalBoost = (soft1 + soft2 + soft3) * clarityStrength * faceMultiplier
                val clampedBoost = totalBoost.coerceIn(-antiHaloClamp, antiHaloClamp)
                yEnhanced[idx] = (origY + clampedBoost).coerceIn(0f, 255f)
            }
        }

        // 4. Reconstruct RGB from Y_enhanced, U, V
        val outputPixels = IntArray(pixelCount)
        for (i in 0 until pixelCount) {
            val yVal = yEnhanced[i]
            val uVal = uChannel[i]
            val vVal = vChannel[i]

            val r = (yVal + 1.13983f * vVal).toInt().coerceIn(0, 255)
            val g = (yVal - 0.39465f * uVal - 0.58060f * vVal).toInt().coerceIn(0, 255)
            val b = (yVal + 2.03211f * uVal).toInt().coerceIn(0, 255)

            outputPixels[i] = -0x1000000 or (r shl 16) or (g shl 8) or b
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return result
    }

    private fun softCore(diff: Float, threshold: Float): Float {
        val absVal = abs(diff)
        return if (absVal <= threshold) {
            0f
        } else {
            sign(diff) * (absVal - threshold)
        }
    }

    /**
     * Fast 3-pass separable box filter.
     * Takes O(1) operations per pixel regardless of radius.
     */
    fun fastBoxBlur(input: FloatArray, width: Int, height: Int, radius: Int): FloatArray {
        var current = input.copyOf()
        // 3 passes of box blur yields high-quality bell curve (Central Limit Theorem)
        for (pass in 0 until 3) {
            val hBlur = FloatArray(width * height)
            val vBlur = FloatArray(width * height)

            // Horizontal pass
            for (y in 0 until height) {
                val rowOffset = y * width
                var sum = 0f
                val windowSize = radius * 2 + 1

                for (i in -radius..radius) {
                    val clampedX = min(max(i, 0), width - 1)
                    sum += current[rowOffset + clampedX]
                }

                for (x in 0 until width) {
                    hBlur[rowOffset + x] = sum / windowSize
                    val removeX = min(max(x - radius, 0), width - 1)
                    val addX = min(max(x + radius + 1, 0), width - 1)
                    sum += current[rowOffset + addX] - current[rowOffset + removeX]
                }
            }

            // Vertical pass
            for (x in 0 until width) {
                var sum = 0f
                val windowSize = radius * 2 + 1

                for (i in -radius..radius) {
                    val clampedY = min(max(i, 0), height - 1)
                    sum += hBlur[clampedY * width + x]
                }

                for (y in 0 until height) {
                    vBlur[y * width + x] = sum / windowSize
                    val removeY = min(max(y - radius, 0), height - 1)
                    val addY = min(max(y + radius + 1, 0), height - 1)
                    sum += hBlur[addY * width + x] - hBlur[removeY * width + x]
                }
            }
            current = vBlur
        }
        return current
    }
}
