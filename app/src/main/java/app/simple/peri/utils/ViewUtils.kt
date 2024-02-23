package app.simple.peri.utils

import android.view.View
import android.view.ViewGroup
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

    /**
     * Set the left padding of the view
     * @param padding the padding in pixels
     */
    fun View.setPaddingTop(padding: Int) {
        setPadding(paddingLeft, padding, paddingRight, paddingBottom)
    }

    /**
     * Set the bottom padding of the view
     * @param padding the padding in pixels
     */
    fun View.setPaddingBottom(padding: Int) {
        setPadding(paddingLeft, paddingTop, paddingRight, padding)
    }

    /**
     * Get the top most view in the view hierarchy
     * @return the top most view
     */
    fun View.firstChild(): View? {
        if (this is ViewGroup) {
            if (childCount > 0) {
                return getChildAt(0)
            }
        }
        return null
    }
}