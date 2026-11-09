package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap
import kotlin.math.abs

object ImageEnhancer {
    /**
     * Applies an adaptive edge deblurring and contrast enhancement filter.
     * Selectively enhances high-frequency edge gradients while preserving smooth surfaces
     * to avoid boosting background sensor noise.
     */
    fun applyAdaptiveDeblur(
        bitmap: Bitmap,
        intensity: Float = 0.35f,
        edgeThreshold: Int = 10
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width < 3 || height < 3) return bitmap

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val outputPixels = pixels.clone()

        val clampedIntensity = intensity.coerceIn(0.1f, 1.0f)

        for (y in 1 until height - 1) {
            val rowOffset = y * width
            val topOffset = (y - 1) * width
            val bottomOffset = (y + 1) * width

            for (x in 1 until width - 1) {
                val idx = rowOffset + x
                val centerPixel = pixels[idx]
                val cr = (centerPixel shr 16) and 0xFF
                val cg = (centerPixel shr 8) and 0xFF
                val cb = centerPixel and 0xFF

                // 4-neighborhood inspection
                val topPixel = pixels[topOffset + x]
                val bottomPixel = pixels[bottomOffset + x]
                val leftPixel = pixels[rowOffset + x - 1]
                val rightPixel = pixels[rowOffset + x + 1]

                val tr = (topPixel shr 16) and 0xFF
                val br = (bottomPixel shr 16) and 0xFF
                val lr = (leftPixel shr 16) and 0xFF
                val rr = (rightPixel shr 16) and 0xFF

                val tg = (topPixel shr 8) and 0xFF
                val bg = (bottomPixel shr 8) and 0xFF
                val lg = (leftPixel shr 8) and 0xFF
                val rg = (rightPixel shr 8) and 0xFF

                val tb = topPixel and 0xFF
                val bb = bottomPixel and 0xFF
                val lb = leftPixel and 0xFF
                val rb = rightPixel and 0xFF

                val gradR = abs(cr - tr) + abs(cr - br) + abs(cr - lr) + abs(cr - rr)
                val gradG = abs(cg - tg) + abs(cg - bg) + abs(cg - lg) + abs(cg - rg)
                val gradB = abs(cb - tb) + abs(cb - bb) + abs(cb - lb) + abs(cb - rb)
                val avgGrad = (gradR + gradG + gradB) / 3

                if (avgGrad > edgeThreshold) {
                    val lapR = 4 * cr - tr - br - lr - rr
                    val lapG = 4 * cg - tg - bg - lg - rg
                    val lapB = 4 * cb - tb - bb - lb - rb

                    val newR = (cr + (lapR * clampedIntensity)).toInt().coerceIn(0, 255)
                    val newG = (cg + (lapG * clampedIntensity)).toInt().coerceIn(0, 255)
                    val newB = (cb + (lapB * clampedIntensity)).toInt().coerceIn(0, 255)

                    outputPixels[idx] = -0x1000000 or (newR shl 16) or (newG shl 8) or newB
                }
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return result
    }
}
