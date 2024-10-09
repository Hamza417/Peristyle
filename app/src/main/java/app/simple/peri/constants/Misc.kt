package app.simple.peri.constants

object Misc {

    const val BLUR_TIMES = 4
    const val COMPRESSION_PERCENTAGE = 50
    var leftMargin = 0
    var rightMargin = 0
    var topMargin = 0
    var bottomMargin = 0

    private var displayWidth = 0
    private var displayHeight = 0

    fun setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        leftMargin = left
        rightMargin = right
        topMargin = top
        bottomMargin = bottom
    }

    fun setDisplaySize(width: Int, height: Int) {
        displayWidth = width
        displayHeight = height
    }

    fun getDisplayWidth(): Int {
        return displayWidth
    }

    fun getDisplayHeight(): Int {
        return displayHeight
    }
}
