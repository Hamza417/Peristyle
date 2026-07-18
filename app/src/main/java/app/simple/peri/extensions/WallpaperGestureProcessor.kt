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

    private var maxPointers = 1
    private var initialX = 0f
    private var initialY = 0f
    private var isSwiping = false

    // Distance in pixels required to register a swipe.
    // Triggering early (e.g., 150px) beats the OS screenshot gesture to the punch.
    private val swipeThreshold = 150f

    // Keep GestureDetector ONLY for complex single-touch gestures like Double Tap
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleTap?.invoke()
            return true
        }
    })

    fun processTouchEvent(event: MotionEvent) {
        // Feed events to detector for double tap
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                maxPointers = 1
                initialX = event.x
                initialY = event.y
                isSwiping = true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                maxPointers = maxOf(maxPointers, event.pointerCount)
            }

            MotionEvent.ACTION_MOVE -> {
                // Only process multitouch swipes manually
                if (!isSwiping || maxPointers < 2) return

                val diffX = event.x - initialX
                val diffY = event.y - initialY

                // If distance crosses the threshold, trigger the swipe immediately
                if (abs(diffX) > swipeThreshold || abs(diffY) > swipeThreshold) {
                    val direction = if (abs(diffX) > abs(diffY)) {
                        if (diffX > 0) Direction.RIGHT else Direction.LEFT
                    } else {
                        if (diffY > 0) Direction.DOWN else Direction.UP
                    }

                    when (maxPointers) {
                        2 -> {
                            onTwoFingerSwipe?.invoke(direction)
                        }
                        3 -> {
                            onThreeFingerSwipe?.invoke(direction)
                        }
                    }

                    // Mark as false so we don't trigger multiple times in a single swipe
                    isSwiping = false
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                // Reset swiping state when fingers are lifted or the OS cancels the gesture
                when (event.actionMasked) {
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isSwiping = false
                    }
                }
            }
        }
    }
}