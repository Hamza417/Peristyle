package app.simple.peri.utils

import android.content.res.Resources
import android.os.Build

object ResourceUtils {
    fun Resources.getFloatCompat(id: Int): Float {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.getFloat(id)
        } else {
            this.getString(id).toFloat()
        }
    }
}