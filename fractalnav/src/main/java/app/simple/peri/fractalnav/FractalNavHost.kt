package app.simple.peri.fractalnav

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced

/**
 * A container that can host special composables defined with [FractalNavScope.FractalNavChild] that
 * can be [zoomed][FractalNavScope.zoomToChild] into and replace the content of this host.
 *
 * To preserve navigation state across configuration changes, create your own [FractalNavState] and
 * store it in a retained configuration instance (e.g. an AAC `ViewModel`).
 */
@NonRestartableComposable
@Composable
fun FractalNavHost(
        modifier: Modifier = Modifier,
        state: FractalNavState = remember { FractalNavState() },
        zoomAnimationSpec: AnimationSpec<Float> = FractalNavState.DefaultZoomAnimationSpec,
        content: @Composable FractalNavScope.() -> Unit
) {
    FractalNavHost(
            state = state,
            modifier = modifier,
            zoomAnimationSpecFactory = { zoomAnimationSpec },
            content = content
    )
}

/**
 * Stores the navigation state for a [FractalNavHost].
 * Should only be passed to a single [FractalNavHost] at a time.
 */
sealed interface FractalNavState {
    companion object {
        val DefaultZoomAnimationSpec: AnimationSpec<Float> = tween(1_000)
    }
}

fun FractalNavState(): FractalNavState = FractalNavStateImpl()

@Composable
internal fun FractalNavHost(
        state: FractalNavState,
        modifier: Modifier,
        zoomAnimationSpecFactory: () -> AnimationSpec<Float>,
        content: @Composable FractalNavScope.() -> Unit
) {
    state as FractalNavStateImpl
    val coroutineScope = rememberCoroutineScope()
    SideEffect {
        state.coroutineScope = coroutineScope
        state.zoomAnimationSpecFactory = zoomAnimationSpecFactory
    }

    val contentStateHolder = rememberSaveableStateHolder()

    // This box serves two purposes:
    //  1. It defines what the viewport coordinates are, and gives us a place to put the modifier
    //     to read them that won't become detached in the middle of zoom animations.
    //  2. The layout behavior of Box ensures that the active child will be drawn over the content.
    Box(
            modifier = modifier
                .onPlaced { state.viewportCoordinates = it }
                .workaroundBoxOnPlacedBug(),
            propagateMinConstraints = true
    ) {
        if (state.composeContent) {
            contentStateHolder.SaveableStateProvider("fractal-nav-host") {
                Box(
                        modifier = Modifier
                            .then(state.contentZoomModifier)
                            .onPlaced { state.scaledContentCoordinates = it }
                            .workaroundBoxOnPlacedBug(),
                        propagateMinConstraints = true
                ) {
                    content(state)
                }
            }
        }

        state.activeChild?.MovableContent(
                modifier = if (state.isFullyZoomedIn) Modifier else state.childZoomModifier
        )
    }
}