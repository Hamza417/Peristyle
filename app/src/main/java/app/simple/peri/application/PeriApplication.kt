package app.simple.peri.application

import android.app.Application
import app.simple.peri.coil.fetchers.FolderFetcher
import app.simple.peri.coil.keyers.ContextFolderKeyer
import app.simple.peri.utils.WallpaperSort.setSeed
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.google.android.material.color.DynamicColors

class PeriApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        setSeed(System.currentTimeMillis())
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(FolderFetcher())
                add(ContextFolderKeyer())
            }
            .build()
    }
}
