package app.simple.peri.interfaces

import app.simple.peri.models.WallhavenResponse
import app.simple.peri.models.WallpaperDetailsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WallhavenApi {
    @GET("search")
    suspend fun searchWallpapers(
            @Query("q") query: String?,
            @Query("categories") categories: String?,
            @Query("purity") purity: String?,
            @Query("atleast") atleast: String?,
            @Query("resolutions") resolution: String?,
            @Query("ratios") ratios: String?,
            @Query("sorting") sorting: String?,
            @Query("order") order: String?,
            @Query("page") page: Int
    ): WallhavenResponse

    @GET("w/{id}")
    suspend fun getWallpaperDetails(@Path("id") id: String): Response<WallpaperDetailsResponse>
}
