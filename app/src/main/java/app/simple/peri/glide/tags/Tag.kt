package app.simple.peri.glide.tags

import android.content.Context

class Tag(
        val tag: app.simple.peri.models.Tag?,
        val context: Context
) {
    override fun equals(other: Any?): Boolean {
        if (other is Tag) {
            return tag == other.tag
        }
        return false
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}
