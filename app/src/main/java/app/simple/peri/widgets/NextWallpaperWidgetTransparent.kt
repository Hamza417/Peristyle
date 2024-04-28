package app.simple.peri.widgets

import app.simple.peri.R

class NextWallpaperWidgetTransparent : NextWallpaper() {
    override fun getLayoutID(): Int {
        return R.layout.widget_next_wallpaper_transparent
    }

    override fun getButtonID(): Int {
        return R.id.widget_next_wallpaper
    }
}
