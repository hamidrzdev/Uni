package ir.taskcmp

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ir.taskcmp.generated.resources.Res
import ir.taskcmp.generated.resources.vazirmatn_bold
import ir.taskcmp.generated.resources.vazirmatn_medium
import ir.taskcmp.generated.resources.vazirmatn_regular
import ir.taskcmp.image.ImageProcessing
import ir.taskcmp.image.SmoothMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.Font
import kotlin.math.ln

private val background = Color(0xFFF6F8FC)
private val ink = Color(0xFF1B2535)
private val muted = Color(0xFF667287)
private val accent = Color(0xFF3658C6)
private val paleBlue = Color(0xFFEAF0FF)

private data class Exercise(val id: String, val title: String, val subtitle: String, val number: String)

private val exercises = listOf(
    Exercise("image-processing", "پردازش تصویر", "تمرین", "۰۱"),
    Exercise("histogram", "عکس و هیستوگرام", "تمرین", "۰۲"),
)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "تمرین‌های من | پردازش تصویر",
        state = rememberWindowState(width = 1280.dp, height = 820.dp),
    ) {
        TaskApp()
    }
}

@Composable
private fun TaskApp() {
    val vazirmatn = FontFamily(
        Font(Res.font.vazirmatn_regular, FontWeight.Normal),
        Font(Res.font.vazirmatn_medium, FontWeight.Medium),
        Font(Res.font.vazirmatn_bold, FontWeight.Bold),
    )
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = accent,
            onPrimary = Color.White,
            primaryContainer = paleBlue,
            onPrimaryContainer = Color(0xFF173984),
            background = background,
            onBackground = ink,
            surface = Color.White,
            onSurface = ink,
            surfaceVariant = Color(0xFFF0F3F8),
            onSurfaceVariant = muted,
            outline = Color(0xFFD4DBE8),
        ),
        typography = remember(vazirmatn) { appTypography(vazirmatn) },
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            var selectedId by remember { mutableStateOf(exercises.first().id) }
            Row(Modifier.fillMaxSize().background(background)) {
                Sidebar(selectedId, onSelect = { selectedId = it }, Modifier.width(270.dp).fillMaxHeight())
                when (selectedId) {
                    "image-processing" -> ImageExercise(Modifier.weight(1f).fillMaxHeight())
                    "histogram" -> HistogramExercise(Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
private fun HistogramExercise(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var image by remember { mutableStateOf<LoadedImage?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load(action: () -> LoadedImage) {
        scope.launch {
            busy = true
            error = null
            try {
                image = withContext(Dispatchers.IO) { action() }
            } catch (exception: Exception) {
                error = exception.message ?: "دریافت تصویر انجام نشد."
            } finally {
                busy = false
            }
        }
    }

    Column(
        modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Text("تمرین ۰۲ / عکس و هیستوگرام", style = MaterialTheme.typography.labelLarge, color = accent)
            Spacer(Modifier.height(2.dp))
            Text("ثبت عکس و هیستوگرام", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("عکس را با دوربین بگیرید یا انتخاب کنید؛ فراوانی ۲۵۶ سطح روشنایی نمایش داده می‌شود.", style = MaterialTheme.typography.bodyMedium, color = muted)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("دریافت تصویر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("برای اولین استفاده، سیستم‌عامل ممکن است اجازهٔ دسترسی به دوربین را درخواست کند.", style = MaterialTheme.typography.bodySmall, color = muted)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { load(::captureCameraImage) }, enabled = !busy, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)) {
                        Text("گرفتن عکس")
                    }
                    OutlinedButton(onClick = {
                        val file = chooseImageFile() ?: return@OutlinedButton
                        load { loadImage(file) }
                    }, enabled = !busy) { Text("انتخاب فایل") }
                    Text(image?.fileName ?: "هنوز تصویری دریافت نشده است", style = MaterialTheme.typography.bodyMedium, color = muted)
                }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            }
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val loaded = image
            if (maxWidth < 760.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ImagePanel("تصویر ثبت‌شده", "Camera / file", loaded?.preview, Modifier.fillMaxWidth())
                    HistogramPanel(loaded?.grayscale?.let(ImageProcessing::histogram), Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ImagePanel("تصویر ثبت‌شده", "Camera / file", loaded?.preview, Modifier.weight(1f))
                    HistogramPanel(loaded?.grayscale?.let(ImageProcessing::histogram), Modifier.weight(1f))
                }
            }
        }
        image?.let { PixelInfo(it) }
    }
}

@Composable
private fun HistogramPanel(histogram: IntArray?, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text("هیستوگرام روشنایی", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("محور افقی: روشنایی پیکسل (۰ = سیاه، ۲۵۵ = سفید)", style = MaterialTheme.typography.labelMedium, color = accent)
            Text("محور عمودی: تعداد پیکسل‌ها", style = MaterialTheme.typography.labelMedium, color = accent)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F6FA)).border(1.dp, Color(0xFFE4E9F2), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                if (histogram == null) {
                    Text("پس از ثبت تصویر، هیستوگرام اینجا نمایش داده می‌شود", color = muted, textAlign = TextAlign.Center)
                } else {
                    val peak = histogram.maxOrNull()?.coerceAtLeast(1) ?: 1
                    val logPeak = ln(peak + 1.0)
                    Canvas(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 12.dp)) {
                        val step = size.width / 256f
                        histogram.forEachIndexed { intensity, count ->
                            val x = intensity * step + step / 2f
                            // Log scale keeps small, meaningful bins visible when one bin dominates.
                            val height = (size.height * (ln(count + 1.0) / logPeak)).toFloat()
                            drawLine(accent, start = androidx.compose.ui.geometry.Offset(x, size.height), end = androidx.compose.ui.geometry.Offset(x, size.height - height), strokeWidth = step.coerceAtLeast(1f), cap = StrokeCap.Butt)
                        }
                    }
                }
            }
            if (histogram != null) {
                Text("مقیاس عمودی لگاریتمی است تا ستون‌های کم‌فراوان هم دیده شوند.", style = MaterialTheme.typography.bodySmall, color = muted, modifier = Modifier.padding(top = 8.dp))
                Text("بیشترین فراوانی: ${histogram.maxOrNull()} پیکسل", style = MaterialTheme.typography.bodySmall, color = muted)
            }
        }
    }
}

