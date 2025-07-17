package app.simple.peri.sources

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.simple.peri.interfaces.WallhavenApi
import app.simple.peri.models.WallhavenFilter
import app.simple.peri.models.WallhavenResponse
import app.simple.peri.models.WallhavenResponse.WallhavenItem
import app.simple.peri.models.WallhavenWallpaper

class WallhavenPagingSource(
        private val api: WallhavenApi,
        private val filter: WallhavenFilter,
        private val onMetaReceived: (WallhavenResponse.Meta) -> Unit = {}
) : PagingSource<Int, WallhavenWallpaper>() {
    override fun getRefreshKey(state: PagingState<Int, WallhavenWallpaper>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, WallhavenWallpaper> {
        val page = params.key ?: 1
        return try {
            val response = api.searchWallpapers(
                    filter.query,
                    filter.categories,
                    filter.purity,
                    filter.atleast,
                    filter.resolution,
                    filter.ratios,
                    filter.sorting,
                    filter.order,
                    page
            )

            val wallpapers = response.data.map { mapToWallpaper(it) }
            onMetaReceived(response.meta)

            LoadResult.Page(
                    data = wallpapers,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.data.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Log.e("WallhavenPagingSource", "Error loading page $page", e)
            LoadResult.Error(e)
        }
    }

    fun mapToWallpaper(item: WallhavenItem): WallhavenWallpaper {
        return WallhavenWallpaper(
                item.id,
                item.url,
                item.thumbs.getOrDefault("small", item.path),
                item.thumbs.getOrDefault("original", item.path),
                item.path,
                item.category,
                item.resolution,
                item.ratio,
                if (item.uploader != null) item.uploader.username else "Unknown",
                item.fileSize,
                item.colors,
        )
    }
}
