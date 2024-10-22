package app.simple.peri.repository

import app.simple.peri.database.dao.WallpaperDao
import app.simple.peri.models.Wallpaper
import kotlinx.coroutines.flow.Flow

class WallpaperRepository(private val wallpaperDao: WallpaperDao) {
    fun getAllWallpapers(): Flow<List<Wallpaper>> = wallpaperDao.getWallpapersFlow()
}
