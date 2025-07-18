package app.simple.peri.models

import java.io.Serializable

data class WallhavenFilter(
        val query: String = "",
        val categories: String = "111",
        val purity: String = "100",
        val atleast: String = "1920x1080",
        val resolution: String = "1920x1080",
        val ratios: String = "16x9",
        val sorting: String = "date_added",
        val order: String = "desc"
) : Serializable
