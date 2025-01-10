package app.simple.peri.fractalnav

import kotlin.math.roundToInt

fun lerp(i1: Int, i2: Int, fraction: Float): Int {
    return (i1 + (i2 - i1) * fraction).roundToInt()
}

fun lerp(f1: Float, f2: Float, fraction: Float): Float {
    return f1 + (f2 - f1) * fraction
}