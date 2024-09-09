package app.simple.peri.compose.screens

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.viewmodels.HomeScreenViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlin.math.absoluteValue

@Composable
fun HomeScreen(context: Context, navController: NavController? = null) {
    val pagerState = rememberPagerState(pageCount = {
        2
    })

    val fling = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(10)
    )

    val homeScreenViewModel: HomeScreenViewModel = viewModel()
    val systemWallpapers: ArrayList<Wallpaper>
            by homeScreenViewModel.getSystemWallpaper().observeAsState(initial = arrayListOf())

    Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
    ) {
        Column {
            Header(
                    title = context.getString(R.string.app_name),
                    modifier = Modifier.padding(24.dp),
                    navController = navController
            )

            HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 32.dp),
                    flingBehavior = fling,
                    modifier = Modifier
                        .fillMaxSize()
            ) { page ->
                val wallpaper = systemWallpapers.getOrNull(page)

                CardItem(
                        title = if (page == 0) context.getString(R.string.lock_screen) else context.getString(R.string.home_screen),
                        onClick = {
                            if (page == 0) {
                                navController?.navigate("wallpaper")
                            } else {
                                navController?.navigate("settings")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer {
                                // Calculate the absolute offset for the current page from the
                                // scroll position. We use the absolute value which allows us to mirror
                                // any effects for both directions
                                val pageOffset = (
                                        (pagerState.currentPage - page) + pagerState
                                            .currentPageOffsetFraction
                                        ).absoluteValue

                                // We animate the alpha, between 50% and 100%
                                alpha = lerp(
                                        start = 0.5f,
                                        stop = 1f,
                                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                            .padding(8.dp), // Add padding to create space between the cards
                        wallpaper = wallpaper

                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CardItem(title: String, onClick: () -> Unit, modifier: Modifier = Modifier, wallpaper: Wallpaper?) {
    val currentScale = remember {
        mutableStateOf(ContentScale.Crop)
    }

    ElevatedCard(
            elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
            ),
            modifier = modifier
                .fillMaxHeight()
                .padding(8.dp), // margin
            onClick = onClick,
            shape = RoundedCornerShape(32.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GlideImage(
                    model = wallpaper?.uri?.toUri(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    alignment = Alignment.Center,
                    contentScale = currentScale.value,
            ) {
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

            Text(
                    text = title,
                    modifier = Modifier
                        .align(Alignment.Center) // Align the text to the center of the Box
                        .padding(16.dp)
                        .background(Color(0x80000000)),
                    textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun Header(title: String, modifier: Modifier = Modifier, navController: NavController? = null) {
    Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 32.sp, // Set the font size
                modifier = Modifier.weight(1f), // Set the weight
                fontWeight = FontWeight.Bold, // Make the text bold
        )

        IconButton(
                onClick = {
                    navController?.navigate("settings")
                },
        ) {
            Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null
            )
        }
    }
}
