package app.simple.peri.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.palette.graphics.Palette
import app.simple.peri.tools.StackBlur
import java.io.InputStream
import androidx.compose.ui.graphics.ColorMatrix as ComposeUiGraphicsColorMatrix

object BitmapUtils {

    /**
     * @param contrast 0..10 1 is default
     * @param brightness -255..255 0 is default
     * @return new bitmap
     */
    fun Bitmap.changeBitmapContrastBrightness(contrast: Float, brightness: Float, saturation: Float, hue: Float): Bitmap {
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
            ))
        }

        colorMatrix.postConcat(ColorMatrix().apply {
            setRotate(0, hue)
            setRotate(1, hue)
            setRotate(2, hue)
        })

        colorMatrix.postConcat(ColorMatrix().apply {
            setSaturation(saturation)
        })

        val ret = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(ret)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(this, 0f, 0f, paint)
        return ret
    }

    fun Bitmap.applyEffects(brightness: Float, contrast: Float) {
        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
            ))
        }

        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        val canvas = Canvas(this)
        canvas.drawBitmap(this, 0f, 0f, paint)
    }

    /**
     * For compose interface only
     */
    fun Bitmap.applyEffects(blur: Float, brightness: Float, contrast: Float, saturation: Float, hue: Float): Bitmap {
        // Initialize the main color matrix
        val colorMatrix = ComposeUiGraphicsColorMatrix()

        // Create color matrices for each transformation
        val rotateRedMatrix = ComposeUiGraphicsColorMatrix().apply { setToRotateRed(hue) }
        val rotateGreenMatrix = ComposeUiGraphicsColorMatrix().apply { setToRotateGreen(hue) }
        val rotateBlueMatrix = ComposeUiGraphicsColorMatrix().apply { setToRotateBlue(hue) }
        val saturationMatrix = ComposeUiGraphicsColorMatrix().apply { setToSaturation(saturation) }
        val contrastMatrix = ComposeUiGraphicsColorMatrix(
                floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                )
        )

        // Manually combine the matrices
        val combinedMatrix = FloatArray(20) // Array to hold the combined matrix
        val tempMatrix = FloatArray(20) // Temporary array for intermediate results

        // Multiply the red and green rotation matrices and store the result in tempMatrix
        multiplyMatrices(rotateRedMatrix.values, rotateGreenMatrix.values, tempMatrix)

        // Multiply the result with the blue rotation matrix and store in combinedMatrix
        multiplyMatrices(tempMatrix, rotateBlueMatrix.values, combinedMatrix)

        // Multiply the result with the saturation matrix and store in tempMatrix
        multiplyMatrices(combinedMatrix, saturationMatrix.values, tempMatrix)

        // Multiply the result with the contrast matrix and store in combinedMatrix
        multiplyMatrices(tempMatrix, contrastMatrix.values, combinedMatrix)

        // Set the combined matrix to the main color matrix
        colorMatrix.set(ComposeUiGraphicsColorMatrix(combinedMatrix))

        // Apply the combined color matrix to the bitmap
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix.toColorMatrix())
        val bitmap = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(this, 0f, 0f, paint)

        try {
            StackBlur().blurRgb(bitmap, blur.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    /**
     * For compose interface only
     */
    fun Bitmap.applyEffects(blur: Float, colorMatrix: ComposeUiGraphicsColorMatrix): Bitmap {
        // Apply the combined color matrix to the bitmap
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix.toColorMatrix())
        val bitmap = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(this, 0f, 0f, paint)

        try {
            StackBlur().blurRgb(bitmap, blur.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

    fun ComposeUiGraphicsColorMatrix.toColorMatrix(): ColorMatrix {
        val values = FloatArray(20)
        this.values.copyInto(values)
        return ColorMatrix(values)
    }

    fun ViewGroup.createLayoutBitmap(): Bitmap {
        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        measure(spec, spec)
        // layout(0, 0, measuredWidth, measuredHeight)
        /**
         * Retain current matrix and scale factor, but replace translation vector with specified values.
         */
        layout(scrollX, scrollY, measuredWidth, measuredHeight)

        val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.translate((-scrollX).toFloat(), (-scrollY).toFloat())
        draw(canvas)
        return bitmap
    }

    fun Bitmap.applySaturation(saturation: Float): Bitmap {
        val cm = ColorMatrix()
        cm.setSaturation(saturation)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        val ret = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(ret)
        canvas.drawBitmap(this, 0f, 0f, paint)
        return ret
    }

    fun Bitmap.applyBrightness(brightness: Float): Bitmap {
        val cm = ColorMatrix()
        cm.set(floatArrayOf(
                1f, 0f, 0f, 0f, brightness,
                0f, 1f, 0f, 0f, brightness,
                0f, 0f, 1f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
        ))
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        val ret = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(ret)
        canvas.drawBitmap(this, 0f, 0f, paint)
        return ret
    }

    fun Bitmap.applyContrast(contrast: Float): Bitmap {
        val cm = ColorMatrix()
        cm.set(floatArrayOf(
                contrast, 0f, 0f, 0f, 0f,
                0f, contrast, 0f, 0f, 0f,
                0f, 0f, contrast, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
        ))
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        val ret = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(ret)
        canvas.drawBitmap(this, 0f, 0f, paint)
        return ret
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        Log.d("BitmapUtils", "calculateInSampleSize: reqWidth: $reqWidth, reqHeight: $reqHeight")

        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        Log.d("BitmapUtils", "calculateInSampleSize: outWidth: ${options.outWidth}, outHeight: ${options.outHeight}")

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun Bitmap.cropBitmap(rect: Rect): Bitmap {
        val ret = Bitmap.createBitmap(rect.width(), rect.height(), config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(ret)
        canvas.drawBitmap(this, rect, Rect(0, 0, rect.width(), rect.height()), null)
        return ret
    }

    /**
     * Corrects the orientation of the bitmap based on the Exif information.
     * @param bitmap The bitmap to correct the orientation.
     * @param inputStream The input stream of the image.
     * @return The corrected bitmap.
     * @see ExifInterface
     */
    fun correctOrientation(bitmap: Bitmap, inputStream: InputStream): Bitmap {
        val ei = ExifInterface(inputStream)
        val orientation: Int = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    /**
     * Rotates the image by the specified angle.
     * @param source The source bitmap to rotate.
     * @param angle The angle to rotate the image.
     * @return The rotated bitmap.
     * @see Matrix
     */
    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun Bitmap.generatePalette(): Palette {
        val palette = Palette.from(this).generate()
        return palette
    }

    /**
     * Multiplies two color matrices and stores the result in the provided result array.
     *
     * @param m1 The first color matrix as a float array.
     * @param m2 The second color matrix as a float array.
     * @param result The array to store the result of the multiplication.
     */
    fun multiplyMatrices(m1: FloatArray, m2: FloatArray, result: FloatArray) {
        for (i in 0..19) {
            val row = i / 5
            val col = i % 5
            // Perform the matrix multiplication and store the result
            result[i] = m1[row * 5] * m2[col] +
                    m1[row * 5 + 1] * m2[col + 5] +
                    m1[row * 5 + 2] * m2[col + 10] +
                    m1[row * 5 + 3] * m2[col + 15] +
                    m1[row * 5 + 4] * m2[col + 4]
        }
    }
}
