package app.simple.peri.fractalnav

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset

/**
 * Add this modifier after `onPlaced` when applied to a Box. Without it, the onPlaced will never
 * get called. Works by simply adding a no-op layout node wrapper between the onPlaced and the
 * Box. See b/228128961.
 */
internal fun Modifier.workaroundBoxOnPlacedBug(): Modifier =
    this.then(WorkaroundBoxOnPlacedBugModifier)

private object WorkaroundBoxOnPlacedBugModifier : LayoutModifier {
    override fun MeasureScope.measure(
            measurable: Measurable,
            constraints: Constraints
    ): MeasureResult = with(measurable.measure(constraints)) {
        layout(width, height) {
            place(IntOffset.Zero)
        }
    }
}