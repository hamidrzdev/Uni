package ir.taskcmp.image

/** Pixels are stored row by row as ARGB integers, independently of the UI toolkit. */
data class PixelImage(val width: Int, val height: Int, val pixels: IntArray) {
    init {
        require(width > 0 && height > 0)
        require(pixels.size.toLong() == width.toLong() * height)
    }
}

enum class SmoothMethod(val title: String, val description: String) {
    MEAN_3X3("میانگین‌گیر ۳×۳", "میانگین ۸ همسایهٔ اطراف هر پیکسل")
}

object ImageProcessing {
    fun grayscale(input: PixelImage): PixelImage {
        val output = IntArray(input.pixels.size)
        for (index in input.pixels.indices) {
            val color = input.pixels[index]
            val alpha = color ushr 24
            val red = color ushr 16 and 0xff
            val green = color ushr 8 and 0xff
            val blue = color and 0xff
            val gray = (red + green + blue) / 3
            output[index] = alpha shl 24 or (gray shl 16) or (gray shl 8) or gray
        }
        return PixelImage(input.width, input.height, output)
    }

    fun smooth(input: PixelImage, method: SmoothMethod): PixelImage = when (method) {
        SmoothMethod.MEAN_3X3 -> ManualMean3x3Smoother().smooth(input)
    }

    /** Counts grayscale intensities from 0 through 255. */
    fun histogram(input: PixelImage): IntArray {
        val bins = IntArray(256)
        for (color in input.pixels) {
            val red = color ushr 16 and 0xff
            val green = color ushr 8 and 0xff
            val blue = color and 0xff
            bins[(red + green + blue) / 3]++
        }
        return bins
    }
}
