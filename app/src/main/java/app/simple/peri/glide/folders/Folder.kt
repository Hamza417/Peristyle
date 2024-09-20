package app.simple.peri.glide.folders

import android.content.Context

class Folder(val hashCode: Int, val context: Context) {
    override fun hashCode(): Int {
        return hashCode
    }

    override fun equals(other: Any?): Boolean {
        return other is Folder && other.hashCode == hashCode
    }
}
