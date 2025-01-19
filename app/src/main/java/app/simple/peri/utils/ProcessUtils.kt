package app.simple.peri.utils

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

object ProcessUtils {
    fun MutableSet<Job>.cancelAll(cause: String) {
        forEach { it.cancel(CancellationException(cause)) }
        clear()
    }

    @Suppress("DEPRECATION")
    fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        /**
         * "As of Build. VERSION_CODES. O, this method is no longer available
         * to third party applications. For backwards compatibility, it will still
         * return the caller's own services."
         *
         * If we are querying my app's services, this method will work just fine.
         */
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }
}
