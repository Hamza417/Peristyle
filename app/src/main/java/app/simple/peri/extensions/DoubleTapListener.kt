package app.simple.peri.extensions

import android.view.GestureDetector
import android.view.MotionEvent

@Suppress("ConvertSecondaryConstructorToPrimary")
class DoubleTapListener : GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private val onDoubleTapCallback: (MotionEvent) -> Boolean

    constructor(onDoubleTapCallback: (MotionEvent) -> Boolean) {
        this.onDoubleTapCallback = onDoubleTapCallback
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        return onDoubleTapCallback(e)
    }

    // No-op implementations for other methods
    override fun onDown(e: MotionEvent): Boolean = false
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = false
    override fun onLongPress(e: MotionEvent) {}
    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = false
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean = false
    override fun onDoubleTapEvent(e: MotionEvent): Boolean = false
}