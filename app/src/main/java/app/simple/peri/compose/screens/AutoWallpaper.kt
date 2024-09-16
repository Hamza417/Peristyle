package app.simple.peri.compose.screens

import ClickablePreference
import DescriptionPreference
import SecondaryClickablePreference
import SecondaryHeader
import SwitchPreference
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.models.Tag
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.viewmodels.TagsViewModel

@Composable
fun AutoWallpaper(navController: NavController? = null) {
    val context = LocalContext.current
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val navigationBarHeightPx = navigationBarHeight
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }

    val topPadding = 8.dp + statusBarHeightDp
    val bottomPadding = 8.dp + navigationBarHeightDp

    LazyColumn(
            contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = topPadding,
                    bottom = bottomPadding)
    ) {
        item { // Header
            TopHeader(title = stringResource(R.string.auto_wallpaper),
                      modifier = Modifier.padding(COMMON_PADDING),
                      navController = navController)

            DescriptionPreference(stringResource(R.string.auto_wallpaper_summary))
        }
        item {
            val screenSelectionDialog = remember { mutableStateOf(false) }
            val autoWallpaperDialog = remember { mutableStateOf(false) }

            ClickablePreference(
                    title = context.getString(R.string.duration),
                    onClick = {
                        autoWallpaperDialog.value = true
                    }
            )

            ClickablePreference(
                    title = context.getString(R.string.auto_wallpaper_set_for),
                    description = context.getString(R.string.auto_wallpaper_set_for_summary),
                    onClick = {
                        screenSelectionDialog.value = true
                    }
            )

            if (screenSelectionDialog.value) {
                ScreenSelectionDialog(
                        onDismiss = { screenSelectionDialog.value = false },
                        onOptionSelected = {
                            MainPreferences.setWallpaperSetFor(it)
                        }
                )
            }

            if (autoWallpaperDialog.value) {
                TimeSelectionDialog(
                        onDismiss = { autoWallpaperDialog.value = false },
                        onOptionSelected = {
                            MainPreferences.setAutoWallpaperInterval(it.second.toString())
                        }
                )
            }
        }
        item {
            val isLockScreenRow = remember { mutableStateOf(MainComposePreferences.getIsLockSourceSet()) }
            val isHomeScreenRow = remember { mutableStateOf(MainComposePreferences.getIsHomeSourceSet()) }
            val lockTagID = remember { mutableStateOf(MainComposePreferences.getLockTagId()) }
            val homeTagID = remember { mutableStateOf(MainComposePreferences.getHomeTagId()) }
            val lockFolderID = remember { mutableStateOf(MainComposePreferences.getLockFolderId()) }
            val homeFolderID = remember { mutableStateOf(MainComposePreferences.getHomeFolderId()) }

            SecondaryHeader(title = context.getString(R.string.source))

            DescriptionPreference(stringResource(R.string.source_summary))

            SwitchPreference(
                    title = stringResource(R.string.lock_screen),
                    description = stringResource(R.string.different_wallpaper_for_lock_screen),
                    checked = MainComposePreferences.getIsLockSourceSet(),
                    topPadding = 4.dp
            ) {
                MainComposePreferences.setIsLockSourceSet(it)
                isLockScreenRow.value = it
            }

            if (isLockScreenRow.value) {
                Column(
                        modifier = Modifier.wrapContentHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.FiberManualRecord,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 8.dp, start = 16.dp)
                                    .height(12.dp)
                                    .align(Alignment.CenterVertically)
                        )
                        SecondaryClickablePreference(
                                title = context.getString(R.string.tags),
                                onClick = {

                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                        )
                        Text(
                                text = lockTagID.value ?: stringResource(R.string.unknown),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .align(Alignment.CenterVertically)
                        )
                    }

                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.FiberManualRecord,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 8.dp, start = 16.dp)
                                    .height(12.dp)
                                    .align(Alignment.CenterVertically)
                        )
                        SecondaryClickablePreference(
                                title = context.getString(R.string.folder),
                                onClick = {

                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                        )
                        Text(
                                text = lockFolderID.value ?: stringResource(R.string.unknown),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            SwitchPreference(
                    title = stringResource(R.string.home_screen),
                    description = stringResource(R.string.different_wallpaper_for_home_screen),
                    checked = MainComposePreferences.getIsHomeSourceSet()
            ) {
                MainComposePreferences.setIsHomeSourceSet(it)
                isHomeScreenRow.value = it
            }

            if (isHomeScreenRow.value) {
                Column(
                        modifier = Modifier.wrapContentHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.FiberManualRecord,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 8.dp, start = 16.dp)
                                    .height(12.dp)
                                    .align(Alignment.CenterVertically)
                        )
                        SecondaryClickablePreference(
                                title = context.getString(R.string.tags),
                                onClick = {

                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                        )
                        Text(
                                text = homeTagID.value ?: stringResource(R.string.unknown),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .align(Alignment.CenterVertically)
                        )
                    }

                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.FiberManualRecord,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 8.dp, start = 16.dp)
                                    .height(12.dp)
                                    .align(Alignment.CenterVertically)
                        )
                        SecondaryClickablePreference(
                                title = context.getString(R.string.folder),
                                onClick = {

                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                        )
                        Text(
                                text = homeFolderID.value ?: stringResource(R.string.unknown),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
        item {
            SecondaryHeader(title = context.getString(R.string.settings))

            SwitchPreference(
                    title = context.getString(R.string.crop_wallpaper),
                    description = context.getString(R.string.crop_wallpaper_summary),
                    checked = MainPreferences.getCropWallpaper(),
                    onCheckedChange = {
                        MainPreferences.setCropWallpaper(it)
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.linear_auto_wallpaper),
                    checked = MainPreferences.isLinearAutoWallpaper()
            ) {
                MainPreferences.setLinearAutoWallpaper(it)
            }
        }
        item {

        }
    }
}

