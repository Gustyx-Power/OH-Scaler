package id.xms.ohscaler.egg.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TileProcessorTest {

    @Test
    fun testTileCalculationCoversEntireImage() {
        val processor = TileProcessor(tileSize = 256, overlapPadding = 16)
        val width = 1000
        val height = 750

        val tiles = processor.calculateTiles(width, height)

        // 1000 / 256 = 4 cols (256, 256, 256, 232)
        // 750 / 256 = 3 rows (256, 256, 238)
        // Total = 4 * 3 = 12 tiles
        assertEquals(12, tiles.size)

        // Verify boundary padding
        val firstTile = tiles.first()
        assertEquals(0, firstTile.padLeft)
        assertEquals(0, firstTile.padTop)
        assertEquals(16, firstTile.padRight)
        assertEquals(16, firstTile.padBottom)

        // Verify interior tile has full padding
        val interiorTile = tiles[5] // row 1, col 1
        assertEquals(16, interiorTile.padLeft)
        assertEquals(16, interiorTile.padTop)
        assertEquals(16, interiorTile.padRight)
        assertEquals(16, interiorTile.padBottom)

        // Verify last tile boundary
        val lastTile = tiles.last()
        assertEquals(16, lastTile.padLeft)
        assertEquals(16, lastTile.padTop)
        assertEquals(0, lastTile.padRight)
        assertEquals(0, lastTile.padBottom)
    }

    @Test
    fun testSmallImageSingleTile() {
        val processor = TileProcessor(tileSize = 512, overlapPadding = 16)
        val tiles = processor.calculateTiles(128, 128)

        assertEquals(1, tiles.size)
        val tile = tiles.first()
        assertEquals(0, tile.padLeft)
        assertEquals(0, tile.padTop)
        assertEquals(0, tile.padRight)
        assertEquals(0, tile.padBottom)
        assertEquals(128, tile.sourceWidth)
        assertEquals(128, tile.sourceHeight)
    }
}
