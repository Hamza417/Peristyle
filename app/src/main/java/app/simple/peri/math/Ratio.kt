package app.simple.peri.math

import app.simple.peri.models.Ratio

object Ratio {

    fun greatestCommonDivisor(a: Int, b: Int): Int {
        return if (b == 0) a else greatestCommonDivisor(b, a % b)
    }

    fun greatestCommonDivisor(a: Long, b: Long): Long {
        return if (b == 0L) a else greatestCommonDivisor(b, a % b)
    }

    fun greatestCommonDivisor(a: Float, b: Float): Float {
        return if (b == 0f) a else greatestCommonDivisor(b, a % b)
    }

    fun greatestCommonDivisor(a: Double, b: Double): Double {
        return if (b == 0.0) a else greatestCommonDivisor(b, a % b)
    }

    fun calculateAspectRatio(width: Int, height: Int): Ratio {
        val gcd = greatestCommonDivisor(width, height)
        val ratioWidth = width / gcd
        val ratioHeight = height / gcd
        return Ratio(ratioWidth.toFloat(), ratioHeight.toFloat())
    }

    fun calculateAspectRatio(width: Long, height: Long): Ratio {
        val gcd = greatestCommonDivisor(width, height)
        val ratioWidth = width / gcd
        val ratioHeight = height / gcd
        return Ratio(ratioWidth.toFloat(), ratioHeight.toFloat())
    }

    fun calculateAspectRatio(width: Float, height: Float): Ratio {
        val gcd = greatestCommonDivisor(width, height)
        val ratioWidth = width / gcd
        val ratioHeight = height / gcd
        return Ratio(ratioWidth, ratioHeight)
    }

    fun calculateAspectRatio(width: Double, height: Double): Ratio {
        val gcd = greatestCommonDivisor(width, height)
        val ratioWidth = width / gcd
        val ratioHeight = height / gcd
        return Ratio(ratioWidth.toFloat(), ratioHeight.toFloat())
    }
}