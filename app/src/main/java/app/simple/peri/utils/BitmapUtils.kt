package app.simple.peri.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup

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

    fun ViewGroup.createLayoutBitmap(): Bitmap {
        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(spec, spec)
        // layout(0, 0, measuredWidth, measuredHeight)
        /**
         * Retain current matrix and scale factor, but replace translation vector with specified values.
         */
        layout(scrollX, scrollY, scrollX + measuredWidth, scrollY + measuredHeight)

        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.translate((-scrollX).toFloat(), (-scrollY).toFloat())
        draw(canvas)
        return bitmap
    }
}