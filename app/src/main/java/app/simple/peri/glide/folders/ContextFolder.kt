package app.simple.peri.glide.folders

import android.content.Context
import app.simple.peri.models.Folder

class ContextFolder(val folder: Folder, val context: Context) {
    override fun hashCode(): Int {
        return folder.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ContextFolder && other.folder == folder
    }
}
