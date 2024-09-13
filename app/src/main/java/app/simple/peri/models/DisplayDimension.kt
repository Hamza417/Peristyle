package app.simple.peri.models

data class DisplayDimension(var width: Int, var height: Int) {
    fun getAspectRatio(): Float {
        return width.toFloat() / height.toFloat()
    }

    fun getReducedWidth() = width / REDUCER
    fun getReducedHeight() = height / REDUCER

    companion object {
        const val REDUCER = 5
    }
}
