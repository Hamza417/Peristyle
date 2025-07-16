package app.simple.peri.interfaces

import app.simple.peri.models.WallhavenResponse
import app.simple.peri.preferences.WallHavenPreferences
import retrofit2.http.GET
import retrofit2.http.Query

interface WallhavenApi {

    @GET("search")
    suspend fun searchWallpapers(
            @Query("q") query: String,
            @Query("page") page: Int,
            @Query("apikey") apiKey: String? = WallHavenPreferences.getAPIKey()
    ): WallhavenResponse
}
