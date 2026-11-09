package id.xms.ohscaler.egg.core.ui

import android.graphics.Bitmap

/**
 * Utility to prevent Android HWUI "Canvas: trying to draw too large bitmap" crashes.
 * Android's RecordingCanvas.throwIfCannotDraw throws a RuntimeException if any Bitmap
 * drawn on a hardware Canvas exceeds 100 MB (104,857,600 bytes) or max texture size.
 */
object BitmapDisplayUtils {
    private const val MAX_CANVAS_BYTES = 50 * 1024 * 1024
    private const val MAX_DISPLAY_DIMENSION = 2560

    fun toDisplaySafeBitmap(bitmap: Bitmap, maxDimension: Int = MAX_DISPLAY_DIMENSION): Bitmap {
        val maxDim = maxOf(bitmap.width, bitmap.height)
        if (bitmap.allocationByteCount <= MAX_CANVAS_BYTES && maxDim <= maxDimension) {
            return bitmap
        }
        val scale = maxDimension.toFloat() / maxDim.toFloat()
        val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }
}
