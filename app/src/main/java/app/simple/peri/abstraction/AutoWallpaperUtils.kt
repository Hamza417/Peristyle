package app.simple.peri.abstraction

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.scale
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.BitmapUtils
import app.simple.peri.utils.FileUtils.toFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

object AutoWallpaperUtils {

    private const val TAG = "AutoWallpaperUtils"

    fun getBitmapFromFile(path: String, expectedWidth: Int, expectedHeight: Int, crop: Boolean = true, recycle: Boolean = true, onBitmap: (Bitmap) -> Unit) {
        path.toFile().inputStream().use { stream ->
            val byteArray = stream.readBytes()
            var bitmap = decodeBitmap(byteArray, expectedWidth, expectedHeight)

            // Correct orientation of the bitmap if faulty due to EXIF data
            bitmap = BitmapUtils.correctOrientation(bitmap, ByteArrayInputStream(byteArray))

            if (crop) {
                bitmap = cropAndScaleToFit(bitmap, expectedWidth, expectedHeight)
            }

            onBitmap(bitmap)

            if (recycle) {
                bitmap.recycle()
            }
        }
    }

    fun calculateVisibleCropHint(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Rect {
        val targetAspect = targetWidth.toFloat() / targetHeight
        val bitmapAspect = bitmap.width.toFloat() / bitmap.height

        val (cropWidth, cropHeight) = if (bitmapAspect > targetAspect) {
            val height = bitmap.height
            val width = (height * targetAspect).toInt().coerceAtMost(bitmap.width)
            width to height
        } else {
            val width = bitmap.width
            val height = (width / targetAspect).toInt().coerceAtMost(bitmap.height)
            width to height
        }

        val left = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
        val top = ((bitmap.height - cropHeight) / 2).coerceAtLeast(0)
        return Rect(left, top, left + cropWidth, top + cropHeight)
    }

    /**
     * Crop the bitmap to match the target aspect ratio while ensuring (when possible) the
     * wallpaper-style behavior of using the full height (top -> bottom) and cropping only from the sides.
     * The crop is always centered on the X (or Y when unavoidable) axis, then the result is scaled
     * to the exact [targetWidth] x [targetHeight].
     *
     * Behaviour:
     * - If the source is wider than the target aspect ratio, keep full height and crop equally from left/right.
     * - If the source is narrower (cannot satisfy aspect with full height), fall back to keeping full width
     *   and cropping vertically (center) as a graceful degradation.
     */
    fun cropAndScaleToFit(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val targetAspect = targetWidth.toFloat() / targetHeight
        val bitmapAspect = bitmap.width.toFloat() / bitmap.height

        // Determine crop rectangle enforcing full-height preference when possible
        val cropRect: Rect = if (bitmapAspect >= targetAspect) {
            // Use full height, crop sides
            val height = bitmap.height
            val width = (height * targetAspect).toInt().coerceAtMost(bitmap.width)
            val left = (bitmap.width - width) / 2
            Rect(left, 0, left + width, height)
        } else {
            // Source too narrow for desired aspect keeping full height: crop vertically instead
            val width = bitmap.width
            val height = (width / targetAspect).toInt().coerceAtMost(bitmap.height)
            val top = (bitmap.height - height) / 2
            Rect(0, top, width, top + height)
        }

        val needsCrop = cropRect.left != 0 || cropRect.top != 0 ||
                cropRect.width() != bitmap.width || cropRect.height() != bitmap.height

        val cropped = if (needsCrop) {
            Bitmap.createBitmap(bitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height())
        } else bitmap

        // Scale to target size if dimensions differ (maintains aspect because crop already matched)
        val finalBitmap = if (cropped.width != targetWidth || cropped.height != targetHeight) {
            val scaled = cropped.scale(targetWidth, targetHeight)
            if (cropped !== bitmap && cropped !== scaled) {
                cropped.recycle()
            }
            scaled
        } else {
            cropped
        }

        return finalBitmap
    }

    fun decodeBitmap(byteArray: ByteArray, targetWidth: Int, targetHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, options)

        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = MainComposePreferences.getWallpaperColorSpace()
            inMutable = true
            inSampleSize = BitmapUtils.calculateInSampleSize(options, targetWidth, targetHeight)
            inJustDecodeBounds = false
        }

        Log.d(TAG, "Expected bitmap size: $targetWidth x $targetHeight")
        Log.d(TAG, "Bitmap decoded with sample size: ${decodeOptions.inSampleSize}")

        return BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, decodeOptions)!!
    }

    fun bitmapToInputStream(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 50): InputStream {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        return ByteArrayInputStream(outputStream.toByteArray())
    }
}