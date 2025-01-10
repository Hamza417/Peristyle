package app.simple.peri.fractalnav

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth

internal interface FractalParent : FractalNavScope {
    val activeChild: FractalChild?
    val zoomDirection: ZoomDirection?
    val zoomAnimationSpecFactory: () -> AnimationSpec<Float>
    val viewportWidth: Int

    fun zoomOut()
}

@OptIn(ExperimentalFoundationApi::class)
internal class FractalChild(
        val key: String,
        private val parent: FractalParent
) {
    private val childState = FractalNavState() as FractalNavStateImpl
    private val childScope = ChildScope(childState)
    private var _content: (@Composable FractalNavChildScope.() -> Unit)? by mutableStateOf(null)
    var placeholderCoordinates: LayoutCoordinates? by mutableStateOf(null)
    val bringIntoViewRequester = BringIntoViewRequester()

    /**
     * A [movableContentOf] wrapper that allows all state inside the child's composable to be moved
     * from being composed from inside the content of a [FractalNavHost] to being the root of
     * the host.
     */
    private val movableContent = movableContentOf { modifier: Modifier ->
        FractalNavHost(
                state = childState,
                modifier = modifier,
                zoomAnimationSpecFactory = { parent.zoomAnimationSpecFactory() }
        ) {
            _content?.invoke(childScope)
        }
    }

    fun setContent(content: @Composable FractalNavChildScope.() -> Unit) {
        if (Snapshot.withoutReadObservation { _content == null }) {
            _content = content
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    @Composable
    inline fun MovableContent(modifier: Modifier = Modifier) {
        movableContent(modifier)
    }

    private inner class ChildScope(private val state: FractalNavStateImpl) :
            FractalNavScope by state,
            FractalNavChildScope {
        override val isActive: Boolean by derivedStateOf {
            parent.activeChild === this@FractalChild
        }
        override val isFullyZoomedIn: Boolean by derivedStateOf {
            isActive && parent.childZoomFactor == 1f
        }
        override val zoomFactor: Float by derivedStateOf {
            if (isActive) parent.childZoomFactor else 0f
        }
        override val zoomDirection: ZoomDirection?
            get() = if (isActive) parent.zoomDirection else null

        override fun zoomToChild(key: String) {
            // An inactive child should never have zoomed-in children, so if this child isn't
            // already zoomed in we need to start zooming.
            parent.zoomToChild(this@FractalChild.key)
            state.zoomToChild(key)
        }

        override fun zoomToParent() {
            if (state.hasActiveChild) {
                // A zoomed-out child should never have zoomed-in descendents.
                state.zoomOut()
            }
            parent.zoomOut()
        }

        override fun Modifier.fillExpandedWidth(): Modifier = layout { measurable, constraints ->
            val parentWidth = parent.viewportWidth
            val parentWidthConstraints = constraints.copy(
                    minWidth = parentWidth,
                    maxWidth = parentWidth
            )
            val placeable = measurable.measure(parentWidthConstraints)
            layout(
                    width = constraints.constrainWidth(placeable.width),
                    height = constraints.constrainHeight(placeable.height)
            ) {
                placeable.place(IntOffset.Zero)
            }
        }
    }
}