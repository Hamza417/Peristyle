package app.simple.peri.tools

import android.graphics.Canvas
import android.graphics.Matrix
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.core.math.MathUtils
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2

class ZoomGestureDetector(private val listener: Listener) {

    companion object {
        const val MIN_SCALE = 0.01f
        const val MAX_SCALE = 100f
        const val MIN_FLING_VELOCITY = 50f
        const val MAX_FLING_VELOCITY = 8000f
    }

    // public
    var isZoomEnabled: Boolean = true
    var isScaleEnabled: Boolean = true
    var isRotationEnabled: Boolean = true
    var isTranslationEnabled: Boolean = true
    var isFlingEnabled: Boolean = true

    // local
    private val mDrawMatrix: Matrix = Matrix()
    private val mTouchMatrix: Matrix = Matrix()
    private val mPointerMap: HashMap<Int, Position> = HashMap()
    private val mTouchPoint: FloatArray = floatArrayOf(0f, 0f)
    private val mPivotPoint: FloatArray = floatArrayOf(0f, 0f)

    // transformations
    private var mTranslationX: Float = 0f
    private var mTranslationY: Float = 0f
    private var mScaling: Float = 1f
    private var mPivotX: Float = 0f
    private var mPivotY: Float = 0f
    private var mRotation: Float = 0f

    // previous values
    private var mPreviousFocusX: Float = 0f
    private var mPreviousFocusY: Float = 0f
    private var mPreviousTouchSpan: Float = 1f

    // fling related
    private var mVelocityTracker: VelocityTracker? = null
    private var mFlingAnimX: FlingAnimation? = null
    private var mFlingAnimY: FlingAnimation? = null

    fun updateTouchLocation(event: MotionEvent) {
        mTouchPoint[0] = event.x
        mTouchPoint[1] = event.y
        mTouchMatrix.mapPoints(mTouchPoint)
        event.setLocation(mTouchPoint[0], mTouchPoint[1])
    }

