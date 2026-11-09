package id.xms.ohscaler.egg.core.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

/**
 * TileProcessor handles splitting high-resolution bitmaps into overlapping tiles,
 * processing each tile independently (to avoid GPU/NPU OOM),
 * and seamlessly re-stitching them back into the final upscaled bitmap.
 */
class TileProcessor(
    val tileSize: Int = 256,
    val overlapPadding: Int = 16
) {
    data class TileInfo(
        val tileIndex: Int,
        val totalTiles: Int,
        val sourceX: Int,
        val sourceY: Int,
        val sourceWidth: Int,
        val sourceHeight: Int,
        val padLeft: Int,
        val padTop: Int,
        val padRight: Int,
        val padBottom: Int
    )

    fun calculateTiles(width: Int, height: Int): List<TileInfo> {
        val tiles = mutableListOf<TileInfo>()
        val numCols = (width + tileSize - 1) / tileSize
        val numRows = (height + tileSize - 1) / tileSize
        val totalTiles = numCols * numRows
        var index = 0

        for (row in 0 until numRows) {
            val y0 = row * tileSize
            val y1 = minOf(height, y0 + tileSize)
            val h = y1 - y0

            for (col in 0 until numCols) {
                val x0 = col * tileSize
                val x1 = minOf(width, x0 + tileSize)
                val w = x1 - x0

                val padLeft = minOf(overlapPadding, x0)
                val padTop = minOf(overlapPadding, y0)
                val padRight = minOf(overlapPadding, width - x1)
                val padBottom = minOf(overlapPadding, height - y1)

                tiles.add(
                    TileInfo(
                        tileIndex = index++,
                        totalTiles = totalTiles,
                        sourceX = x0,
                        sourceY = y0,
                        sourceWidth = w,
                        sourceHeight = h,
                        padLeft = padLeft,
                        padTop = padTop,
                        padRight = padRight,
                        padBottom = padBottom
                    )
                )
            }
        }
        return tiles
    }

    suspend fun processTiled(
        sourceBitmap: Bitmap,
        scaleFactor: Int,
        onProgress: (current: Int, total: Int, progressFloat: Float) -> Unit,
        transformTile: suspend (paddedTile: Bitmap) -> Bitmap
    ): Bitmap {
        val srcWidth = sourceBitmap.width
        val srcHeight = sourceBitmap.height
        val dstWidth = srcWidth * scaleFactor
        val dstHeight = srcHeight * scaleFactor

        val resultBitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        val tiles = calculateTiles(srcWidth, srcHeight)
        val totalTiles = tiles.size

        for (tile in tiles) {
            coroutineContext.ensureActive()

            // Extract tile with padding
            val extractX = tile.sourceX - tile.padLeft
            val extractY = tile.sourceY - tile.padTop
            val extractW = tile.sourceWidth + tile.padLeft + tile.padRight
            val extractH = tile.sourceHeight + tile.padTop + tile.padBottom

            val paddedTile = Bitmap.createBitmap(
                sourceBitmap,
                extractX,
                extractY,
                extractW,
                extractH
            )

            // Process tile
            val upscaledPaddedTile = transformTile(paddedTile)
            if (paddedTile != upscaledPaddedTile && !paddedTile.isRecycled) {
                paddedTile.recycle()
            }

            // Crop out scaled padding
            val cropLeft = tile.padLeft * scaleFactor
            val cropTop = tile.padTop * scaleFactor
            val coreW = tile.sourceWidth * scaleFactor
            val coreH = tile.sourceHeight * scaleFactor

            val srcRect = Rect(cropLeft, cropTop, cropLeft + coreW, cropTop + coreH)
            val dstX = tile.sourceX * scaleFactor
            val dstY = tile.sourceY * scaleFactor
            val dstRect = Rect(dstX, dstY, dstX + coreW, dstY + coreH)

            canvas.drawBitmap(upscaledPaddedTile, srcRect, dstRect, paint)

            if (!upscaledPaddedTile.isRecycled) {
                upscaledPaddedTile.recycle()
            }

            val progress = (tile.tileIndex + 1).toFloat() / totalTiles.toFloat()
            onProgress(tile.tileIndex + 1, totalTiles, progress)
        }

        return resultBitmap
    }
}
