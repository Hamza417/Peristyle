package app.simple.peri.storage

import android.content.Context
import java.io.File
import kotlin.math.ln
import kotlin.math.pow

/**
 * Kotlin extension functions for RemovableStorageDetector.
 * Provides a more idiomatic Kotlin API for storage detection.
 */
object RemovableStorageExtensions {

    private const val TAG = "RemovableStorageExtensions"

    /**
     * Get the primary SD card path, or null if not available.
     */
    fun Context.getPrimarySDCardPath(): File? {
        return RemovableStorageDetector.getPrimaryRemovableStoragePath(this)
    }

    /**
     * Get all removable storage paths.
     */
    fun Context.getAllSDCardPaths(): List<File> {
        return RemovableStorageDetector.getAllRemovableStoragePaths(this)
    }

    /**
     * Get all storage volumes with detailed information.
     */
    fun Context.getAllStorageVolumes(): List<RemovableStorageDetector.StorageInfo> {
        return RemovableStorageDetector.getAllStorageVolumes(this)
    }

    /**
     * Get only removable storage volumes.
     */
    fun Context.getRemovableStorageVolumes(): List<RemovableStorageDetector.StorageInfo> {
        return RemovableStorageDetector.getRemovableStorageVolumes(this)
    }

    /**
     * Check if any SD card is available and accessible.
     */
    fun Context.hasAccessibleSDCard(): Boolean {
        return getRemovableStorageVolumes().any { it.isAccessible }
    }

    /**
     * Get the first accessible SD card path.
     */
    fun Context.getFirstAccessibleSDCard(): File? {
        return getRemovableStorageVolumes()
            .firstOrNull { it.isAccessible }
            ?.path()
    }

    /**
     * Execute a block of code with the SD card path if available.
     */
    inline fun Context.withSDCard(block: (File) -> Unit) {
        getPrimarySDCardPath()?.let { block(it) }
    }

    /**
     * Execute a block for each available SD card.
     */
    inline fun Context.forEachSDCard(block: (File) -> Unit) {
        getAllSDCardPaths().forEach { block(it) }
    }

    /**
     * Extension functions for StorageInfo class.
     */

    /**
     * Format bytes to human-readable string.
     */
    fun Long.formatBytes(): String {
        if (this < 1024) return "$this B"
        val exp = (ln(this.toDouble()) / ln(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format("%.2f %sB", this / 1024.0.pow(exp.toDouble()), pre)
    }

    /**
     * Get formatted total space string.
     */
    val RemovableStorageDetector.StorageInfo.totalSpaceFormatted: String
        get() = totalSpace.formatBytes()

    /**
     * Get formatted free space string.
     */
    val RemovableStorageDetector.StorageInfo.freeSpaceFormatted: String
        get() = freeSpace.formatBytes()

    /**
     * Get formatted usable space string.
     */
    val RemovableStorageDetector.StorageInfo.usableSpaceFormatted: String
        get() = usableSpace.formatBytes()

    /**
     * Get space usage percentage.
     */
    val RemovableStorageDetector.StorageInfo.usagePercentage: Float
        get() = if (totalSpace > 0) {
            ((totalSpace - freeSpace).toFloat() / totalSpace.toFloat()) * 100f
        } else 0f

    /**
     * Check if storage is low on space (less than 10% free).
     */
    val RemovableStorageDetector.StorageInfo.isLowOnSpace: Boolean
        get() = usagePercentage > 90f

    /**
     * Get a summary string of the storage info.
     */
    fun RemovableStorageDetector.StorageInfo.toSummary(): String {
        return buildString {
            append("Storage: ${path()?.absolutePath ?: "unknown"}\n")
            append("Type: ${if (isRemovable) "Removable" else "Internal"}")
            if (isPrimary) append(" (Primary)")
            append("\n")
            append("Status: ${if (isMounted) "Mounted" else "Unmounted"}")
            if (!isAccessible) append(" - Not Accessible")
            append("\n")
            append("Space: $freeSpaceFormatted free / $totalSpaceFormatted total ")
            append("(${String.format("%.1f", usagePercentage)}% used)")
            if (description() != null) {
                append("\nDescription: ${description()}")
            }
        }
    }
}