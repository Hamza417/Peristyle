package app.simple.peri.utils

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.simple.peri.utils.StringUtils.endsWithAny
import java.text.CharacterIterator
import java.text.StringCharacterIterator

object FileUtils {

    private val imageExtensions = arrayOf(".jpg", ".jpeg", ".png", ".webp")

    fun DocumentFile.isImageFile(): Boolean {
        return this.name!!.lowercase().endsWithAny(*imageExtensions)
    }

    fun String.toUri(): Uri {
        return Uri.parse(this)
    }

    fun Int.toSize(): String {
        return toLong().toSize()
    }

    fun Long.toSize(): String {
        return this.humanReadableByteCountSI()
    }

    private fun Long.humanReadableByteCountSI(): String {
        var bytes = this
        if (-1000 < bytes && bytes < 1000) {
            return "$bytes B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current())
    }
}