package app.simple.peri.ui.screens

import ButtonPreference
import ClickablePreference
import DescriptionPreference
import SecondaryClickablePreference
import SecondaryHeader
import SwitchPreference
import android.content.Intent
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
import androidx.compose.material.icons.rounded.Edit
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
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.dialogs.autowallpaper.ColorSpaceSelectionDialog
import app.simple.peri.ui.dialogs.autowallpaper.FoldersDialog
import app.simple.peri.ui.dialogs.autowallpaper.ScreenSelectionDialog
import app.simple.peri.ui.dialogs.autowallpaper.TagsDialog
import app.simple.peri.ui.dialogs.autowallpaper.TimeSelectionDialog
import app.simple.peri.ui.dialogs.autowallpaper.UsageTimesDeleteDialog
import app.simple.peri.ui.dialogs.autowallpaper.WallpaperMethodDialog
import app.simple.peri.ui.dialogs.wallpaper.AutoWallpaperEffectsDialog
import app.simple.peri.ui.settings.SkipColumn

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
                      isAutoWallpaper = true,
                      navController = navController)

            DescriptionPreference(stringResource(R.string.auto_wallpaper_summary))
        }
        item {
            val screenSelectionDialog = remember { mutableStateOf(false) }
            val autoWallpaperDialog = remember { mutableStateOf(false) }

            ButtonPreference(
                    title = stringResource(R.string.next_wallpaper),
                    onClick = {
                        Intent(context, AutoWallpaperService::class.java).also { intent ->
                            intent.action = AutoWallpaperService.ACTION_NEXT_WALLPAPER
                            context.startService(intent)
                        }
                    })

            ClickablePreference(
                    title = context.getString(R.string.interval),
                    description = context.getString(R.string.duration_summary),
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
            val isLockScreenRow = remember { mutableStateOf(MainComposePreferences.isLockSourceSet()) }
            val isHomeScreenRow = remember { mutableStateOf(MainComposePreferences.isHomeSourceSet()) }
            val lockTagID = remember { mutableStateOf(MainComposePreferences.getLockTagId()) }
            val homeTagID = remember { mutableStateOf(MainComposePreferences.getHomeTagId()) }
            val lockFolderID = remember { mutableIntStateOf(MainComposePreferences.getLockFolderId()) }
            val homeFolderID = remember { mutableIntStateOf(MainComposePreferences.getHomeFolderId()) }
            val lockFolderName = remember { mutableStateOf(MainComposePreferences.getLockFolderName()) }
            val homeFolderName = remember { mutableStateOf(MainComposePreferences.getHomeFolderName()) }
            val isSameWallpaperForBoth = remember { mutableStateOf(MainPreferences.isSettingSameWallpaperToBoth()) }
            val showLockTagDialog = remember { mutableStateOf(false) }
            val showHomeTagDialog = remember { mutableStateOf(false) }
            val showLockFolderDialog = remember { mutableStateOf(false) }
            val showHomeFolderDialog = remember { mutableStateOf(false) }
            val showLockEffectsDialog = remember { mutableStateOf(false) }
            val showHomeEffectsDialog = remember { mutableStateOf(false) }

            SecondaryHeader(title = context.getString(R.string.source))

            DescriptionPreference(stringResource(R.string.source_summary))

            SwitchPreference(
                    title = stringResource(R.string.lock_screen),
                    description = stringResource(R.string.different_wallpaper_for_lock_screen),
                    checked = MainComposePreferences.isLockSourceSet(),
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
                                tint = if (lockTagID.value != null) {
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
                                    showLockTagDialog.value = true
                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                        )
                        Text(
                                text = lockTagID.value ?: stringResource(R.string.not_set),
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
                                tint = if (lockFolderName.value != null) {
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
                                    showLockFolderDialog.value = true
                                },
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1f)
                        )
                        Text(
                                text = lockFolderName.value ?: stringResource(R.string.not_set),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .align(Alignment.CenterVertically)
                        )
                    }

                    Row {
                        Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 8.dp, start = 12.dp)
                                    .height(16.dp)
                                    .align(Alignment.CenterVertically)
                        )

                        SecondaryClickablePreference(
                                title = context.getString(R.string.effects),
                                description = context.getString(R.string.apply_effects_summary),
                                onClick = {
                                    showLockEffectsDialog.value = true
                                }
                        )
                    }
                }
            }

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

                    Row {
                        Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(end = 8.dp, start = 12.dp)
                                    .height(16.dp)
                                    .align(Alignment.CenterVertically)
                        )

                        SecondaryClickablePreference(
                                title = context.getString(R.string.effects),
                                description = context.getString(R.string.apply_effects_summary),
                                onClick = {
                                    showHomeEffectsDialog.value = true
                                }
                        )
                    }
                }
            }

            SwitchPreference(
                    title = context.getString(R.string.same_wallpaper_for_both),
                    description = context.getString(R.string.same_wallpaper_for_both_summary),
                    checked = isSameWallpaperForBoth.value,
            ) {
                MainPreferences.setUseSameWallpaperForBoth(it)
                isSameWallpaperForBoth.value = true

                if (it) {
                    MainComposePreferences.setLockTagId(null)
                    MainComposePreferences.setHomeTagId(null)
                    MainComposePreferences.setLockFolderId(-1)
                    MainComposePreferences.setHomeFolderId(-1)
                    MainComposePreferences.setLockFolderName(null)
                    MainComposePreferences.setHomeFolderName(null)

                    lockTagID.value = null
                    homeTagID.value = null
                    lockFolderID.intValue = 0
                    homeFolderID.intValue = 0
                    lockFolderName.value = null
                    homeFolderName.value = null
                }
            }

            if (showLockTagDialog.value) {
                TagsDialog(
                        selected = lockTagID.value ?: "",
                        setShowing = { showLockTagDialog.value = it },
                        onTag = {
                            lockTagID.value = it.name
                            showLockTagDialog.value = false
                            MainComposePreferences.setLockTagId(it.name)

                            lockFolderID.intValue = 0
                            lockFolderName.value = null
                            MainComposePreferences.setLockFolderId(0)
                            MainComposePreferences.setLockFolderName(null)
                            MainComposePreferences.setLastLockWallpaperPosition(0)
                            MainPreferences.setUseSameWallpaperForBoth(false)
                            isSameWallpaperForBoth.value = false
                        })
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
                            MainPreferences.setUseSameWallpaperForBoth(false)
                            isSameWallpaperForBoth.value = false
                        })
            }

            if (showLockFolderDialog.value) {
                FoldersDialog(
                        selected = lockFolderID.intValue,
                        setShowing = { showLockFolderDialog.value = it },
                        onFolder = {
                            lockFolderID.intValue = it.hashcode
                            lockFolderName.value = it.name
                            showLockFolderDialog.value = false
                            MainComposePreferences.setLockFolderId(it.hashcode)
                            MainComposePreferences.setLockFolderName(it.name)

                            lockTagID.value = null
                            MainComposePreferences.setLockTagId(null)
                            MainComposePreferences.resetLastWallpaperPositions()
                            MainPreferences.setUseSameWallpaperForBoth(false)
                            isSameWallpaperForBoth.value = false
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
                            MainPreferences.setUseSameWallpaperForBoth(false)
                            isSameWallpaperForBoth.value = false
                        })
            }

            if (showLockEffectsDialog.value) {
                AutoWallpaperEffectsDialog(
                        setShowDialog = { showLockEffectsDialog.value = it },
                        initialBlurValue = MainComposePreferences.getAutoWallpaperLockBlur(),
                        initialBrightnessValue = MainComposePreferences.getAutoWallpaperLockBrightness(),
                        initialContrastValue = MainComposePreferences.getAutoWallpaperLockContrast(),
                        initialSaturationValue = MainComposePreferences.getAutoWallpaperLockSaturation(),
                        initialHueRedValue = MainComposePreferences.getAutoWallpaperLockHueRed(),
                        initialHueGreenValue = MainComposePreferences.getAutoWallpaperLockHueGreen(),
                        initialHueBlueValue = MainComposePreferences.getAutoWallpaperLockHueBlue(),
                        initialScaleRedValue = MainComposePreferences.getAutoWallpaperLockScaleRed(),
                        initialScaleGreenValue = MainComposePreferences.getAutoWallpaperLockScaleGreen(),
                        initialScaleBlueValue = MainComposePreferences.getAutoWallpaperLockScaleBlue(),
                        onApplyEffects = { blur, brightness, contrast, saturation, hueRed, hueGreen, hueBlue, scaleRed, scaleGreen, scaleBlue ->
                            MainComposePreferences.setAutoWallpaperLockBlur(blur)
                            MainComposePreferences.setAutoWallpaperLockBrightness(brightness)
                            MainComposePreferences.setAutoWallpaperLockContrast(contrast)
                            MainComposePreferences.setAutoWallpaperLockSaturation(saturation)
                            MainComposePreferences.setAutoWallpaperLockHueRed(hueRed)
                            MainComposePreferences.setAutoWallpaperLockHueGreen(hueGreen)
                            MainComposePreferences.setAutoWallpaperLockHueBlue(hueBlue)
                            MainComposePreferences.setAutoWallpaperLockScaleRed(scaleRed)
                            MainComposePreferences.setAutoWallpaperLockScaleGreen(scaleGreen)
                            MainComposePreferences.setAutoWallpaperLockScaleBlue(scaleBlue)
                        }
                )
            }

            if (showHomeEffectsDialog.value) {
                AutoWallpaperEffectsDialog(
                        setShowDialog = { showHomeEffectsDialog.value = it },
                        initialBlurValue = MainComposePreferences.getAutoWallpaperHomeBlur(),
                        initialBrightnessValue = MainComposePreferences.getAutoWallpaperHomeBrightness(),
                        initialContrastValue = MainComposePreferences.getAutoWallpaperHomeContrast(),
                        initialHueRedValue = MainComposePreferences.getAutoWallpaperHomeHueRed(),
                        initialHueGreenValue = MainComposePreferences.getAutoWallpaperHomeHueGreen(),
                        initialHueBlueValue = MainComposePreferences.getAutoWallpaperHomeHueBlue(),
                        initialSaturationValue = MainComposePreferences.getAutoWallpaperHomeSaturation(),
                        initialScaleRedValue = MainComposePreferences.getAutoWallpaperHomeScaleRed(),
                        initialScaleGreenValue = MainComposePreferences.getAutoWallpaperHomeScaleGreen(),
                        initialScaleBlueValue = MainComposePreferences.getAutoWallpaperHomeScaleBlue(),
                        onApplyEffects = { blur, brightness, contrast, saturation, hueRed, hueGreen, hueBlue, scaleRed, scaleGreen, scaleBlue ->
                            MainComposePreferences.setAutoWallpaperHomeBlur(blur)
                            MainComposePreferences.setAutoWallpaperHomeBrightness(brightness)
                            MainComposePreferences.setAutoWallpaperHomeContrast(contrast)
                            MainComposePreferences.setAutoWallpaperHomeSaturation(saturation)
                            MainComposePreferences.setAutoWallpaperHomeHueRed(hueRed)
                            MainComposePreferences.setAutoWallpaperHomeHueGreen(hueGreen)
                            MainComposePreferences.setAutoWallpaperHomeHueBlue(hueBlue)
                            MainComposePreferences.setAutoWallpaperHomeScaleRed(scaleRed)
                            MainComposePreferences.setAutoWallpaperHomeScaleGreen(scaleGreen)
                            MainComposePreferences.setAutoWallpaperHomeScaleBlue(scaleBlue)
                        }
                )
            }
        }
        item { // Settings
            val showEffectsDialog = remember { mutableStateOf(false) }
            val showDeleteDialog = remember { mutableStateOf(false) }
            val showMethodDialog = remember { mutableStateOf(false) }
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
                    title = context.getString(R.string.method),
                    description = context.getString(R.string.wallpaper_set_method_summary),
                    onClick = {
                        showMethodDialog.value = true
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
                    title = context.getString(R.string.crop_wallpaper),
                    description = context.getString(R.string.crop_wallpaper_summary),
                    checked = MainPreferences.getCropWallpaper(),
                    onCheckedChange = {
                        MainPreferences.setCropWallpaper(it)
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.linear_auto_wallpaper),
                    description = context.getString(R.string.linear_auto_wallpaper_summary),
                    checked = MainPreferences.isLinearAutoWallpaper(),
            ) {
                MainPreferences.setLinearAutoWallpaper(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.notifications),
                    description = context.getString(R.string.notifications_summary),
                    checked = MainComposePreferences.getAutoWallpaperNotification(),
            ) {
                MainComposePreferences.setAutoWallpaperNotification(it)
            }

            if (showDeleteDialog.value) {
                UsageTimesDeleteDialog {
                    showDeleteDialog.value = false
                }
            }

            if (showMethodDialog.value) {
                WallpaperMethodDialog {
                    showMethodDialog.value = false
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
