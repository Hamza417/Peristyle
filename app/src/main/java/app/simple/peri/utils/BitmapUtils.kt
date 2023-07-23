package app.simple.peri.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object BitmapUtils {

    /**
     * @param contrast 0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    fun Bitmap.changeBitmapContrastBrightness(contrast: Float, brightness: Float, saturation: Float): Bitmap {
        val cm = ColorMatrix(floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
        ))

        cm.postConcat(ColorMatrix().apply {
            setSaturation(saturation)
        })

        val ret = Bitmap.createBitmap(width, height, config)
        val canvas = Canvas(ret)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(this, 0f, 0f, paint)
        return ret
    }
}