package id.xms.ohscaler.egg.core.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class EnhancementTest {

    @Test
    fun testEnhancementModesEnum() {
        assertEquals("Natural", EnhancementMode.Natural.name)
        assertEquals("UltraDeblur", EnhancementMode.UltraDeblur.name)
        assertEquals(2, EnhancementMode.values().size)
    }

    @Test
    fun testAdaptiveTileTuning() {
        val processor = TileProcessor(tileSize = 256, overlapPadding = 16)
        val tiles = processor.calculateTiles(1920, 1080)
        // 1920 / 256 = 8 cols, 1080 / 256 = 5 rows => 40 tiles
        assertEquals(40, tiles.size)

        for (t in tiles) {
            assertNotNull(t)
            org.junit.Assert.assertTrue(t.sourceWidth in 1..256)
            org.junit.Assert.assertTrue(t.sourceHeight in 1..256)
            org.junit.Assert.assertTrue(t.padLeft in 0..16)
            org.junit.Assert.assertTrue(t.padRight in 0..16)
            org.junit.Assert.assertTrue(t.padTop in 0..16)
            org.junit.Assert.assertTrue(t.padBottom in 0..16)
        }
    }
}