@Composable
private fun Sidebar(selectedId: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, color = Color.White, shadowElevation = 3.dp) {
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 28.dp)) {
            Text("کارگاه تمرین", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Compose Multiplatform", style = MaterialTheme.typography.bodySmall, color = muted)
            Spacer(Modifier.height(38.dp))
            Text("فهرست تمرین‌ها", style = MaterialTheme.typography.labelLarge, color = muted)
            Spacer(Modifier.height(12.dp))
            exercises.forEach { exercise ->
                val selected = exercise.id == selectedId
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) paleBlue else Color.Transparent)
                        .clickable { onSelect(exercise.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                            .background(if (selected) accent else Color(0xFFF0F3F8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(exercise.number, color = if (selected) Color.White else muted, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(exercise.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(exercise.subtitle, style = MaterialTheme.typography.bodySmall, color = muted)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            HorizontalDivider(color = Color(0xFFE7EBF2))
            Spacer(Modifier.height(16.dp))
            Text("فضایی برای یادگیری و آزمایش", style = MaterialTheme.typography.bodySmall, color = muted)
        }
    }
}

@Composable
private fun ImageExercise(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var image by remember { mutableStateOf<LoadedImage?>(null) }
    var output by remember { mutableStateOf<ImageBitmap?>(null) }
    var selectedMethod by remember { mutableStateOf(SmoothMethod.MEAN_3X3) }
    var appliedMethod by remember { mutableStateOf<SmoothMethod?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Text("تمرین ۰۱ / پردازش تصویر", style = MaterialTheme.typography.labelLarge, color = accent)
            Spacer(Modifier.height(2.dp))
            Text("پردازش تصویر", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "استخراج پیکسل‌ها، تبدیل به خاکستری و هموارسازی تصویر با فیلتر میانگین‌گیر",
                style = MaterialTheme.typography.bodyMedium,
                color = muted,
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("انتخاب و پردازش تصویر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("دادهٔ خام در آرایه ذخیره و نسخهٔ خاکستری ساخته می‌شود.", style = MaterialTheme.typography.bodySmall, color = muted)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            val file = chooseImageFile() ?: return@Button
                            scope.launch {
                                busy = true
                                error = null
                                try {
                                    val loaded = withContext(Dispatchers.IO) { loadImage(file) }
                                    val grayscalePreview = withContext(Dispatchers.Default) { loaded.grayscale.toImageBitmap() }
                                    image = loaded
                                    output = grayscalePreview
                                    appliedMethod = null
                                } catch (exception: Exception) {
                                    error = exception.message ?: "خواندن تصویر انجام نشد."
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        enabled = !busy,
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    ) { Text("انتخاب فایل") }
                    Text(image?.fileName ?: "هنوز فایلی انتخاب نشده است", style = MaterialTheme.typography.bodyMedium, color = muted)
                }
                HorizontalDivider(color = Color(0xFFE8ECF3))
                BoxWithConstraints {
                    val compact = maxWidth < 620.dp
                    @Composable fun MethodControl() {
                        Column {
                            Text("روش هموارسازی", style = MaterialTheme.typography.labelLarge, color = muted)
                            Spacer(Modifier.height(6.dp))
                            MethodSelector(selectedMethod, onSelect = { selectedMethod = it })
                        }
                    }
                    @Composable fun SmoothControl() {
                    Button(
                        onClick = {
                            val loaded = image ?: return@Button
                            scope.launch {
                                busy = true
                                error = null
                                try {
                                    val result = withContext(Dispatchers.Default) {
                                        ImageProcessing.smooth(loaded.grayscale, selectedMethod).toImageBitmap()
                                    }
                                    output = result
                                    appliedMethod = selectedMethod
                                } catch (exception: Exception) {
                                    error = exception.message ?: "هموارسازی تصویر انجام نشد."
                                } finally {
                                    busy = false
                                }
                            }
                        },
                        enabled = image != null && !busy,
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    ) { Text("Smooth / هموارسازی") }
                    }
                    if (compact) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            MethodControl()
                            SmoothControl()
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                            MethodControl()
                            SmoothControl()
                        }
                    }
                }
                if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val outputSubtitle = if (appliedMethod == null) "Grayscale" else "Smooth · ${appliedMethod?.title}"
            if (maxWidth < 760.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ImagePanel("تصویر ورودی", "Input image", image?.preview, Modifier.fillMaxWidth())
                    ImagePanel("تصویر خروجی", outputSubtitle, output, Modifier.fillMaxWidth())
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ImagePanel("تصویر ورودی", "Input image", image?.preview, Modifier.weight(1f))
                    ImagePanel("تصویر خروجی", outputSubtitle, output, Modifier.weight(1f))
                }
            }
        }

        image?.let { loaded -> PixelInfo(loaded) }
    }
}

@Composable
private fun MethodSelector(selected: SmoothMethod, onSelect: (SmoothMethod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)) {
            Text("${selected.title}  ▾")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SmoothMethod.entries.forEach { method ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(method.title)
                            Text(method.description, style = MaterialTheme.typography.bodySmall, color = muted)
                        }
                    },
                    onClick = {
                        onSelect(method)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ImagePanel(title: String, subtitle: String, image: ImageBitmap?, modifier: Modifier = Modifier) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = accent)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF4F6FA))
                    .border(1.dp, Color(0xFFE4E9F2), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (image == null) {
                    Text("تصویر اینجا نمایش داده می‌شود", color = muted, textAlign = TextAlign.Center)
                } else {
                    Image(
                        bitmap = image,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize().padding(8.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

@Composable
private fun PixelInfo(image: LoadedImage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("دادهٔ خام تصویر", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "${image.raw.width} × ${image.raw.height} پیکسل  ·  ${image.raw.pixels.size} مقدار ARGB در آرایه",
                color = muted,
            )
            Text("مرزهای فیلتر ۳×۳ با تکرار نزدیک‌ترین پیکسل محاسبه می‌شوند.", style = MaterialTheme.typography.bodySmall, color = muted)
        }
    }
}

private fun appTypography(font: FontFamily): Typography = Typography().let { default ->
    Typography(
        displayLarge = default.displayLarge.copy(fontFamily = font),
        displayMedium = default.displayMedium.copy(fontFamily = font),
        displaySmall = default.displaySmall.copy(fontFamily = font),
        headlineLarge = default.headlineLarge.copy(fontFamily = font),
        headlineMedium = default.headlineMedium.copy(fontFamily = font, fontSize = 32.sp),
        headlineSmall = default.headlineSmall.copy(fontFamily = font),
        titleLarge = default.titleLarge.copy(fontFamily = font),
        titleMedium = default.titleMedium.copy(fontFamily = font),
        titleSmall = default.titleSmall.copy(fontFamily = font),
        bodyLarge = default.bodyLarge.copy(fontFamily = font),
        bodyMedium = default.bodyMedium.copy(fontFamily = font),
        bodySmall = default.bodySmall.copy(fontFamily = font),
        labelLarge = default.labelLarge.copy(fontFamily = font),
        labelMedium = default.labelMedium.copy(fontFamily = font),
        labelSmall = default.labelSmall.copy(fontFamily = font),
    )
}
