package app.simple.peri.models

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager

data class DisplayDimension(var width: Int, var height: Int) {
    fun getAspectRatio(): Float {
        return if (width > 0 && height > 0) {
            width.toFloat() / height.toFloat()
        } else {
            1f
        }
    }

    fun getAspectRatio(context: Context): Float {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val displayMetrics = context.resources.displayMetrics
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowInsets = windowManager.currentWindowMetrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.statusBars())

            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels + insets.top
            Log.i("DisplayDimension", "Width: $width, Height: $height")
            return width.toFloat() / height.toFloat()
        } else {
            val displayMetrics = context.resources.displayMetrics
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
            Log.i("DisplayDimension", "Width: $width, Height: $height")
            return width.toFloat() / height.toFloat()
        }
    }

    fun getReducedWidth() = width / REDUCER
    fun getReducedHeight() = height / REDUCER

    companion object {
        const val REDUCER = 5
    }
}
