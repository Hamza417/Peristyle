package app.simple.waller.utils

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.simple.waller.utils.StringUtils.endsWithAny

object FileUtils {

    private val imageExtensions = arrayOf(".jpg", ".jpeg", ".png", ".webp")

    fun DocumentFile.isImageFile(): Boolean {
        return this.name!!.endsWithAny(*imageExtensions)
    }

    fun String.toUri(): Uri {
        return Uri.parse(this)
    }
}