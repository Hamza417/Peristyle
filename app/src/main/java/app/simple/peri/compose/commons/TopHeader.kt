package app.simple.peri.compose.commons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.nav.Routes
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

val COMMON_PADDING = 16.dp

@Composable
fun TopHeader(title: String,
              modifier: Modifier = Modifier,
              count: Int = 0,
              navController: NavController? = null,
              isSettings: Boolean = false) {
    Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 32.sp, // Set the font size
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp), // Set the weight
                fontWeight = FontWeight.Bold, // Make the text bold
                lineHeight = 36.sp, // Set the line height
                maxLines = 1, // Set the max lines
                overflow = TextOverflow.Ellipsis, // Set the overflow
        )

        if (count > 0) {
            Text(
                    text = count.toString(),
                    textAlign = TextAlign.End,
                    fontSize = 24.sp,
                    modifier = Modifier.wrapContentWidth(),
                    fontWeight = FontWeight.Thin,
            )
        }

        if (isSettings.not()) {
            IconButton(
                    onClick = {
                        navController?.navigate(Routes.SETTINGS)
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                )
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BottomHeader(title: String,
                 modifier: Modifier = Modifier,
                 count: Int = 0,
                 navController: NavController? = null,
                 isSettings: Boolean = false,
                 hazeState: HazeState,
                 navigationBarHeight: Dp,
                 statusBarHeight: Dp) {

    val navHeight = if (navigationBarHeight == 0.dp) {
        COMMON_PADDING
    } else {
        navigationBarHeight
    }

    Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(elevation = 24.dp,
                        spotColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ambientColor = MaterialTheme.colorScheme.surfaceVariant)
                .hazeChild(
                        state = hazeState,
                        style = HazeMaterials.ultraThin()
                ),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 32.sp, // Set the font size
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp, bottom = navHeight, start = COMMON_PADDING, top = COMMON_PADDING), // Set the weight
                fontWeight = FontWeight.Bold, // Make the text bold
                lineHeight = 36.sp, // Set the line height
                maxLines = 1, // Set the max lines
                overflow = TextOverflow.Ellipsis, // Set the overflow
        )

        if (count > 0) {
            Text(
                    text = count.toString(),
                    textAlign = TextAlign.End,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(end = 8.dp, bottom = navHeight, top = COMMON_PADDING),
                    fontWeight = FontWeight.Thin,
            )
        }

        if (isSettings.not()) {
            IconButton(
                    onClick = {
                        navController?.navigate(Routes.SETTINGS)
                    },
                    modifier = Modifier.padding(end = COMMON_PADDING, bottom = navHeight, top = COMMON_PADDING),
            ) {
                Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                )
            }
        }
    }
}
