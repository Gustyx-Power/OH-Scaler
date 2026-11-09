package id.xms.ohscaler.egg.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EnhancementTest {

    @Test
    fun testTuningConfigDefaults() {
        val config = TuningConfig()
        assertEquals(2.0f, config.scaleFactor, 0.001f)
        assertEquals(0.5f, config.sharpness, 0.001f)
        assertEquals(0.5f, config.antiAliasing, 0.001f)
    }

    @Test
    fun testAdaptiveTileTuning() {
        val processor = TileProcessor(tileSize = 256, overlapPadding = 16)
        val tiles = processor.calculateTiles(1920, 1080)
        assertEquals(40, tiles.size)

        for (t in tiles) {
            assertNotNull(t)
            assertTrue(t.sourceWidth in 1..256)
            assertTrue(t.sourceHeight in 1..256)
            assertTrue(t.padLeft in 0..16)
            assertTrue(t.padRight in 0..16)
            assertTrue(t.padTop in 0..16)
            assertTrue(t.padBottom in 0..16)
        }
    }

    @Test
    fun testFloatScaleTileCalculation() {
        val processor = TileProcessor(tileSize = 256, overlapPadding = 16)
        val tiles = processor.calculateTiles(320, 240)
        assertEquals(2, tiles.size)
    }
}
