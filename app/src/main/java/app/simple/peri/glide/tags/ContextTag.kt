package app.simple.peri.glide.tags

import android.content.Context

class ContextTag(
        val tag: app.simple.peri.models.Tag,
        val context: Context
) {
    override fun equals(other: Any?): Boolean {
        if (other is ContextTag) {
            return tag == other.tag && tag.sum?.count() == other.tag.sum?.count()
        }

        return false
    }

    override fun hashCode(): Int {
        return tag.hashCode()
    }
}
