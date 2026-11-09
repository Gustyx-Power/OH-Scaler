package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.media.FaceDetector

object FaceDetectorHelper {

    /**
     * Detects human faces using native Android framework FaceDetector (100% offline, zero library).
     * @return List of bounding boxes for detected faces.
     */
    fun detectFaces(bitmap: Bitmap, maxFaces: Int = 5): List<RectF> {
        val width = bitmap.width
        val height = bitmap.height
        if (width < 32 || height < 32) return emptyList()

        // FaceDetector requires even width
        val adjustedWidth = if (width % 2 != 0) width - 1 else width
        val adjustedHeight = height

        val rgb565Bitmap = try {
            val bmp = Bitmap.createScaledBitmap(bitmap, adjustedWidth, adjustedHeight, false)
            if (bmp.config != Bitmap.Config.RGB_565) {
                val converted = bmp.copy(Bitmap.Config.RGB_565, false)
                if (bmp != bitmap && !bmp.isRecycled) bmp.recycle()
                converted
            } else {
                bmp
            }
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Error preparing RGB_565 bitmap for FaceDetector", e)
            return emptyList()
        } ?: return emptyList()

        return try {
            val detector = FaceDetector(adjustedWidth, adjustedHeight, maxFaces)
            val faces = Array<FaceDetector.Face?>(maxFaces) { null }
            val count = detector.findFaces(rgb565Bitmap, faces)

            val result = mutableListOf<RectF>()
            val scaleX = width.toFloat() / adjustedWidth.toFloat()

            for (i in 0 until count) {
                val face = faces[i] ?: continue
                if (face.confidence() < 0.3f) continue

                val midPoint = PointF()
                face.getMidPoint(midPoint)
                val eyeDist = face.eyesDistance()

                // Calculate bounding box around the face
                val left = ((midPoint.x - eyeDist * 1.6f) * scaleX).coerceAtLeast(0f)
                val top = (midPoint.y - eyeDist * 1.8f).coerceAtLeast(0f)
                val right = ((midPoint.x + eyeDist * 1.6f) * scaleX).coerceAtMost(width.toFloat())
                val bottom = (midPoint.y + eyeDist * 2.2f).coerceAtMost(height.toFloat())

                result.add(RectF(left, top, right, bottom))
            }
            result
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "FaceDetector failed", e)
            emptyList()
        } finally {
            if (rgb565Bitmap != bitmap && !rgb565Bitmap.isRecycled) {
                rgb565Bitmap.recycle()
            }
        }
    }
}
