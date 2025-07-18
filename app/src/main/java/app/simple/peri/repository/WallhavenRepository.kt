package app.simple.peri.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.simple.peri.interfaces.WallhavenApi
import app.simple.peri.models.WallhavenFilter
import app.simple.peri.models.WallhavenResponse
import app.simple.peri.models.WallhavenTag
import app.simple.peri.models.WallhavenWallpaper
import app.simple.peri.sources.WallhavenPagingSource
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class WallhavenRepository @Inject constructor(
        private val api: WallhavenApi
) {

    fun getWallpapers(
            filter: WallhavenFilter,
            onMetaReceived: (WallhavenResponse.Meta) -> Unit
    ): Flow<PagingData<WallhavenWallpaper>> {
        return Pager(
                config = PagingConfig(pageSize = 24),
                pagingSourceFactory = {
                    WallhavenPagingSource(api, filter, onMetaReceived)
                }
        ).flow
    }

    suspend fun getWallpaperTags(id: String): List<WallhavenTag> {
        return try {
            val response = api.getWallpaperDetails(id)
            if (response.isSuccessful) {
                response.body()?.data?.tags ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
