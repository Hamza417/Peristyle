package app.simple.peri.ui.commons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.ui.dialogs.autowallpaper.AutoWallpaperPageSelectionDialog
import app.simple.peri.ui.nav.Routes
import com.kyant.liquidglass.GlassStyle
import com.kyant.liquidglass.LiquidGlassProviderState
import com.kyant.liquidglass.liquidGlass
import com.kyant.liquidglass.refraction.InnerRefraction
import com.kyant.liquidglass.refraction.RefractionAmount
import com.kyant.liquidglass.refraction.RefractionHeight
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

val COMMON_PADDING = 16.dp

@Composable
fun TopHeader(title: String,
              modifier: Modifier = Modifier,
              count: Int = 0,
              navController: NavController? = null,
              isSettings: Boolean = false,
              isSearch: Boolean = false,
              isAutoWallpaper: Boolean = false,
              onSearch: (() -> Unit)? = null) {

    val context = LocalContext.current
    val autoWallpaperScreenSelection = remember { mutableStateOf(false) }

    if (autoWallpaperScreenSelection.value) {
        AutoWallpaperPageSelectionDialog(
                onDismiss = {
                    autoWallpaperScreenSelection.value = false
                },
                onOptionSelected = { option ->
                    when (option) {
                        context.getString(R.string.wallpaper_manager) -> {
                            navController?.navigate(Routes.AUTO_WALLPAPER)
                        }

                        context.getString(R.string.live_auto_wallpaper) -> {
                            navController?.navigate(Routes.LIVE_AUTO_WALLPAPER)
                        }
                    }
                }
        )
    }

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
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(end = 8.dp),
                    fontWeight = FontWeight.Thin,
            )
        }

        if (isAutoWallpaper.not()) {
            IconButton(
                    onClick = {
                        autoWallpaperScreenSelection.value = true
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null
                )
            }
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

        if (isSearch) {
            IconButton(
                    onClick = {
                        onSearch?.invoke()
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.Search,
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
                 isSearch: Boolean = false,
                 isAutoWallpaper: Boolean = false,
                 onSearch: (() -> Unit)? = null,
                 navigationBarHeight: Dp,
                 providerState: LiquidGlassProviderState? = null
) {

    val context = LocalContext.current
    val autoWallpaperScreenSelection = remember { mutableStateOf(false) }

    if (autoWallpaperScreenSelection.value) {
        AutoWallpaperPageSelectionDialog(
                onDismiss = {
                    autoWallpaperScreenSelection.value = false
                },
                onOptionSelected = { option ->
                    when (option) {
                        context.getString(R.string.wallpaper_manager) -> {
                            navController?.navigate(Routes.AUTO_WALLPAPER)
                        }

                        context.getString(R.string.live_auto_wallpaper) -> {
                            navController?.navigate(Routes.LIVE_AUTO_WALLPAPER)
                        }
                    }
                }
        )
    }

    val navHeight = if (navigationBarHeight == 0.dp) {
        COMMON_PADDING
    } else {
        navigationBarHeight
    }

    Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(elevation = 24.dp,
                        spotColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ambientColor = MaterialTheme.colorScheme.surfaceVariant)
                .liquidGlass(
                        providerState!!,
                        GlassStyle(
                                shape = RoundedCornerShape(0.dp),
                                innerRefraction = InnerRefraction(
                                        height = RefractionHeight(8.dp),
                                        amount = RefractionAmount((-50).dp)
                                ),
                                material = com.kyant.liquidglass.material.GlassMaterial(
                                        blurRadius = 8.dp,
                                        brush = SolidColor(Color.Transparent),
                                        alpha = 0.3f
                                )
                        )
                ),
    ) {
        HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
        )

        Row(
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

            if (isAutoWallpaper.not()) {
                IconButton(
                        onClick = {
                            autoWallpaperScreenSelection.value = true
                        },
                        modifier = Modifier.padding(end = 4.dp, bottom = navHeight, top = COMMON_PADDING),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null
                    )
                }
            }

            if (isSettings.not()) {
                IconButton(
                        onClick = {
                            navController?.navigate(Routes.SETTINGS)
                        },
                        modifier = Modifier.padding(
                                end = if (isSearch) 4.dp else COMMON_PADDING,
                                bottom = navHeight,
                                top = COMMON_PADDING),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(id = R.string.settings),
                    )
                }
            }

            if (isSearch) {
                IconButton(
                        onClick = {
                            onSearch?.invoke()
                        },
                        modifier = Modifier.padding(end = COMMON_PADDING, bottom = navHeight, top = COMMON_PADDING),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(id = R.string.settings),
                    )
                }
            }
        }
    }
}
