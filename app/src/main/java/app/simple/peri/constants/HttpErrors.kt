package app.simple.peri.constants

object HttpErrors {
    // User-friendly HTTP/network error messages for normal users
    const val GENERIC = "Something went wrong. Please try again."
    const val OFFLINE = "You're offline. Check your internet and try again."
    const val TIMEOUT = "That took too long. Please try again."
    const val SERVER = "Server is having issues. Please try again later."
    const val RATE_LIMIT = "You're doing that too much. Please try again in a bit."
    const val UNAUTHORIZED = "You're not authorized to do that."
    const val FORBIDDEN = "Access is forbidden."
    const val NOT_FOUND = "Content not found."

    fun userFriendly(t: Throwable): String {
        return when (t) {
            is java.net.UnknownHostException,
            is java.net.ConnectException -> OFFLINE
            is java.net.SocketTimeoutException -> TIMEOUT
            is retrofit2.HttpException -> when (t.code()) {
                401 -> UNAUTHORIZED
                403 -> FORBIDDEN
                404 -> NOT_FOUND
                408 -> TIMEOUT
                429 -> RATE_LIMIT
                in 500..599 -> SERVER
                else -> GENERIC
            }
            else -> GENERIC
        }
    }
}

