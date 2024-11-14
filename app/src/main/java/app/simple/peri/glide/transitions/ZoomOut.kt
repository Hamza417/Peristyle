package app.simple.peri.glide.transitions

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.drawscope.scale
import com.bumptech.glide.integration.compose.DrawPainter
import com.bumptech.glide.integration.compose.Transition

val LinearOutVerySlowInEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)

class ZoomOut(
        private val animationSpec: AnimationSpec<Float>
) : Transition.Factory {
    override fun build(): Transition = ZoomOutImpl(animationSpec)

    companion object : Transition.Factory {
        override fun build(): Transition =
            ZoomOutImpl(animationSpec = tween(400, easing = LinearOutVerySlowInEasing))
    }

    override fun equals(other: Any?): Boolean {
        if (other is ZoomOut) {
            return animationSpec == other.animationSpec
        }
        return false
    }

    override fun hashCode(): Int {
        return animationSpec.hashCode()
    }
}

internal class ZoomOutImpl(
        private val animationSpec: AnimationSpec<Float>
) : Transition {

    private companion object {
        const val INITIAL_SCALE = 0.75f
        const val FINAL_SCALE = 1F
    }

    private val animatable: Animatable<Float, AnimationVector1D> =
        Animatable(INITIAL_SCALE, Float.VectorConverter, FINAL_SCALE)

    override suspend fun transition(invalidate: () -> Unit) {
        try {
            animatable.animateTo(FINAL_SCALE, animationSpec)
            invalidate()
        } finally {
            animatable.snapTo(FINAL_SCALE)
            invalidate()
        }
    }

    override suspend fun stop() {
        animatable.stop()
    }

    override val drawPlaceholder: DrawPainter = { painter, size, alpha, colorFilter ->
        with(painter) {
            scale(animatable.value) {
                val normalizedAlpha = (animatable.value - 0.75f) / (1f - 0.75f)
                draw(size, normalizedAlpha, colorFilter)
            }
        }
    }

    override val drawCurrent: DrawPainter = { painter, size, alpha, colorFilter ->
        with(painter) {
            scale(animatable.value) {
                val normalizedAlpha = (animatable.value - 0.75f) / (1f - 0.75f)
                draw(size, normalizedAlpha, colorFilter)
            }
        }
    }
}
