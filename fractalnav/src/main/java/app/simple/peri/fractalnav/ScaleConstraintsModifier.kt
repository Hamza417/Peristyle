package app.simple.peri.fractalnav

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlin.math.roundToInt

/**
 * Insets and centers a layout by a [factor] of its measured size.
 * Does not actually scale the layer, so the modified element may draw outside its bounds.
 * Works around the [scale] modifier not being used correctly in calculations.
 */
internal fun Modifier.scaleLayout(factor: () -> Float): Modifier = layout { m, c ->
    val scale = factor()
    if (scale == 0f) {
        // Don't measure or place if it won't take any space anyway.
        return@layout layout(0, 0) {}
    }
    val p = m.measure(c)
    val width = p.width * scale
    val height = p.height * scale
    layout(width.roundToInt(), height.roundToInt()) {
        val center = Offset(width, height) / 2f
        val pCenter = IntOffset(p.width, p.height) / 2f
        p.place(center.round() - pCenter)
    }
}