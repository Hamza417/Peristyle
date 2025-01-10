package app.simple.peri.fractalnav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

enum class ZoomDirection {
    ZoomingIn,
    ZoomingOut
}

interface FractalNavScope {
    /**
     * True when a child of this scope is being zoomed in or has fully-zoomed in.
     * Always false initially.
     */
    val hasActiveChild: Boolean

    /**
     * When [hasActiveChild] is true, this is the zoom factor of the active child.
     */
    val childZoomFactor: Float

    /**
     * Defines a child composable that can be zoomed into by calling [zoomToChild].
     * The [content] of this function is automatically placed inside its own [FractalNavHost], and
     * so can define its own children.
     */
    @Composable
    fun FractalNavChild(
            key: String,
            modifier: Modifier,
            content: @Composable FractalNavChildScope.() -> Unit
    )

    /**
     * Requests that the [FractalNavChild] passed the given [key] be activated and eventually
     * entirely replace the content of this [FractalNavHost].
     */
    fun zoomToChild(key: String)
}

/**
 * A [FractalNavScope] that is also a child of a [FractalNavChild].
 */
interface FractalNavChildScope : FractalNavScope {
    /**
     * True if this [FractalNavChild] is being zoomed in or out of, or is fully zoomed in.
     * Only one sibling may be active at a time.
     * When this is false, [isFullyZoomedIn] will always be false.
     */
    val isActive: Boolean

    /**
     * True if this [FractalNavChild] is completely replacing its parent content.
     * When this is true, [isActive] will always be true.
     */
    val isFullyZoomedIn: Boolean

    /**
     * The amount that this child is zoomed, between 0 ([isActive]=false) and
     * 1 ([isFullyZoomedIn]=true).
     */
    val zoomFactor: Float

    /** Null when not zooming. */
    val zoomDirection: ZoomDirection?

    /** Requests the parent of this [FractalNavChild] zoom it out and eventually deactivate it. */
    fun zoomToParent()

    fun Modifier.scaleLayoutByZoomFactor(): Modifier = scaleLayout { zoomFactor }
    fun Modifier.scaleByZoomFactor(): Modifier = graphicsLayer {
        scaleX = zoomFactor
        scaleY = zoomFactor
    }

    fun Modifier.alphaByZoomFactor(): Modifier = graphicsLayer { alpha = zoomFactor }

    /**
     * Measures the modified element with the width constraints it will have when fully zoomed-in.
     */
    fun Modifier.fillExpandedWidth(): Modifier
}