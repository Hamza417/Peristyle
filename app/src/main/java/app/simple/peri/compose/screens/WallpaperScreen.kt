package app.simple.peri.compose.screens

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import app.simple.peri.compose.nav.Routes
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toUri
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import me.saket.telephoto.zoomable.glide.ZoomableGlideImage

@Composable
fun WallpaperScreen(context: Context, navController: NavHostController) {
    val wallpaper = navController.previousBackStackEntry?.savedStateHandle?.get<Wallpaper>(Routes.WALLPAPER_ARG)

    Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
    ) {
        val currentScale = remember {
            mutableStateOf(ContentScale.Crop)
        }

        ZoomableGlideImage(
                model = wallpaper?.uri?.toUri(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = currentScale.value,
                onLongClick = {

                },
                onClick = {
                    // Set content scale to alternate between crop and fill
                    currentScale.value = if (currentScale.value == ContentScale.Crop) {
                        ContentScale.Inside
                    } else {
                        ContentScale.Crop
                    }
                }
        )
        {
            it.addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    return false
                }
            })
                .transition(withCrossFade())
                .disallowHardwareConfig()
                .fitCenter()
        }
    }
}
