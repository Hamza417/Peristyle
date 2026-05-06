package app.simple.peri.ui.screens

import android.util.Log
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
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Tag
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.AnchoredHeader
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.isScrollingUp
import app.simple.peri.ui.dialogs.tags.TagsMenu
import app.simple.peri.ui.nav.Routes
import app.simple.peri.ui.theme.LocalBarsSize
import app.simple.peri.viewmodels.TagsViewModel
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@Composable
fun Tags(navController: NavController? = null) {
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory()
    )
    val tags = remember { mutableStateListOf<Tag>() }
    val hazeState = remember { HazeState() }
    val gridState = rememberLazyStaggeredGridState()
    val isScrollingUp = gridState.isScrollingUp()
    var headerHeight by remember { mutableStateOf(0.dp) }

    val topPadding = 8.dp + LocalBarsSize.current.statusBarHeight
    val bottomPadding = 8.dp + LocalBarsSize.current.navigationBarHeight

    val contentTopPadding = if (!MainComposePreferences.getBottomHeader()) topPadding + headerHeight else topPadding
    val contentBottomPadding = if (MainComposePreferences.getBottomHeader()) bottomPadding + headerHeight else bottomPadding

    tagsViewModel.getTags().observeAsState().value?.let {
        Log.d("Tags", "Received tags: ${it.size}")
        tags.clear()
        tags.addAll(it)
    }

    Box {
        LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(
                        top = contentTopPadding,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = contentBottomPadding
                ),
        ) {
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

        val density = LocalDensity.current
        AnchoredHeader(
                title = stringResource(R.string.tags),
                count = tags.size,
                modifier = Modifier
                    .align(if (MainComposePreferences.getBottomHeader()) Alignment.BottomCenter else Alignment.TopCenter)
                    .onGloballyPositioned {
                        headerHeight = with(density) { it.size.height.toDp() }
                    },
                navController = navController,
                hazeState = hazeState,
                statusBarHeight = LocalBarsSize.current.statusBarHeight,
                navigationBarHeight = LocalBarsSize.current.navigationBarHeight,
                isVisible = isScrollingUp,
        )
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
                    modifier = Modifier.hazeSource(hazeState),
            )

            Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .hazeEffect(state = hazeState,
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
