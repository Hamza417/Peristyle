package app.simple.peri.models

import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

data class LiveWallpaperInfo(
        val name: String,
        val icon: Drawable,
        val resolveInfo: ResolveInfo
)
