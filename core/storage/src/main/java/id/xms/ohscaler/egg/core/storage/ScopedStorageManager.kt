package id.xms.ohscaler.egg.core.storage

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