@Composable
fun ScreenSelectionDialog(onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {
    val options = listOf(
            stringResource(R.string.lock_screen) to MainPreferences.LOCK,
            stringResource(R.string.home_screen) to MainPreferences.HOME,
            stringResource(R.string.both) to MainPreferences.BOTH
    )

    val storedOption = MainPreferences.getWallpaperSetFor()
    val selectedOption = remember { mutableStateOf(options.find { it.second == storedOption }) }

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(R.string.auto_wallpaper_set_for_summary)) },
            text = {
                Column {
                    options.forEach { option ->
                        Button(
                                onClick = {
                                    selectedOption.value = option
                                    onOptionSelected(option.second)
                                    onDismiss()
                                },
                                colors = when (selectedOption.value) {
                                    option -> {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    }

                                    else -> {
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                    text = option.first,
                                    color = when (selectedOption.value) {
                                        option -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}

@Composable
fun TimeSelectionDialog(onDismiss: () -> Unit, onOptionSelected: (Pair<String, Long>) -> Unit) {
    val options = listOf(
            stringResource(R.string.off) to 0L,
            stringResource(R.string.every_minute) to 60_000L,
            stringResource(R.string.every_5_minutes) to 300_000L,
            stringResource(R.string.every_15_minutes) to 900_000L,
            stringResource(R.string.every_30_minutes) to 1_800_000L,
            stringResource(R.string.every_1_hour) to 3_600_000L,
            stringResource(R.string.every_3_hours) to 10_800_000L,
            stringResource(R.string.every_6_hours) to 21_600_000L,
            stringResource(R.string.every_12_hours) to 43_200_000L,
            stringResource(R.string.every_24_hours) to 86_400_000L,
            stringResource(R.string.every_3_days) to 259_200_000L,
            stringResource(R.string.every_7_days) to 604_800_000L
    )

    val storedInterval = MainPreferences.getAutoWallpaperInterval().toLong()
    val selectedOption = remember { mutableStateOf(options.find { it.second == storedInterval }) }

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(R.string.auto_wallpaper)) },
            text = {
                Column {
                    options.forEach { option ->
                        Button(
                                onClick = {
                                    selectedOption.value = option
                                    onOptionSelected(option)
                                    onDismiss()
                                },
                                colors = when (selectedOption.value) {
                                    option -> {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    }

                                    else -> {
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                    text = option.first,
                                    color = when (selectedOption.value) {
                                        option -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}

@Composable
fun TagsDialog(onTag: (Tag) -> Unit) {
    val tagsViewModel: TagsViewModel = viewModel()
    val tags = remember { mutableStateOf(emptyList<Tag>()) }

    tagsViewModel.getTags().observeAsState().value?.let {
        tags.value = it
    }

    AlertDialog(
            onDismissRequest = { },
            title = { Text(text = stringResource(R.string.tags)) },
            text = {
                Column {
                    tags.value.forEach { tag ->
                        Button(
                                onClick = {
                                    onTag(tag)
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                    text = tag.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
