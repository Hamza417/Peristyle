package app.simple.peri.extensions

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class WallpaperGestureProcessor(
        context: Context,
        private val onDoubleTap: (() -> Unit)? = null,
        private val onTwoFingerSwipe: ((Direction) -> Unit)? = null,
        private val onThreeFingerSwipe: ((Direction) -> Unit)? = null
) {
    enum class Direction { UP, DOWN, LEFT, RIGHT }

    // Track the highest number of fingers touching the screen during a single gesture
    private var maxPointers = 1

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTap?.invoke()
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null) return false

            // Calculate swipe direction
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            val direction = if (abs(diffX) > abs(diffY)) {
                if (diffX > 0) Direction.RIGHT else Direction.LEFT
            } else {
                if (diffY > 0) Direction.DOWN else Direction.UP
            }

            // Trigger the correct callback based on the max fingers recorded
            return when (maxPointers) {
                2 -> {
                    onTwoFingerSwipe?.invoke(direction)
                    true
                }
                3 -> {
                    onThreeFingerSwipe?.invoke(direction)
                    true
                }
                else -> false
            }
        }
    })

    // Feed the MotionEvents from the Wallpaper Engine into this function
    fun processTouchEvent(event: MotionEvent) {
        // Reset our pointer tracker when a new touch sequence begins
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            maxPointers = 1
        }

        // If more fingers touch down, update our max count
        if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN) {
            maxPointers = maxOf(maxPointers, event.pointerCount)
        }

        // Pass the event to the gesture detector
        gestureDetector.onTouchEvent(event)
    }
}