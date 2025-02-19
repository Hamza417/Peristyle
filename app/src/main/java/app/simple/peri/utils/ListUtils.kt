package app.simple.peri.utils

object ListUtils {
    fun <T> List<T>.deepEquals(other: List<T>) =
        size == other.size && asSequence()
            .mapIndexed { index, element -> element == other[index] }
            .all { it }
}