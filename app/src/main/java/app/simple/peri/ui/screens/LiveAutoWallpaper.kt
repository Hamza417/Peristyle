package app.simple.peri.ui.screens

import ButtonPreference
import ClickablePreference
import DescriptionPreference
import SecondaryClickablePreference
import SecondaryHeader
import SwitchPreference
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.services.AutoWallpaperService
import app.simple.peri.services.LiveAutoWallpaperService
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.dialogs.autowallpaper.ColorSpaceSelectionDialog
import app.simple.peri.ui.dialogs.autowallpaper.FoldersDialog
import app.simple.peri.ui.dialogs.autowallpaper.TagsDialog
import app.simple.peri.ui.dialogs.autowallpaper.TimeSelectionDialog
import app.simple.peri.ui.dialogs.autowallpaper.UsageTimesDeleteDialog
import app.simple.peri.ui.dialogs.wallpaper.AutoWallpaperEffectsDialog
import app.simple.peri.ui.settings.SkipColumn

@Composable
fun LiveAutoWallpaper(navController: NavController? = null) {
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
                      isAutoWallpaper = true,
                      navController = navController)

            DescriptionPreference(stringResource(R.string.auto_wallpaper_summary))
        }
        item {
            val autoWallpaperDialog = remember { mutableStateOf(false) }

            ButtonPreference(
                    title = stringResource(R.string.next_wallpaper),
                    onClick = {
                        Intent(context, AutoWallpaperService::class.java).also { intent ->
                            intent.action = AutoWallpaperService.ACTION_NEXT_WALLPAPER
                            context.startService(intent)
                        }
                    })

            ButtonPreference(
                    title = stringResource(R.string.set_as_wallpaper),
                    onClick = {
                        runCatching {
                            val componentName = ComponentName(
                                    context,
                                    LiveAutoWallpaperService::class.java
                            )
                            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }.onFailure {
                            it.printStackTrace()
                            Toast
                                .makeText(
                                        context,
                                        it.message ?: it.localizedMessage ?: it.stackTraceToString(),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    })

            ClickablePreference(
                    title = context.getString(R.string.interval),
                    description = context.getString(R.string.duration_summary),
                    onClick = {
                        autoWallpaperDialog.value = true
                    }
            )

            DescriptionPreference(stringResource(R.string.live_wallpaper_waring))

            SwitchPreference(
                    title = context.getString(R.string.change_when_screen_on),
                    description = context.getString(R.string.change_when_screen_on_summary),
                    checked = MainComposePreferences.getChangeWhenOn(),
                    topPadding = 4.dp,
                    onCheckedChange = {
                        MainComposePreferences.setChangeWhenOn(it)
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.change_when_screen_off),
                    description = context.getString(R.string.change_when_screen_off_summary),
                    checked = MainComposePreferences.getChangeWhenOff(),
                    onCheckedChange = {
                        MainComposePreferences.setChangeWhenOff(it)
                    }
            )

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
            val isHomeScreenRow = remember { mutableStateOf(MainComposePreferences.isHomeSourceSet()) }
            val homeTagID = remember { mutableStateOf(MainComposePreferences.getHomeTagId()) }
            val homeFolderID = remember { mutableIntStateOf(MainComposePreferences.getHomeFolderId()) }
            val homeFolderName = remember { mutableStateOf(MainComposePreferences.getHomeFolderName()) }
            val showHomeTagDialog = remember { mutableStateOf(false) }
            val showHomeFolderDialog = remember { mutableStateOf(false) }

            SecondaryHeader(title = context.getString(R.string.source))

            DescriptionPreference(stringResource(R.string.source_summary))

            SwitchPreference(
                    title = stringResource(R.string.home_screen),
                    description = stringResource(R.string.different_wallpaper_for_home_screen),
                    checked = MainComposePreferences.isHomeSourceSet()
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
                                tint = if (homeTagID.value != null) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp, start = 16.dp)
                                    .height(12.dp)
                                    .align(Alignment.CenterVertically)
                        )
                        SecondaryClickablePreference(
                                title = context.getString(R.string.tags),
                                onClick = {
                                    showHomeTagDialog.value = true
                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                        )
                        Text(
                                text = homeTagID.value ?: stringResource(R.string.not_set),
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
                                tint = if (homeFolderName.value != null) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    Color.Transparent
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp, start = 16.dp)
                                    .height(12.dp)
                                    .align(Alignment.CenterVertically)
                        )
                        SecondaryClickablePreference(
                                title = context.getString(R.string.folder),
                                onClick = {
                                    showHomeFolderDialog.value = true
                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                        )
                        Text(
                                text = homeFolderName.value ?: stringResource(R.string.not_set),
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

            if (showHomeTagDialog.value) {
                TagsDialog(
                        selected = homeTagID.value ?: "",
                        setShowing = { showHomeTagDialog.value = it },
                        onTag = {
                            homeTagID.value = it.name
                            showHomeTagDialog.value = false
                            MainComposePreferences.setHomeTagId(it.name)

                            homeFolderName.value = null
                            homeFolderID.intValue = 0
                            MainComposePreferences.setHomeFolderId(0)
                            MainComposePreferences.setHomeFolderName(null)
                            MainComposePreferences.setLastHomeWallpaperPosition(0)
                        })
            }

            if (showHomeFolderDialog.value) {
                FoldersDialog(
                        selected = homeFolderID.intValue,
                        setShowing = { showHomeFolderDialog.value = it },
                        onFolder = {
                            homeFolderID.intValue = it.hashcode
                            homeFolderName.value = it.name
                            showHomeFolderDialog.value = false
                            MainComposePreferences.setHomeFolderId(it.hashcode)
                            MainComposePreferences.setHomeFolderName(it.name)

                            homeTagID.value = null
                            MainComposePreferences.setHomeTagId(null)
                            MainComposePreferences.resetLastWallpaperPositions()
                        })
            }
        }
        item {
            val showEffectsDialog = remember { mutableStateOf(false) }
            val showDeleteDialog = remember { mutableStateOf(false) }
            val showColorSpaceDialog = remember { mutableStateOf(false) }

            SecondaryHeader(title = context.getString(R.string.settings))

            ClickablePreference(
                    title = context.getString(R.string.default_effects),
                    description = context.getString(R.string.default_effects_summary),
                    onClick = {
                        showEffectsDialog.value = true
                    }
            )

            ClickablePreference(
                    title = context.getString(R.string.color_space),
                    description = context.getString(R.string.color_space_summary),
                    onClick = {
                        showColorSpaceDialog.value = true
                    }
            )

            ClickablePreference(
                    title = context.getString(R.string.delete_after),
                    description = context.getString(R.string.delete_summary),
                    onClick = {
                        showDeleteDialog.value = true
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.linear_auto_wallpaper),
                    description = context.getString(R.string.linear_auto_wallpaper_summary),
                    checked = MainPreferences.isLinearAutoWallpaper()
            ) {
                MainPreferences.setLinearAutoWallpaper(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.notifications),
                    description = context.getString(R.string.notifications_summary),
                    checked = MainComposePreferences.getAutoWallpaperNotification()
            ) {
                MainComposePreferences.setAutoWallpaperNotification(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.double_tap_to_change_wallpaper),
                    description = context.getString(R.string.double_tap_to_change_wallpaper_summary),
                    checked = MainComposePreferences.getDoubleTapToChange()
            ) {
                MainComposePreferences.setDoubleTapToChange(it)
            }

            if (showDeleteDialog.value) {
                UsageTimesDeleteDialog {
                    showDeleteDialog.value = false
                }
            }

            if (showColorSpaceDialog.value) {
                ColorSpaceSelectionDialog {
                    showColorSpaceDialog.value = false
                }
            }

            if (showEffectsDialog.value) {
                AutoWallpaperEffectsDialog(
                        setShowDialog = { showEffectsDialog.value = it },
                        initialBlurValue = MainComposePreferences.getAutoWallpaperBlur(),
                        initialBrightnessValue = MainComposePreferences.getAutoWallpaperBrightness(),
                        initialContrastValue = MainComposePreferences.getAutoWallpaperContrast(),
                        initialSaturationValue = MainComposePreferences.getAutoWallpaperSaturation(),
                        initialHueRedValue = MainComposePreferences.getAutoWallpaperHueRed(),
                        initialHueGreenValue = MainComposePreferences.getAutoWallpaperHueGreen(),
                        initialHueBlueValue = MainComposePreferences.getAutoWallpaperHueBlue(),
                        initialScaleRedValue = MainComposePreferences.getAutoWallpaperScaleRed(),
                        initialScaleGreenValue = MainComposePreferences.getAutoWallpaperScaleGreen(),
                        initialScaleBlueValue = MainComposePreferences.getAutoWallpaperScaleBlue(),
                        onApplyEffects = { blur, brightness, contrast, saturation, hueRed, hueGreen, hueBlue, scaleRed, scaleGreen, scaleBlue ->
                            MainComposePreferences.setAutoWallpaperBlur(blur)
                            MainComposePreferences.setAutoWallpaperBrightness(brightness)
                            MainComposePreferences.setAutoWallpaperContrast(contrast)
                            MainComposePreferences.setAutoWallpaperSaturation(saturation)
                            MainComposePreferences.setAutoWallpaperHueRed(hueRed)
                            MainComposePreferences.setAutoWallpaperHueGreen(hueGreen)
                            MainComposePreferences.setAutoWallpaperHueBlue(hueBlue)
                            MainComposePreferences.setAutoWallpaperScaleRed(scaleRed)
                            MainComposePreferences.setAutoWallpaperScaleGreen(scaleGreen)
                            MainComposePreferences.setAutoWallpaperScaleBlue(scaleBlue)
                        }
                )
            }
        }
        item {
            SkipColumn()
        }
    }
}
