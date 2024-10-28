package app.simple.peri.utils

import androidx.documentfile.provider.DocumentFile
import app.simple.peri.utils.FileUtils.isImageFile
import java.util.Stack

object DocumentFileUtils {
    fun DocumentFile.listOnlyFirstLevelFiles(): List<DocumentFile> {
        val allFiles = mutableListOf<DocumentFile>()
        this.listFiles().forEach { child ->
            if (child.isFile) {
                if (child.isImageFile()) {
                    allFiles.add(child)
                }
            }
        }

        return allFiles
    }

    fun List<DocumentFile>.filterDotFiles(): ArrayList<DocumentFile> {
        return this.filter { !it.name!!.startsWith(".") } as ArrayList<DocumentFile>
    }

    fun DocumentFile.listCompleteFiles(): List<DocumentFile> {
        val allFiles = mutableListOf<DocumentFile>()
        val stack = Stack<DocumentFile>()
        stack.push(this)

        while (stack.isNotEmpty()) {
            val currentFile = stack.pop()

            currentFile.listFiles().forEach { child ->
                when {
                    child.isDirectory -> {
                        stack.push(child)
                    }

                    child.isFile -> {
                        if (child.isImageFile()) {
                            allFiles.add(child)
                        }
                    }
                }
            }
        }

        return allFiles
    }
}
