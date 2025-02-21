package app.simple.peri.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Tag
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.BottomHeader
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.dialogs.tags.TagsMenu
import app.simple.peri.ui.nav.Routes
import app.simple.peri.viewmodels.TagsViewModel
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun Tags(navController: NavController? = null) {
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory()
    )
    val tags = remember { mutableListOf<Tag>() }
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets
    ).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets
    ).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val navigationBarHeightPx = navigationBarHeight
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }
    var bottomHeaderHeight by remember { mutableStateOf(0.dp) }

    val topPadding = 8.dp + statusBarHeightDp
    val bottomPadding = 8.dp + if (MainComposePreferences.getBottomHeader()) {
        bottomHeaderHeight
    } else {
        navigationBarHeightDp
    }

    tagsViewModel.getTags().observeAsState().value?.let {
        tags.clear()
        tags.addAll(it)
    }

    Box {
        LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
                contentPadding = PaddingValues(
                        top = topPadding,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = bottomPadding
                ),
        ) {
            if (MainComposePreferences.getBottomHeader().not()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    TopHeader(
                            title = stringResource(R.string.tags),
                            modifier = Modifier.padding(COMMON_PADDING),
                            count = tags.size,
                            navController = navController
                    )
                }
            }
            if (tags.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                            text = stringResource(id = R.string.tags_summary),
                            modifier = Modifier.padding(COMMON_PADDING)
                    )
                }
            }
            items(tags.size) { index ->
                TagItem(
                        tag = tags[index],
                        navController = navController
                )
            }
        }

        if (MainComposePreferences.getBottomHeader()) {
            val density = LocalDensity.current

            BottomHeader(
                    title = stringResource(R.string.tags),
                    count = tags.size,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onGloballyPositioned {
                            bottomHeaderHeight = with(density) { it.size.height.toDp() }
                        },
                    navController = navController,
                    hazeState = hazeState,
                    navigationBarHeight = navigationBarHeightDp,
                    statusBarHeight = statusBarHeightDp
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun TagItem(tag: Tag, navController: NavController? = null) {
    var showDialog by remember { mutableStateOf(false) }
    val hazeState = remember { HazeState() }

    if (showDialog) {
        TagsMenu(
                setShowDialog = { showDialog = it },
                tag = tag
        )
    }

    ElevatedCard(
            elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp,
            ),
            modifier = Modifier
                .padding(8.dp)
                .combinedClickable(
                        onClick = {
                            navController?.navigate(Routes.TAGGED_WALLPAPERS) {
                                navController.currentBackStackEntry?.savedStateHandle?.set(Routes.TAG_ARG, tag)
                            }
                        },
                        onLongClick = {
                            showDialog = true
                        }),
    ) {
        Box {
            GlideImage(
                    model = app.simple.peri.glide.tags.ContextTag(tag, LocalContext.current),
                    contentDescription = null,
                    transition = CrossFade,
                    modifier = Modifier.haze(hazeState),
            )

            Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .hazeChild(
                                state = hazeState,
                                style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 5.dp)
                        )
                        .align(Alignment.BottomCenter)
            ) {
                Text(
                        text = tag.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = COMMON_PADDING, end = COMMON_PADDING, top = COMMON_PADDING)
                )

                Text(
                        text = stringResource(id = R.string.tag_count, tag.sum.count()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        modifier = Modifier.padding(start = COMMON_PADDING, end = COMMON_PADDING, bottom = COMMON_PADDING)
                )
            }
        }
    }
}