    fun updateCanvasMatrix(canvas: Canvas) {
        canvas.setMatrix(mDrawMatrix)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        if (isZoomEnabled) {
            // update velocity tracker
            if (isFlingEnabled) {
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain()
                }
                mVelocityTracker?.addMovement(event)
            }
            // handle touch events
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // update focus point
                    mPreviousFocusX = event.x
                    mPreviousFocusY = event.y
                    event.savePointers()
                    // cancel ongoing fling animations
                    if (isFlingEnabled) {
                        mFlingAnimX?.cancel()
                        mFlingAnimY?.cancel()
                    }
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    updateTouchParameters(event)
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    // Check the dot product of current velocities.
                    // If the pointer that left was opposing another velocity vector, clear.
                    if (isFlingEnabled) {
                        mVelocityTracker?.let { tracker ->
                            tracker.computeCurrentVelocity(1000, MAX_FLING_VELOCITY)
                            val upIndex: Int = event.actionIndex
                            val id1: Int = event.getPointerId(upIndex)
                            val x1 = tracker.getXVelocity(id1)
                            val y1 = tracker.getYVelocity(id1)
                            for (i in 0 until event.pointerCount) {
                                if (i == upIndex) continue
                                val id2: Int = event.getPointerId(i)
                                val x = x1 * tracker.getXVelocity(id2)
                                val y = y1 * tracker.getYVelocity(id2)
                                val dot = x + y
                                if (dot < 0) {
                                    tracker.clear()
                                    break
                                }
                            }
                        }
                    }
                    updateTouchParameters(event)
                }
                MotionEvent.ACTION_UP -> {
                    // do fling animation
                    if (isFlingEnabled) {
                        mVelocityTracker?.let { tracker ->
                            val pointerId: Int = event.getPointerId(0)
                            tracker.computeCurrentVelocity(1000, MAX_FLING_VELOCITY)
                            val velocityY: Float = tracker.getYVelocity(pointerId)
                            val velocityX: Float = tracker.getXVelocity(pointerId)
                            if (abs(velocityY) > MIN_FLING_VELOCITY || abs(velocityX) > MIN_FLING_VELOCITY) {
                                val translateX = mTranslationX
                                val translateY = mTranslationY
                                val valueHolder = FloatValueHolder()
                                mFlingAnimX = FlingAnimation(valueHolder).apply {
                                    setStartVelocity(velocityX)
                                    setStartValue(0f)
                                    addUpdateListener { _, value, _ ->
                                        mTranslationX = translateX + value
                                        updateDrawMatrix()
                                        listener.onZoom(mScaling, mRotation, mTranslationX to mTranslationY, mPivotX to mPivotY)
                                    }
                                    addEndListener { _, _, _, _ ->
                                        updateTouchMatrix()
                                    }
                                    start()
                                }
                                mFlingAnimY = FlingAnimation(valueHolder).apply {
                                    setStartVelocity(velocityY)
                                    setStartValue(0f)
                                    addUpdateListener { _, value, _ ->
                                        mTranslationY = translateY + value
                                        updateDrawMatrix()
                                        listener.onZoom(mScaling, mRotation, mTranslationX to mTranslationY, mPivotX to mPivotY)
                                    }
                                    addEndListener { _, _, _, _ ->
                                        updateTouchMatrix()
                                    }
                                    start()
                                }
                            }
                            tracker.recycle()
                            mVelocityTracker = null
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val (focusX, focusY) = event.focalPoint()
                    if (event.pointerCount > 1) {
                        if (isScaleEnabled) {
                            val touchSpan = event.touchSpan(focusX, focusY)
                            mScaling *= scaling(touchSpan)
                            mScaling = MathUtils.clamp(mScaling, MIN_SCALE, MAX_SCALE)
                            mPreviousTouchSpan = touchSpan
                        }
                        if (isRotationEnabled) {
                            mRotation += event.rotation(focusX, focusY)
                        }
                        if (isTranslationEnabled) {
                            val (translationX, translationY) = translation(focusX, focusY)
                            mTranslationX += translationX
                            mTranslationY += translationY
                        }
                    } else {
                        if (isTranslationEnabled) {
                            val (translationX, translationY) = translation(focusX, focusY)
                            mTranslationX += translationX
                            mTranslationY += translationY
                        }
                    }
                    mPreviousFocusX = focusX
                    mPreviousFocusY = focusY
                    updateTouchMatrix()
                    updateDrawMatrix()
                    event.savePointers()
                    listener.onZoom(mScaling, mRotation, mTranslationX to mTranslationY, mPivotX to mPivotY)
                }
            }
            return true
        }
        return false
    }

    // update focus point, touch span and pivot point
    private fun updateTouchParameters(event: MotionEvent) {
        val (focusX, focusY) = event.focalPoint()
        mPreviousFocusX = focusX
        mPreviousFocusY = focusY
        mPreviousTouchSpan = event.touchSpan(focusX, focusY)
        updatePivotPoint(focusX, focusY)
        updateTouchMatrix()
        updateDrawMatrix()
        event.savePointers()
        listener.onZoom(mScaling, mRotation, mTranslationX to mTranslationY, mPivotX to mPivotY)
    }

    // touch matrix is used to transform touch points
    // on the child view and to find pivot point
    private fun updateTouchMatrix() {
        mTouchMatrix.reset()
        mTouchMatrix.preTranslate(-mTranslationX, -mTranslationY)
        mTouchMatrix.postRotate(-mRotation, mPivotX, mPivotY)
        mTouchMatrix.postScale(1f / mScaling, 1f / mScaling, mPivotX, mPivotY)
    }

    // draw matrix is used to transform child view when drawing on the canvas
    private fun updateDrawMatrix() {
        mDrawMatrix.reset()
        mDrawMatrix.preScale(mScaling, mScaling, mPivotX, mPivotY)
        mDrawMatrix.preRotate(mRotation, mPivotX, mPivotY)
        mDrawMatrix.postTranslate(mTranslationX, mTranslationY)
    }

    // this updates the pivot point and translation error caused by changing the pivot point
    private fun updatePivotPoint(focusX: Float, focusY: Float) {
        // update point
        mPivotPoint[0] = focusX
        mPivotPoint[1] = focusY
        mTouchMatrix.mapPoints(mPivotPoint)
        mPivotX = mPivotPoint[0]
        mPivotY = mPivotPoint[1]
        // correct pivot error
        mDrawMatrix.mapPoints(mPivotPoint)
        mTranslationX -= mTranslationX + mPivotX - mPivotPoint[0]
        mTranslationY -= mTranslationY + mPivotY - mPivotPoint[1]
    }

    private fun MotionEvent.focalPoint(): Pair<Float, Float> {
        val upIndex = if (actionMasked == MotionEvent.ACTION_POINTER_UP) actionIndex else -1
        var sumX = 0f
        var sumY = 0f
        var sumCount = 0
        for (pointerIndex in 0 until pointerCount) {
            if (pointerIndex == upIndex) continue
            sumX += getX(pointerIndex)
            sumY += getY(pointerIndex)
            sumCount++
        }
        val focusX = sumX / sumCount
        val focusY = sumY / sumCount
        return focusX to focusY
    }

    private fun MotionEvent.touchSpan(
        currentFocusX: Float,
        currentFocusY: Float
    ): Float {
        var spanSumX = 0f
        var spanSumY = 0f
        var sumCount = 0
        val ignoreIndex = if (actionMasked == MotionEvent.ACTION_POINTER_UP) actionIndex else -1
        for (pointerIndex in 0 until pointerCount) {
            if (pointerIndex == ignoreIndex) continue
            spanSumX += abs(currentFocusX - getX(pointerIndex))
            spanSumY += abs(currentFocusY - getY(pointerIndex))
            sumCount++
        }
        if (sumCount > 1) {
            val spanX = spanSumX / sumCount
            val spanY = spanSumY / sumCount
            return spanX + spanY
        }
        return mPreviousTouchSpan
    }

    private fun scaling(currentTouchSpan: Float): Float {
        return currentTouchSpan / mPreviousTouchSpan
    }

    private fun MotionEvent.rotation(
        currentFocusX: Float,
        currentFocusY: Float
    ): Float {
        var rotationSum = 0f
        var weightSum = 0f
        for (pointerIndex in 0 until pointerCount) {
            val pointerId = getPointerId(pointerIndex)
            val x1 = getX(pointerIndex)
            val y1 = getY(pointerIndex)
            val (x2, y2) = mPointerMap[pointerId] ?: continue
            val dx1 = x1 - currentFocusX
            val dy1 = y1 - currentFocusY
            val dx2 = x2 - currentFocusX
            val dy2 = y2 - currentFocusY
            // dot product is proportional to the cosine of the angle
            // the determinant is proportional to its sine
            // sign of the rotation tells if it is clockwise or counter-clockwise
            val dot = dx1 * dx2 + dy1 * dy2
            val det = dy1 * dx2 - dx1 * dy2
            val rotation = atan2(det, dot)
            val weight = abs(dx1) + abs(dy1)
            rotationSum += rotation * weight
            weightSum += weight
        }
        if (weightSum > 0f) {
            val rotation = rotationSum / weightSum
            return rotation * 180f / PI.toFloat()
        }
        return 0f
    }

    private fun translation(
        currentFocusX: Float,
        currentFocusY: Float
    ): Pair<Float, Float> {
        return (currentFocusX - mPreviousFocusX) to (currentFocusY - mPreviousFocusY)
    }

    private fun MotionEvent.savePointers() {
        mPointerMap.clear()
        for (pointerIndex in 0 until pointerCount) {
            val id = getPointerId(pointerIndex)
            val x = getX(pointerIndex)
            val y = getY(pointerIndex)
            mPointerMap[id] = x to y
        }
    }

    interface Listener {
        fun onZoom(scaling: Float, rotation: Float, translation: Position, pivot: Position)
    }

}

typealias Position = Pair<Float, Float>