package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap
import kotlin.math.abs

object ImageEnhancer {

    /**
     * Applies parametric detail tuning:
     * @param sharpness Intensity of adaptive high-frequency edge enhancement (0.0f to 1.0f).
     * @param antiAliasing Intensity of directional edge smoothing to remove jaggies/staircase artifacts (0.0f to 1.0f).
     */
    fun applyDetailTuning(
        bitmap: Bitmap,
        sharpness: Float = 0.5f,
        antiAliasing: Float = 0.5f,
        edgeThreshold: Int = 10
    ): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width < 3 || height < 3) return bitmap
        if (sharpness <= 0.01f && antiAliasing <= 0.01f) return bitmap

        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val outputPixels = pixels.clone()

        val clampedSharpness = sharpness.coerceIn(0.0f, 1.0f) * 0.7f // max boost factor 0.7
        val clampedAA = antiAliasing.coerceIn(0.0f, 1.0f) * 0.45f      // max smoothing blend 0.45

        for (y in 1 until height - 1) {
            val rowOffset = y * width
            val topOffset = (y - 1) * width
            val bottomOffset = (y + 1) * width

            for (x in 1 until width - 1) {
                val idx = rowOffset + x
                val centerPixel = pixels[idx]
                var cr = (centerPixel shr 16) and 0xFF
                var cg = (centerPixel shr 8) and 0xFF
                var cb = centerPixel and 0xFF

                // 4-neighborhood
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

                val gradH = (abs(lr - rr) + abs(lg - rg) + abs(lb - rb)) / 3
                val gradV = (abs(tr - br) + abs(tg - bg) + abs(tb - bb)) / 3
                val totalGrad = gradH + gradV

                if (totalGrad > edgeThreshold) {
                    // 1. Anti-aliasing pass: Directional edge blend for diagonal staircases
                    if (clampedAA > 0.01f) {
                        // Average color of orthogonal neighbors
                        val avgR = (tr + br + lr + rr) shr 2
                        val avgG = (tg + bg + lg + rg) shr 2
                        val avgB = (tb + bb + lb + rb) shr 2

                        cr = (cr * (1.0f - clampedAA) + avgR * clampedAA).toInt()
                        cg = (cg * (1.0f - clampedAA) + avgG * clampedAA).toInt()
                        cb = (cb * (1.0f - clampedAA) + avgB * clampedAA).toInt()
                    }

                    // 2. Adaptive sharpness pass: Laplacian high-frequency edge contrast
                    if (clampedSharpness > 0.01f) {
                        val lapR = 4 * cr - tr - br - lr - rr
                        val lapG = 4 * cg - tg - bg - lg - rg
                        val lapB = 4 * cb - tb - bb - lb - rb

                        cr = (cr + (lapR * clampedSharpness)).toInt()
                        cg = (cg + (lapG * clampedSharpness)).toInt()
                        cb = (cb + (lapB * clampedSharpness)).toInt()
                    }

                    val finalR = cr.coerceIn(0, 255)
                    val finalG = cg.coerceIn(0, 255)
                    val finalB = cb.coerceIn(0, 255)

                    outputPixels[idx] = -0x1000000 or (finalR shl 16) or (finalG shl 8) or finalB
                }
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(outputPixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Backward-compatible helper for legacy deblur calls.
     */
    fun applyAdaptiveDeblur(
        bitmap: Bitmap,
        intensity: Float = 0.35f,
        edgeThreshold: Int = 10
    ): Bitmap {
        return applyDetailTuning(
            bitmap = bitmap,
            sharpness = intensity,
            antiAliasing = 0.3f,
            edgeThreshold = edgeThreshold
        )
    }
}
