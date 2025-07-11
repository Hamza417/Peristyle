package app.simple.peri.application

import android.app.Application
import app.simple.peri.coil.fetchers.FolderFetcher
import app.simple.peri.coil.keyers.FolderKeyer
import app.simple.peri.utils.WallpaperSort.setSeed
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
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
                add(FolderKeyer())
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .build()
    }
}
