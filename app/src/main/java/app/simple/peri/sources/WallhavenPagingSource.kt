package app.simple.peri.sources

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.simple.peri.interfaces.WallhavenApi
import app.simple.peri.models.WallhavenResponse.WallhavenItem
import app.simple.peri.models.WallhavenWallpaper

class WallhavenPagingSource(
        private val api: WallhavenApi,
        private val query: String
) : PagingSource<Int, WallhavenWallpaper>() {
    override fun getRefreshKey(state: PagingState<Int, WallhavenWallpaper>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, WallhavenWallpaper> {
        return try {
            val page = params.key ?: 1
            val response = api.searchWallpapers(query, page)
            val wallpapers = response.data.map { mapToWallpaper(it) }

            LoadResult.Page(
                    data = wallpapers,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (response.data.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    fun mapToWallpaper(item: WallhavenItem): WallhavenWallpaper {
        return WallhavenWallpaper(
                item.id,
                item.url,
                item.thumbs.getOrDefault("original", item.path),
                item.path,
                item.category,
                item.resolution,
                item.ratio,
                if (item.uploader != null) item.uploader.username else "Unknown",
                item.fileSize
        )
    }
}
