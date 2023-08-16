package app.simple.peri.constants

object Misc {

    var leftMargin = 0
    var rightMargin = 0
    var topMargin = 0
    var bottomMargin = 0

    fun setMargins(left: Int, top: Int, right: Int, bottom: Int) {
        leftMargin = left
        rightMargin = right
        topMargin = top
        bottomMargin = bottom
    }
}