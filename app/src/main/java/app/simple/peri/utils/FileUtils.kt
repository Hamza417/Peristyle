package app.simple.peri.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import app.simple.peri.utils.StringUtils.endsWithAny
import java.io.File
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.Stack

object FileUtils {

    val imageExtensions = arrayOf(".jpg", ".jpeg", ".png", ".webp")

    fun DocumentFile.isImageFile(): Boolean {
        return this.name!!.lowercase().endsWithAny(*imageExtensions)
    }

    fun String.toFile(): File {
        return File(this)
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

    fun File.listCompleteFiles(): List<File> {
        val allFiles = mutableListOf<File>()
        val stack = Stack<File>()
        stack.push(this)

        while (stack.isNotEmpty()) {
            val currentFile = stack.pop()

            currentFile.listFiles()?.forEach { child ->
                when {
                    child.isDirectory -> {
                        stack.push(child)
                    }

                    child.isFile -> {
                        if (child.name.lowercase().endsWithAny(*imageExtensions)) {
                            allFiles.add(child)
                        }
                    }
                }
            }
        }

        return allFiles
    }

    fun File.listOnlyFirstLevelFiles(): List<File> {
        val allFiles = mutableListOf<File>()
        this.listFiles()?.forEach { child ->
            if (child.isFile) {
                if (child.name.lowercase().endsWithAny(*imageExtensions)) {
                    allFiles.add(child)
                }
            }
        }

        return allFiles
    }

    fun List<File>.filterDotFiles(): ArrayList<File> {
        return this.filter { !it.name.startsWith(".") } as ArrayList<File>
    }

    fun InputStream.generateMD5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(this.readBytes()))
            .toString(16).padStart(32, '0')
    }

    fun File.toFileUri(context: Context): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".provider", this)
    }

    fun String.toFileUri(context: Context): Uri {
        return FileProvider.getUriForFile(context, context.packageName + ".provider", File(this))
    }
}
