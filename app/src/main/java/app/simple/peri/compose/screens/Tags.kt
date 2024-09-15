package app.simple.peri.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.DisplayDimension
import app.simple.peri.models.Tag
import app.simple.peri.viewmodels.TagsViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

val displayDimension = DisplayDimension(1080, 1920)

@Composable
fun Tags(navController: NavController? = null) {
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory()
    )
    var tags = remember { mutableListOf<Tag>() }
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    displayDimension.width = LocalView.current.width
    displayDimension.height = LocalView.current.height

    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val navigationBarHeightPx = navigationBarHeight
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }
    val topPadding = 8.dp + statusBarHeightDp
    val bottomPadding = 8.dp + navigationBarHeightDp

    tagsViewModel.getTags().observeAsState().value?.let {
        tags.clear()
        tags.addAll(it)
    }

    LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                    top = topPadding,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomPadding),
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            TopHeader(
                    title = stringResource(R.string.tags),
                    modifier = Modifier.padding(COMMON_PADDING),
                    count = tags.size,
                    navController = navController)
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
            TagItem(tag = tags[index],
                    navController = navController) {
                tags = tags.filter { tag -> tag.name != it.name }.toMutableList()
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun TagItem(tag: Tag, navController: NavController? = null, onDelete: (Tag) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val hazeState = remember { HazeState() }

    if (showDialog) {
        TagsMenu(
                setShowDialog = { showDialog = it },
                tag = tag,
                onDelete = { tag ->
                    onDelete(tag)
                    showDialog = false
                }
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
                            navController?.navigate("${Routes.TAGGED_WALLPAPERS}/${tag.name}")
                        },
                        onLongClick = {
                            showDialog = true
                        }),
    ) {
        Box {
            GlideImage(
                    model = app.simple.peri.glide.tags.Tag(tag, LocalContext.current),
                    contentDescription = null,
                    modifier = Modifier.haze(hazeState),
            )

            Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .hazeChild(
                                state = hazeState,
                                style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 5.dp))
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

@Composable
fun TagsMenu(setShowDialog: (Boolean) -> Unit,
             tag: Tag,
             onDelete: (Tag) -> Unit) {

    val context = LocalContext.current
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory()
    )

    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
        ) {
            Box(
                    contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = tag.name,
                                style = TextStyle(
                                        fontSize = 24.sp,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Bold
                                )
                        )
                        Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "",
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(30.dp)
                                    .clickable { setShowDialog(false) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                            onClick = {
                                tagsViewModel.deleteTag(tag)
                            },
                            colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                                text = context.getString(R.string.delete),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
