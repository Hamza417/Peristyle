package app.simple.peri.fractalnav

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal class FractalNavStateImpl : FractalNavState, FractalNavScope, FractalParent {
    // These do not need to be backed by snapshot state because they're set in a side effect.
    lateinit var coroutineScope: CoroutineScope
    override lateinit var zoomAnimationSpecFactory: () -> AnimationSpec<Float>

    private val children = mutableMapOf<String, FractalChild>()
    private val zoomFactorAnimatable = Animatable(0f)
    private val zoomFactor: Float by zoomFactorAnimatable.asState()
    val isFullyZoomedIn by derivedStateOf { zoomFactor == 1f }
    override var zoomDirection: ZoomDirection? by mutableStateOf(null)
        private set

    /**
     * When true, the nav hosts' content should be composed. This is false when a child is fully
     * zoomed-in AND is not imminently starting to zoom out. We compose when starting to zoom out
     * even before the [zoomFactor] animation has started to give the content a chance to initialize
     * before starting the animation, to avoid jank caused by an extra long first-frame.
     */
    val composeContent: Boolean get() = !isFullyZoomedIn || zoomDirection == ZoomDirection.ZoomingOut

    var viewportCoordinates: LayoutCoordinates? = null
    var scaledContentCoordinates: LayoutCoordinates? by mutableStateOf(null)

    override val viewportWidth: Int
        get() = viewportCoordinates?.size?.width ?: 0

    override val hasActiveChild: Boolean get() = activeChild != null
    override val childZoomFactor: Float get() = zoomFactor

    /**
     * The first time the content, and thus this modifier, is composed after
     * starting a zoom-out, this modifier will run before the placeholder has been
     * placed, so we can't calculate the scale. We'll return early, and because
     * the above layer will have set alpha to 0, it doesn't matter that the content
     * is initially in the wrong place/size. The subsequent frame it will be able to
     * calculate correctly.
     */
    private var isContentBeingScaled by mutableStateOf(false)

    /**
     * The child that is currently either zoomed in or out, or fully zoomed in. Null when fully
     * zoomed out.
     */
    override var activeChild: FractalChild? by mutableStateOf(null)
    private var childPlaceholderSize: IntSize? by mutableStateOf(null)

    /**
     * A pair of values representing the x and y scale factors that the content needs to be scaled
     * by to make the placeholder fill the screen.
     */
    private val activeChildScaleTarget: Offset
        get() {
            val coordinates = viewportCoordinates?.takeIf { it.isAttached } ?: return Offset(1f, 1f)
            val childSize = childPlaceholderSize ?: return Offset(1f, 1f)
            return Offset(
                    x = coordinates.size.width / childSize.width.toFloat(),
                    y = coordinates.size.height / childSize.height.toFloat()
            )
        }

    val contentZoomModifier
        get() = if (activeChild != null) {
            Modifier
                .graphicsLayer {
                    // Somehow, even though this modifier should immediately be removed when
                    // activeChild is set to null, it's still running this block so we can just exit
                    // early in that case. Relatedly, it seems that if we *don't* return before
                    // reading zoomFactor, the snapshot system will sometimes throw a
                    // ConcurrentModificationException.
                    if (activeChild == null) return@graphicsLayer

                    // The scale needs to happen around the center of the placeholder.
                    val coordinates = scaledContentCoordinates?.takeIf { it.isAttached }
                    val childCoordinates = activeChild?.placeholderCoordinates?.takeIf { it.isAttached }
                    val childBounds = childCoordinates?.let {
                        coordinates?.localBoundingBoxOf(childCoordinates, clipBounds = false)
                    }

                    if (coordinates == null || childBounds == null) {
                        // If there's an active child but the content's not being scaled it means
                        // we're on the first frame of a zoom-out and the scale couldn't be
                        // calculated yet, so the content is in the wrong place, and we shouldn't
                        // draw it.
                        alpha = 0f
                        isContentBeingScaled = false
                        return@graphicsLayer
                    }
                    isContentBeingScaled = true

                    val scaleTarget = activeChildScaleTarget
                    scaleX = lerp(1f, scaleTarget.x, zoomFactor)
                    scaleY = lerp(1f, scaleTarget.y, zoomFactor)

                    val pivot = TransformOrigin(
                            pivotFractionX = childBounds.center.x / (coordinates.size.width),
                            pivotFractionY = childBounds.center.y / (coordinates.size.height)
                    )
                    transformOrigin = pivot

                    // And we need to translate so that the left edge of the placeholder will be
                    // eventually moved to the left edge of the viewport.
                    val parentCenter = viewportCoordinates!!.size.center.toOffset()
                    val distanceToCenter = parentCenter - childBounds.center
                    translationX = zoomFactor * distanceToCenter.x
                    translationY = zoomFactor * distanceToCenter.y

                    // Fade the content out so that if it's drawing its own background it won't
                    // blink in and out when the content enters/leaves the composition.
                    alpha = 1f - zoomFactor

                    // Radius of 0f causes a crash.
                    renderEffect = BlurEffect(
                            // Swap the x and y scale values for the blur radius so the blur scales
                            // squarely, and not proportionally with the rest of the layer.
                            radiusX = 0.000001f + (zoomFactor * scaleTarget.y),
                            radiusY = 0.000001f + (zoomFactor * scaleTarget.x),
                            // Since this layer is also being clipped, decal gives a better look for
                            // the gradient near the edges than the default.
                            edgeTreatment = TileMode.Decal
                    )
                }
                .composed {
                    // When the scale modifier stops being applied, it's not longer scaling.
                    DisposableEffect(this@FractalNavStateImpl) {
                        onDispose {
                            isContentBeingScaled = false
                        }
                    }
                    Modifier
                }
        } else Modifier

    /**
     * Modifier that is applied to the active child when it's currently being zoomed and has been
     * removed from the content composition to be composed by the host instead.
     */
    val childZoomModifier = Modifier
        .layout { measurable, constraints ->
            // But scale the layout bounds up instead, so the child will grow to fill the space
            // previously filled by the content.
            val childSize = childPlaceholderSize!!.toSize()
            val parentSize = viewportCoordinates!!.size.toSize()
            val scaledSize = lerp(childSize, parentSize, zoomFactor)
            val scaledConstraints = Constraints(
                    minWidth = scaledSize.width.roundToInt(),
                    minHeight = scaledSize.height.roundToInt(),
                    maxWidth = scaledSize.width.roundToInt(),
                    maxHeight = scaledSize.height.roundToInt()
            )
            val placeable = measurable.measure(scaledConstraints)
            layout(
                    constraints.constrainWidth(placeable.width),
                    constraints.constrainHeight(placeable.height)
            ) {
                val coordinates1 = viewportCoordinates!!
                val childCoordinates1 = activeChild?.placeholderCoordinates?.takeIf { it.isAttached }
                // If the activeChild is null, that means the animation finished _just_ before this
                // placement pass – e.g. the user could have been scrolling the content while the
                // animation was still running.
                val offset = if (childCoordinates1 == null || !isContentBeingScaled) {
                    IntOffset.Zero
                } else {
                    val placeholderBounds =
                        coordinates1.localBoundingBoxOf(childCoordinates1, clipBounds = false)
                    placeholderBounds.topLeft.round()
                }
                placeable.place(offset)
            }
        }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun FractalNavChild(
            key: String,
            modifier: Modifier,
            content: @Composable FractalNavChildScope.() -> Unit
    ) {
        key(this, key) {
            // TODO This check fails inside LazyVerticalGrid. It's probably too strict anyway.
            // check(composeContent) {
            //     "FractalNavHost content shouldn't be composed when composeContent is false."
            // }

            val child = remember {
                if (activeChild?.key == key) {
                    // If there's an active child on the first composition, that means that we're
                    // starting a zoom-out and should move the child into the content composition
                    // here by re-using the child's state.
                    activeChild!!
                } else {
                    FractalChild(key, parent = this)
                }
            }

            // Register the child with its key so that zoomToChild can find it.
            DisposableEffect(child) {
                check(key !in children) {
                    "FractalNavChild with key \"$key\" has already been composed."
                }
                children[key] = child
                onDispose {
                    // TODO uncomment this once I figure out why the state is being re-initialized.
                    //child.placeholderCoordinates = null
                    children -= key
                }
            }

            child.setContent(content)

            // When the host is composing the content request the placeholder box to stay at the
            // size the content measured before the animation started.
            val placeholderSizeModifier = if (child === activeChild) {
                childPlaceholderSize?.let { size ->
                    with(LocalDensity.current) {
                        Modifier.size(DpSize(size.width.toDp(), size.height.toDp()))
                    }
                } ?: Modifier
            } else Modifier

            Box(
                    modifier = modifier
                        .onPlaced { child.placeholderCoordinates = it }
                        .workaroundBoxOnPlacedBug()
                        .bringIntoViewRequester(child.bringIntoViewRequester)
                        .then(placeholderSizeModifier),
                    propagateMinConstraints = true
            ) {
                // The active child is always composed directly by the host, so when that happens
                // remove it from the composition here so the movable content is moved and not
                // re-instantiated.
                if (child !== activeChild) {
                    child.MovableContent()
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun zoomToChild(key: String) {
        // Can't zoom into a child while a different child is currently active.
        // However, if we're currently zooming out of a child, and that same child requested to
        // be zoomed in again, we can cancel the zoom out and start zooming in immediately.
        if (activeChild != null &&
                (activeChild?.key != key || zoomDirection != ZoomDirection.ZoomingOut)
        ) {
            return
        }

        val requestedChild = children.getOrElse(key) {
            throw IllegalArgumentException("No child with key \"$key\".")
        }
        activeChild = requestedChild

        // Capture the size before starting the animation since the child's layout node will be
        // removed from wherever it is but we need the placeholder to preserve that space.
        // This could probably be removed once speculative layout exists.
        childPlaceholderSize = activeChild?.placeholderCoordinates?.takeIf { it.isAttached }?.size
        zoomDirection = ZoomDirection.ZoomingIn

        coroutineScope.launch {
            try {
                // Try to make the child fully visible before starting zoom so the scale animation
                // doesn't end up scaling a clipped view.
                requestedChild.bringIntoViewRequester.bringIntoView()
                zoomFactorAnimatable.animateTo(1f, zoomAnimationSpecFactory())
            } finally {
                // If we weren't cancelled by an opposing animation, jump to the end state.
                if (zoomDirection == ZoomDirection.ZoomingIn) {
                    zoomDirection = null
                    // Do this last since it can suspend and we want to make sure the other states are
                    // updated asap.
                    zoomFactorAnimatable.snapTo(1f)
                }
            }
        }
    }

    override fun zoomOut() {
        // If already zooming or zoomed out, do nothing.
        if (activeChild == null || zoomDirection == ZoomDirection.ZoomingOut) return

        zoomDirection = ZoomDirection.ZoomingOut
        coroutineScope.launch {
            // Composing the parent for the first time can take some time. Wait a frame before
            // animating to give it a chance to settle.
            withFrameMillis {}
            try {
                zoomFactorAnimatable.animateTo(0f, zoomAnimationSpecFactory())
            } finally {
                // If we weren't cancelled by an opposing animation, jump to the end state.
                if (zoomDirection == ZoomDirection.ZoomingOut) {
                    activeChild = null
                    zoomDirection = null
                    // Do this last since it can suspend and we want to make sure the other states
                    // are updated asap.
                    zoomFactorAnimatable.snapTo(0f)
                }
            }
        }
    }
}