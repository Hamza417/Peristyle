package app.simple.peri.utils

import android.view.View
import android.view.ViewTreeObserver

object ViewUtils {
    inline fun <T : View> T.afterMeasured(crossinline function: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    function()
                }
            }
        })
    }
}