package id.xms.ohscaler.egg.core.storage

import android.content.ContentUris
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
        filenamePrefix: String = "OH_SCALER",
        mimeType: String = "image/png",
        quality: Int = 100
    ): Uri? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val timestamp = System.currentTimeMillis()

        val (ext, compressFormat) = when (mimeType) {
            "image/jpeg" -> Pair("jpg", Bitmap.CompressFormat.JPEG)
            "image/webp" -> {
                val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (quality >= 100) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
                Pair("webp", format)
            }
            else -> Pair("png", Bitmap.CompressFormat.PNG)
        }

        val displayName = "${filenamePrefix}_$timestamp.$ext"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
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
                if (!bitmap.compress(compressFormat, quality.coerceIn(10, 100), outputStream)) {
                    resolver.delete(imageUri, null, null)
                    return@withContext null
                }
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)
            imageUri
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Failed saving bitmap to gallery", e)
            resolver.delete(imageUri, null, null)
            null
        }
    }

    suspend fun saveBitmapToGallery(
        bitmap: Bitmap,
        filenamePrefix: String = "OH_SCALER"
    ): Uri? {
        return saveBitmapToGallery(bitmap, filenamePrefix, "image/png", 100)
    }

    suspend fun loadRecentCreations(limit: Int = 10): List<Uri> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%${Environment.DIRECTORY_PICTURES}/OH-Scaler%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                var count = 0
                while (it.moveToNext() && count < limit) {
                    val id = it.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    list.add(contentUri)
                    count++
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("OHScaler", "Error querying recent creations", e)
        }
        list
    }
}
