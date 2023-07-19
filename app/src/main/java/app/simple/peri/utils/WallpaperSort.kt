package app.simple.peri.utils

import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences

object WallpaperSort {

    const val NAME = "name"
    const val DATE = "date"
    const val SIZE = "size"
    const val WIDTH = "width"
    const val HEIGHT = "height"

    const val ASC = "asc"
    const val DESC = "desc"

    fun ArrayList<Wallpaper>.getSortedList() {
        when (MainPreferences.getSort()) {
            NAME -> sortByName()
            DATE -> sortByDate()
            SIZE -> sortBySize()
            WIDTH -> sortByWidth()
            HEIGHT -> sortByHeight()
        }
    }

    private fun ArrayList<Wallpaper>.sortByName() {
        if (isOrderAsc()) {
            sortBy { it.name }
        } else {
            sortByDescending { it.name }
        }
    }

    private fun ArrayList<Wallpaper>.sortByDate() {
        if (isOrderAsc()) {
            sortBy { it.dateModified }
        } else {
            sortByDescending { it.dateModified }
        }
    }

    private fun ArrayList<Wallpaper>.sortBySize() {
        if (isOrderAsc()) {
            sortBy { it.size }
        } else {
            sortByDescending { it.size }
        }
    }

    private fun ArrayList<Wallpaper>.sortByWidth() {
        if (isOrderAsc()) {
            sortBy { it.width }
        } else {
            sortByDescending { it.width }
        }
    }

    private fun ArrayList<Wallpaper>.sortByHeight() {
        if (isOrderAsc()) {
            sortBy { it.height }
        } else {
            sortByDescending { it.height }
        }
    }

    private fun isOrderAsc(): Boolean {
        return MainPreferences.getOrder() == ASC
    }
}