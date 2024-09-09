package app.simple.peri.compose.screens

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.simple.peri.R
import app.simple.peri.compose.nav.Routes
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.utils.FileUtils.toUri
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WallpaperScreen(context: Context, navController: NavHostController) {
    val wallpaper = navController.previousBackStackEntry?.savedStateHandle?.get<Wallpaper>(Routes.WALLPAPER_ARG)

    Box(
            modifier = Modifier.fillMaxSize(),
    ) {

        val currentScale = remember {
            mutableStateOf(ContentScale.Crop)
        }

        val hazeState = remember { HazeState() }

        GlideImage(
                model = wallpaper?.uri?.toUri(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
                alignment = Alignment.Center,
                contentScale = currentScale.value,
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


        Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(32.dp))
                    .hazeChild(
                            state = hazeState,
                            style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 15.dp)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                )
        ) {
            Text(
                    text = wallpaper?.name ?: "",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp),
                    fontSize = 22.sp
            )

            Text(
                    text = buildString {
                        append(wallpaper?.width ?: 0)
                        append(" x ")
                        append(wallpaper?.height ?: 0)
                        append(" ")
                        append(wallpaper?.size?.toSize() ?: "")
                    },
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Light,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp, start = 16.dp, top = 4.dp),
                    fontSize = 18.sp
            )

            Button(
                    onClick = {

                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                    )
            ) {
                Text(
                        text = context.getString(R.string.set_as_wallpaper),
                        color = Color.Black,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
        }
    }
}
