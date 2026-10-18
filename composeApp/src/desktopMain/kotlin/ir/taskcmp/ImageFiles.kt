package ir.taskcmp

import androidx.compose.ui.graphics.ImageBitmap
import com.github.sarxos.webcam.Webcam
import ir.taskcmp.image.ImageProcessing
import ir.taskcmp.image.PixelImage
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

data class LoadedImage(
    val fileName: String,
    val raw: PixelImage,
    val grayscale: PixelImage,
    val preview: ImageBitmap,
)

fun chooseImageFile(): File? {
    val chooser = JFileChooser().apply {
        dialogTitle = "انتخاب تصویر"
        fileFilter = FileNameExtensionFilter("تصویر (PNG, JPG, BMP, GIF)", "png", "jpg", "jpeg", "bmp", "gif")
        isAcceptAllFileFilterUsed = false
    }
    return if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) chooser.selectedFile else null
}

fun loadImage(file: File): LoadedImage {
    require(file.isFile) { "فایل انتخاب‌شده در دسترس نیست." }
    require(file.length() <= 30L * 1024 * 1024) { "حجم فایل نباید بیشتر از ۳۰ مگابایت باشد." }
    val buffered = ImageIO.read(file) ?: throw IllegalArgumentException("فرمت این تصویر پشتیبانی نمی‌شود.")
    require(buffered.width.toLong() * buffered.height <= 20_000_000L) {
        "تصویر نباید بیشتر از ۲۰ میلیون پیکسل داشته باشد."
    }
    return loadBufferedImage(file.name, buffered)
}

fun captureCameraImage(): LoadedImage {
    val webcam = Webcam.getDefault() ?: throw IllegalStateException("دوربینی در دستگاه پیدا نشد.")
    try {
        webcam.open()
        val image = webcam.image ?: throw IllegalStateException("ثبت تصویر از دوربین انجام نشد.")
        return loadBufferedImage("عکس دوربین", image)
    } finally {
        if (webcam.isOpen) webcam.close()
    }
}

private fun loadBufferedImage(fileName: String, buffered: BufferedImage): LoadedImage {
    require(buffered.width.toLong() * buffered.height <= 20_000_000L) {
        "تصویر نباید بیشتر از ۲۰ میلیون پیکسل داشته باشد."
    }
    val pixels = IntArray(buffered.width * buffered.height)
    buffered.getRGB(0, 0, buffered.width, buffered.height, pixels, 0, buffered.width)
    val raw = PixelImage(buffered.width, buffered.height, pixels)
    return LoadedImage(fileName, raw, ImageProcessing.grayscale(raw), buffered.toImageBitmap())
}

fun PixelImage.toImageBitmap(): ImageBitmap {
    val buffered = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    buffered.setRGB(0, 0, width, height, pixels, 0, width)
    return buffered.toImageBitmap()
}

private fun BufferedImage.toImageBitmap(): ImageBitmap {
    val encoded = ByteArrayOutputStream().use { stream ->
        check(ImageIO.write(this, "png", stream)) { "نمایش تصویر ممکن نشد." }
        stream.toByteArray()
    }
    return encoded.decodeToImageBitmap()
}
