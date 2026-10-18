package app.oubliettes

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File

/** The longest side the photo is kept at: sharp on any phone, light on memory. */
private const val LONGEST = 1_600

/**
 * The player's own photo behind every screen, picked in Options. It is copied into the app (shrunk,
 * and turned the right way up), so it stays even if the original is deleted from the gallery.
 * [dimming] (0 to 1, kept under "bgDim") darkens it so that the texts stay easy to read.
 */
internal object Photo {
    var image by mutableStateOf<ImageBitmap?>(null)
        private set
    var dimming by mutableFloatStateOf(0.5f)
        private set

    /** How opaque the veil over the photo is: never clear, never black. */
    val veil get() = 0.15f + 0.7f * dimming

    private fun file(context: Context) = File(context.filesDir, "background.jpg")

    fun load(context: Context, prefs: SharedPreferences) {
        dimming = prefs.getFloat("bgDim", 0.5f).coerceIn(0f, 1f)
        image = runCatching { file(context).takeIf { it.exists() }?.let { BitmapFactory.decodeFile(it.path)?.asImageBitmap() } }.getOrNull()
    }

    fun dim(prefs: SharedPreferences, value: Float) {
        dimming = value.coerceIn(0f, 1f)
        prefs.edit().putFloat("bgDim", dimming).apply()
    }

    /** Takes the photo at [uri] as the background. Reads and writes files: not for the main thread. False when it is not a picture. */
    fun choose(context: Context, uri: Uri): Boolean = runCatching {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= LONGEST) sample *= 2
        var bitmap = resolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }!!
        val turn = when (
            resolver.openInputStream(uri)!!.use { ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) }
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val scale = LONGEST.toFloat() / maxOf(bitmap.width, bitmap.height)
        if (turn != 0f || scale < 1f) {
            val matrix = Matrix().apply { postRotate(turn); if (scale < 1f) postScale(scale, scale) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        val target = file(context)
        val temporary = File(target.path + ".tmp")
        temporary.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        check(temporary.renameTo(target) || (target.delete() && temporary.renameTo(target)))
        image = bitmap.asImageBitmap()
    }.isSuccess

    fun clear(context: Context) {
        image = null
        file(context).delete()
    }
}

/** Behind the screens: the photo, under a veil of the colour of the app. Nothing at all without a photo. */
@Composable
internal fun PhotoBackdrop() {
    val photo = Photo.image ?: return
    Box(Modifier.fillMaxSize()) {
        Image(photo, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Bg.copy(alpha = Photo.veil)))
    }
}
