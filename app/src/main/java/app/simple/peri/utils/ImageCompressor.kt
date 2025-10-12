package app.simple.peri.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    /**
     * Compress image while maintaining original dimensions but reducing file size
     * Optimized for speed by using inSampleSize and efficient bitmap operations
     */
    suspend fun compressImage(
            sourceFile: File,
            destinationFile: File,
            quality: Int = 60,
            targetWidth: Int? = null,
            targetHeight: Int? = null
    ): File = withContext(Dispatchers.IO) {
        // Read original image dimensions without loading full bitmap
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(sourceFile.absolutePath, bounds)

        val exifOrientation = getExifOrientation(sourceFile)

        val srcWidth = bounds.outWidth
        val srcHeight = bounds.outHeight

        // Determine desired output dimensions in visual/oriented space
        val needsSwap = isRotation90or270(exifOrientation)
        val desiredOutWidth = (targetWidth ?: if (needsSwap) srcHeight else srcWidth).coerceAtLeast(1)
        val desiredOutHeight = (targetHeight ?: if (needsSwap) srcWidth else srcHeight).coerceAtLeast(1)

        // For decode sampling, compute required dims in source (pre-rotation) space
        val reqDecodeWidth = if (needsSwap) desiredOutHeight else desiredOutWidth
        val reqDecodeHeight = if (needsSwap) desiredOutWidth else desiredOutHeight

        // Determine output format (prefer destination extension if provided)
        val format = getCompressFormat(destinationFile.name, sourceFile.name)

        // Configure decoder for speed
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inPreferredConfig = if (format == Bitmap.CompressFormat.JPEG) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
            inMutable = false
            inTempStorage = ByteArray(64 * 1024)
            inSampleSize = calculateInSampleSize(srcWidth, srcHeight, reqDecodeWidth, reqDecodeHeight)
        }

        var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            ?: throw IllegalStateException("Failed to decode image")

        // Single-pass transform: apply orientation (rotate/flip) + scale to desired output size
        bitmap = transformBitmap(bitmap, exifOrientation, desiredOutWidth, desiredOutHeight)

        // Compress and save (buffered I/O)
        BufferedOutputStream(FileOutputStream(destinationFile), 16 * 1024).use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }

        bitmap.recycle()
        destinationFile
    }

    /**
     * Reduce image resolution (half or custom factor) while maintaining quality
     * Optimized for speed using inSampleSize
     */
    suspend fun reduceResolution(
            sourceFile: File,
            destinationFile: File,
            scaleFactor: Float = 0.5f,
            quality: Int = 100
    ): File = withContext(Dispatchers.IO) {
        val safeScale = if (scaleFactor <= 0f || scaleFactor.isNaN()) 0.5f else scaleFactor

        // Read original dimensions
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(sourceFile.absolutePath, bounds)

        val exifOrientation = getExifOrientation(sourceFile)

        val srcWidth = bounds.outWidth
        val srcHeight = bounds.outHeight

        // Compute target dimensions in pre-rotated space then map to visual/oriented space
        val preTargetWidth = (srcWidth * safeScale).toInt().coerceAtLeast(1)
        val preTargetHeight = (srcHeight * safeScale).toInt().coerceAtLeast(1)

        val needsSwap = isRotation90or270(exifOrientation)
        val desiredOutWidth = if (needsSwap) preTargetHeight else preTargetWidth
        val desiredOutHeight = if (needsSwap) preTargetWidth else preTargetHeight

        // For decode sampling, compute required dims in source (pre-rotation) space
        val reqDecodeWidth = if (needsSwap) desiredOutHeight else desiredOutWidth
        val reqDecodeHeight = if (needsSwap) desiredOutWidth else desiredOutHeight

        // Determine format (prefer destination extension if provided)
        val format = getCompressFormat(destinationFile.name, sourceFile.name)

        // Decode with inSampleSize
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inPreferredConfig = if (format == Bitmap.CompressFormat.JPEG) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
            inMutable = false
            inTempStorage = ByteArray(64 * 1024)
            inSampleSize = calculateInSampleSize(srcWidth, srcHeight, reqDecodeWidth, reqDecodeHeight)
        }

        var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            ?: throw IllegalStateException("Failed to decode image")

        // Single-pass orientation + scale
        bitmap = transformBitmap(bitmap, exifOrientation, desiredOutWidth, desiredOutHeight)

        // Save with high quality
        BufferedOutputStream(FileOutputStream(destinationFile), 16 * 1024).use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }

        bitmap.recycle()
        destinationFile
    }

    /**
     * Calculate optimal inSampleSize for efficient bitmap loading
     * Returns power of 2 for best performance
     */
    private fun calculateInSampleSize(
            srcWidth: Int,
            srcHeight: Int,
            reqWidth: Int,
            reqHeight: Int
    ): Int {
        var inSampleSize = 1

        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2

            // Calculate the largest inSampleSize value that is a power of 2
            // and keeps both height and width larger than requested
            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize.coerceAtLeast(1)
    }

    // --- EXIF helpers and single-pass transform ---

    private fun getExifOrientation(file: File): Int = try {
        ExifInterface(file.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        )
    } catch (_: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }

    private fun isRotation90or270(orientation: Int): Boolean {
        return orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270
    }

    private fun transformBitmap(src: Bitmap, exifOrientation: Int, outWidth: Int, outHeight: Int): Bitmap {
        // Fast path: no transform and size already matches
        if (exifOrientation == ExifInterface.ORIENTATION_NORMAL &&
                src.width == outWidth && src.height == outHeight) {
            return src
        }

        val rotation = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        val flipH = exifOrientation == ExifInterface.ORIENTATION_FLIP_HORIZONTAL
        val flipV = exifOrientation == ExifInterface.ORIENTATION_FLIP_VERTICAL

        val needsSwap = rotation == 90f || rotation == 270f
        val preW = if (needsSwap) src.height else src.width
        val preH = if (needsSwap) src.width else src.height

        val scaleX = (outWidth.toFloat() / preW.toFloat()).takeIf { it.isFinite() && it > 0f } ?: 1f
        val scaleY = (outHeight.toFloat() / preH.toFloat()).takeIf { it.isFinite() && it > 0f } ?: 1f

        val m = Matrix()
        if (flipH) m.postScale(-1f, 1f)
        if (flipV) m.postScale(1f, -1f)
        if (rotation != 0f) m.postRotate(rotation)
        if (scaleX != 1f || scaleY != 1f) m.postScale(scaleX, scaleY)

        val out = Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
        if (out != src) src.recycle()
        return out
    }

    /**
     * Determine compression format based on file extension
     */
    private fun getCompressFormat(fileName: String): Bitmap.CompressFormat {
        return when (fileName.lowercase().substringAfterLast(".")) {
            "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            "webp" -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
            else -> Bitmap.CompressFormat.JPEG
        }
    }

    // Prefer primary name's extension; fallback if unknown
    private fun getCompressFormat(primaryName: String, fallbackName: String): Bitmap.CompressFormat {
        val primary = getCompressFormat(primaryName)
        // If primary resolved to JPEG only because it was unknown (no dot), try fallback.
        // Simple heuristic: if primaryName has extension, trust it; else use fallback.
        return if (primaryName.contains('.')) primary else getCompressFormat(fallbackName)
    }
}
