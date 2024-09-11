package app.simple.peri.compose.screens

import android.app.Application
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Tag
import app.simple.peri.viewmodels.TagsViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@Composable
fun Tags(navController: NavController? = null) {
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(
                    application = requireNotNull(LocalContext.current.applicationContext as Application),
            )
    )
    val tags = remember { mutableListOf<Tag>() }
    var statusBarHeight by remember { mutableIntStateOf(0) }

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.statusBars()).top
    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val topPadding = 8.dp + statusBarHeightDp

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
                    bottom = 8.dp),
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            TopHeader(
                    title = stringResource(R.string.tags),
                    modifier = Modifier.padding(COMMON_PADDING),
                    count = tags.size)
        }
        items(tags.size) { index ->
            TagItem(tag = tags[index],
                    navController = navController)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun TagItem(tag: Tag, navController: NavController? = null) {
    ElevatedCard(
            elevation = CardDefaults.cardElevation(
                    defaultElevation = 12.dp,
            ),
            onClick = {
                navController?.navigate("${Routes.TAGGED_WALLPAPERS}/${tag.name}")
            },
            modifier = Modifier.padding(8.dp)
    ) {
        GlideImage(
                model = app.simple.peri.glide.tags.Tag(tag, LocalContext.current),
                contentDescription = null,
        )

        Text(
                text = tag.name,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp)
        )

        Text(
                text = stringResource(id = R.string.tag_count, tag.sum.count()),
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        )
    }
}
