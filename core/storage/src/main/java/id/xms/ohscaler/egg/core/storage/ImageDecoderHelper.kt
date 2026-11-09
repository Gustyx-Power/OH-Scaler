package id.xms.ohscaler.egg.core.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageDecoderHelper {

    suspend fun decodeBitmapFromUri(
        context: Context,
        uri: Uri,
        maxDimension: Int = 4096
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val source = if (uri.scheme == "file") {
                val path = uri.path ?: return@withContext null
                ImageDecoder.createSource(File(path))
            } else {
                ImageDecoder.createSource(context.contentResolver, uri)
            }

            val decoded = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true

                val origW = info.size.width
                val origH = info.size.height
                if (origW > maxDimension || origH > maxDimension) {
                    val scale = maxDimension.toFloat() / maxOf(origW, origH)
                    val targetW = (origW * scale).toInt().coerceAtLeast(1)
                    val targetH = (origH * scale).toInt().coerceAtLeast(1)
                    decoder.setTargetSize(targetW, targetH)
                }
            }
            // Ensure ARGB_8888 config for ONNX and pixel manipulation
            if (decoded.config != Bitmap.Config.ARGB_8888) {
                decoded.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                decoded
            }
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "ImageDecoder failed for uri: $uri", e)
            null
        }
    }
}
