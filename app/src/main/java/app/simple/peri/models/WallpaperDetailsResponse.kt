package app.simple.peri.models

import com.google.gson.annotations.SerializedName

data class WallpaperDetailsResponse(
        @SerializedName("data") val data: WallpaperData
)

data class WallpaperData(
        @SerializedName("tags") val tags: List<WallhavenTag>
)

data class WallhavenTag(
        @SerializedName("id") val id: Int,
        @SerializedName("name") val name: String
)