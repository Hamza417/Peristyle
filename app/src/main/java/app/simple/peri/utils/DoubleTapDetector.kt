package app.simple.peri.utils

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class DoubleTapDetector(
        context: Context,
        private val onDoubleTapListener: (MotionEvent) -> Unit
) {
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTapListener(e)
            return true
        }
    })

    fun onTouchEvent(event: MotionEvent) {
        gestureDetector.onTouchEvent(event)
    }
}