package ir.taskcmp.image

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ImageProcessingTest {
    @Test
    fun grayscaleAveragesRgbAndPreservesAlpha() {
        val input = PixelImage(1, 1, intArrayOf(0x80204060.toInt()))
        assertEquals(0x80404040.toInt(), ImageProcessing.grayscale(input).pixels.single())
    }

    @Test
    fun meanUsesOnlyTheEightNeighbours() {
        val pixels = IntArray(9) { 0xff000000.toInt() }
        pixels[4] = 0xff5a5a5a.toInt()
        val output = ManualMean3x3Smoother().smooth(PixelImage(3, 3, pixels))
        assertEquals(0, output.pixels[4] and 0xff)
    }

    @Test
    fun borderReplicatesNearestPixel() {
        val pixels = intArrayOf(0xff000000.toInt(), 0xff5a5a5a.toInt())
        val output = ImageProcessing.smooth(PixelImage(2, 1, pixels), SmoothMethod.MEAN_3X3)
        assertContentEquals(intArrayOf(33, 56), output.pixels.map { it and 0xff }.toIntArray())
    }

    @Test
    fun histogramCountsEachGrayscaleIntensity() {
        val image = PixelImage(
            3,
            1,
            intArrayOf(0xff000000.toInt(), 0xff7f7f7f.toInt(), 0xffffffff.toInt()),
        )

        val histogram = ImageProcessing.histogram(image)

        assertEquals(1, histogram[0])
        assertEquals(1, histogram[127])
        assertEquals(1, histogram[255])
        assertEquals(3, histogram.sum())
    }
}
