package app.simple.peri.utils

import android.content.Context

object AppUtils {
    fun Context.isLandscape(): Boolean {
        return resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    }
}