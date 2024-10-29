package app.simple.peri.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

object ProcessUtils {
    fun MutableSet<Job>.cancelAll(cause: String) {
        forEach { it.cancel(CancellationException(cause)) }
        clear()
    }
}
