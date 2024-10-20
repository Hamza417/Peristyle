package app.simple.peri.compose.commons

import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import app.simple.peri.compose.screens.displayDimension

@Composable
fun InitDisplayDimension() {
    val view = LocalView.current

    DisposableEffect(view) {
        val observer = view.viewTreeObserver
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (observer.isAlive) {
                    observer.removeOnGlobalLayoutListener(this)
                    displayDimension.width = view.width
                    displayDimension.height = view.height
                    Log.i("InitDisplayDimension", "Width: ${displayDimension.width}, Height: ${displayDimension.height}")
                }
            }
        }
        observer.addOnGlobalLayoutListener(listener)

        onDispose {
            observer.removeOnGlobalLayoutListener(listener)
        }
    }
}
