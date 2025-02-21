package app.simple.peri.ui.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

@Composable
fun captureGlideImage(drawable: Drawable?, width: Int, height: Int): Drawable? {
    if (drawable == null) return null

    val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    val imageBitmap = drawable.toBitmap().asImageBitmap()

    canvas.drawBitmap(imageBitmap.asAndroidBitmap(), 0f, 0f, null)

    return BitmapDrawable(null, bitmap)
}
