package id.xms.ohscaler.egg.core.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScopedStorageManager(private val context: Context) {

    suspend fun saveBitmapToGallery(
        bitmap: Bitmap,
        filenamePrefix: String = "OH_SCALER"
    ): Uri? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val timestamp = System.currentTimeMillis()
        val displayName = "${filenamePrefix}_$timestamp.png"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/OH-Scaler"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageUri = resolver.insert(collectionUri, contentValues) ?: return@withContext null

        try {
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    resolver.delete(imageUri, null, null)
                    return@withContext null
                }
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
            imageUri
        } catch (e: Exception) {
            resolver.delete(imageUri, null, null)
            null
        }
    }
}
