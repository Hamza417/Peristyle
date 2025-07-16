package app.simple.peri.utils

import kotlinx.coroutines.flow.MutableStateFlow

object CommonUtils {

    /**
     * Execute a block of code with a boolean scope flag. What it does is set the flag to true
     * before executing the block, and then set it back to false after the block is done.
     */
    inline fun <T> withBooleanScope(scopeFlag: MutableStateFlow<Boolean>, block: () -> T): T {
        scopeFlag.value = true
        return try {
            block()
        } finally {
            scopeFlag.value = false
        }
    }

    fun Float.toSeconds(): Float {
        return this / 1000f
    }

    fun Long.toSeconds(): Long {
        return this / 1000
    }

    fun Int.toSeconds(): Int {
        return this / 1000
    }

    fun Double.toSeconds(): Double {
        return this / 1000
    }

    fun sizeToRatio(size: String): String {
        val (width, height) = size.split("x").map { it.toInt() }
        fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
        val divisor = gcd(width, height)
        return "${width / divisor}x${height / divisor}"
    }
}
