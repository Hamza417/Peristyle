package app.simple.peri.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import app.simple.peri.models.WallhavenWallpaper
import app.simple.peri.viewmodels.WallhavenViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun WallhavenScreen(navController: NavController? = null) {
    val viewModel: WallhavenViewModel = hiltViewModel()
    val wallpapers = viewModel.wallpapers.collectAsLazyPagingItems()

    LazyColumn {
        items(wallpapers.itemCount) { index ->
            wallpapers[index]?.let { wallpaper ->
                ImageCard(wallpaper)
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageCard(wallpaper: WallhavenWallpaper) {
    Card {
        GlideImage(
                model = wallpaper.thumbnailUrl,
                contentDescription = wallpaper.uploader,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth())
    }
}