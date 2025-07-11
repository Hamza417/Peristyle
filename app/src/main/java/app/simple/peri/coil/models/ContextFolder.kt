package app.simple.peri.coil.models

import android.content.Context
import app.simple.peri.models.Folder

class ContextFolder(val folder: Folder, val context: Context) {
    override fun hashCode(): Int {
        return folder.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ContextFolder
                && other.folder.hashcode == folder.hashcode
    }
}
