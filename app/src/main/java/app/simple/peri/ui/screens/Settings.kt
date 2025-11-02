package app.simple.peri.ui.screens

import ClickablePreference
import DescriptionPreference
import OtherApps
import SecondaryHeader
import SwitchPreference
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.BuildConfig
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.dialogs.common.ShowWarningDialog
import app.simple.peri.ui.dialogs.common.SureDialog
import app.simple.peri.ui.dialogs.settings.CacheDirectoryDialog
import app.simple.peri.ui.dialogs.settings.ConcurrencyDialog
import app.simple.peri.ui.dialogs.settings.DeveloperProfileDialog
import app.simple.peri.ui.dialogs.settings.GridSpanSelectionDialog
import app.simple.peri.ui.dialogs.settings.InureAppManagerDialog
import app.simple.peri.ui.dialogs.settings.OrderDialog
import app.simple.peri.ui.dialogs.settings.ShowPositionalDialog
import app.simple.peri.ui.dialogs.settings.SortDialog
import app.simple.peri.ui.theme.LocalBarsSize
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.viewmodels.ComposeWallpaperViewModel
import app.simple.peri.viewmodels.HomeScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "Settings"

@Composable
fun Settings(navController: NavController? = null) {
    val context = LocalContext.current
    val composeWallpaperViewModel: ComposeWallpaperViewModel = viewModel(LocalActivity.current as ComponentActivity)
    val homeViewModel: HomeScreenViewModel = viewModel(LocalActivity.current as ComponentActivity)

    val isWallpaperDetails = remember { mutableStateOf(MainComposePreferences.getWallpaperDetails()) }

    LazyColumn(
            contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = COMMON_PADDING + LocalBarsSize.current.statusBarHeight,
                    bottom = 8.dp + LocalBarsSize.current.navigationBarHeight)
    ) {
        item { // Header
            TopHeader(
                    title = stringResource(R.string.settings),
                    modifier = Modifier.padding(COMMON_PADDING),
                    navController = navController,
                    isSettings = true)
        }
        item { // Interface
            val gridSpanSelectionDialog = remember { mutableStateOf(false) }

            if (gridSpanSelectionDialog.value) {
                GridSpanSelectionDialog(
                        onDismiss = { gridSpanSelectionDialog.value = false },
                        onNumberSelected = {
                            // gridSpanSelectionDialog.value = false
                            Log.i(TAG, "Selected span: $it")
                        }
                )
            }

            SecondaryHeader(title = context.getString(R.string.interface_settings))

            ClickablePreference(
                    title = context.getString(R.string.grid_span),
                    description = context.getString(R.string.grid_span_summary),
                    onClick = {
                        gridSpanSelectionDialog.value = true
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.image_shadow_title),
                    description = context.getString(R.string.image_shadow_summary),
                    checked = MainComposePreferences.getShowImageShadow()
            ) {
                MainComposePreferences.setShowImageShadow(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.skip_palettes),
                    description = context.getString(R.string.skip_palettes_summary),
                    checked = MainComposePreferences.skipPalette()
            ) {
                MainComposePreferences.setSkipPalette(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.original_aspect_ratio),
                    description = context.getString(R.string.original_aspect_ratio_summary),
                    checked = MainComposePreferences.isOriginalAspectRatio(),
            ) {
                MainComposePreferences.setOriginalAspectRatio(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.bottom_bar),
                    description = context.getString(R.string.bottom_bar_summary),
                    checked = MainComposePreferences.getBottomHeader()
            ) {
                MainComposePreferences.setBottomHeader(it)
            }

            SwitchPreference(
                    title = stringResource(R.string.margin_between_wallpapers),
                    description = stringResource(R.string.margin_between_wallpapers_summary),
                    checked = MainComposePreferences.getMarginBetween()
            ) {
                MainComposePreferences.setMarginBetween(it)
            }

            SwitchPreference(
                    title = stringResource(R.string.enabled_wallpaper_details),
                    description = stringResource(R.string.show_wallpaper_details_on_the_main_screen),
                    checked = MainComposePreferences.getWallpaperDetails()
            ) {
                MainComposePreferences.setWallpaperDetails(it)
                isWallpaperDetails.value = it
            }

            if (isWallpaperDetails.value) {
                SwitchPreference(
                        title = context.getString(R.string.warning_indicator_title),
                        description = context.getString(R.string.warning_indicator_summary),
                        checked = MainComposePreferences.getShowWarningIndicator().invert()
                ) {
                    MainComposePreferences.setShowWarningIndicator(it.invert())
                }
            }
        }
        item { // Data
            val showSortDialog = remember { mutableStateOf(false) }
            val showOrderDialog = remember { mutableStateOf(false) }
            val showClearCacheDialog = remember { mutableStateOf(false) }
            val showCacheListDialog = remember { mutableStateOf(false) }
            val totalCache = remember { mutableLongStateOf(0L) }
            val showConcurrencyDialog = remember { mutableStateOf(false) }
            val showRecreateDatabaseDialog = remember { mutableStateOf(false) }

            if (showSortDialog.value) {
                SortDialog {
                    showSortDialog.value = false
                }
            }

            if (showOrderDialog.value) {
                OrderDialog {
                    showOrderDialog.value = false
                }
            }

            if (showClearCacheDialog.value) {
                ShowWarningDialog(
                        title = context.getString(R.string.clear_cache),
                        warning = context.getString(R.string.clear_cache_message, totalCache.longValue.toSize()),
                        onDismiss = {
                            showClearCacheDialog.value = false
                        })
            }

            if (showCacheListDialog.value) {
                CacheDirectoryDialog(
                        onDismiss = {
                            showCacheListDialog.value = false
                        },
                        onClearCache = {
                            CoroutineScope(Dispatchers.IO).launch {
                                totalCache.longValue = context.cacheDir.walkTopDown().sumOf { it.length() }
                                context.cacheDir.walkTopDown().forEach { it.delete() }
                                withContext(Dispatchers.Main) {
                                    showClearCacheDialog.value = true
                                    showCacheListDialog.value = false
                                    // Can break the loading in the whole app
                                    // Glide.get(context).clearMemory()
                                    homeViewModel.refetchSystemWallpapers()
                                }
                            }
                        }
                )
            }

            if (showConcurrencyDialog.value) {
                ConcurrencyDialog {
                    showConcurrencyDialog.value = false
                }
            }

            if (showRecreateDatabaseDialog.value) {
                SureDialog(
                        title = context.getString(R.string.recreate_database),
                        message = context.getString(R.string.recreate_database_message),
                        onSure = {
                            composeWallpaperViewModel.recreateDatabase()
                            showRecreateDatabaseDialog.value = false
                        },
                        onDismiss = {
                            showRecreateDatabaseDialog.value = false
                        }
                )
            }

            SecondaryHeader(title = context.getString(R.string.data))

            // Sort
            ClickablePreference(
                    title = context.getString(R.string.sort),
                    description = context.getString(R.string.sort_summary)
            ) {
                showSortDialog.value = true
            }

            // Order
            ClickablePreference(
                    title = context.getString(R.string.order),
                    description = context.getString(R.string.order_summary)
            ) {
                showOrderDialog.value = true
            }

            SwitchPreference(
                    title = context.getString(R.string.ignore_dot_files),
                    checked = MainPreferences.isIgnoreDotFiles(),
                    topPadding = 8.dp
            ) {
                MainPreferences.setIgnoreDotFiles(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.ignore_subdirectories),
                    checked = MainPreferences.isIgnoreSubDirs(),
                    topPadding = 8.dp
            ) {
                MainPreferences.setIgnoreSubDirs(it)
            }

            ClickablePreference(
                    title = context.getString(R.string.max_process),
                    description = context.getString(R.string.max_process_summary),
            ) {
                showConcurrencyDialog.value = true
            }

            ClickablePreference(
                    title = context.getString(R.string.clear_cache),
            ) {
                showCacheListDialog.value = true
            }

            ClickablePreference(
                    title = context.getString(R.string.recreate_database),
            ) {
                showRecreateDatabaseDialog.value = true
            }
        }
        item { // Accessibility
            SecondaryHeader(title = context.getString(R.string.accessibility))

            SwitchPreference(
                    title = context.getString(R.string.show_lock_screen_wallpaper),
                    description = context.getString(R.string.show_lock_screen_wallpaper_summary),
                    checked = MainComposePreferences.getShowLockScreenWallpaper()
            ) {
                MainComposePreferences.setShowLockScreenWallpaper(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.disable_animations),
                    checked = MainComposePreferences.getDisableAnimations(),
                    topPadding = 8.dp
            ) {
                MainComposePreferences.setDisableAnimations(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.predictive_back),
                    checked = MainComposePreferences.isPredictiveBack(),
                    topPadding = 8.dp
            ) {
                MainComposePreferences.setPredictiveBack(it)
            }
        }
        item {
            val showDeveloperProfileDialog = remember { mutableStateOf(false) }

            if (showDeveloperProfileDialog.value) {
                DeveloperProfileDialog {
                    showDeveloperProfileDialog.value = false
                }
            }

            SecondaryHeader(title = context.getString(R.string.about))

            DescriptionPreference(
                    description = BuildConfig.VERSION_NAME,
            )

            ClickablePreference(
                    title = context.getString(R.string.github),
                    description = context.getString(R.string.github_summary))
            {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = "https://github.com/Hamza417/Peristyle".toUri()
                context.startActivity(intent)
            }

            ClickablePreference(
                    title = context.getString(R.string.developer_profile))
            {
                showDeveloperProfileDialog.value = true
            }
        }
        item {
            val inureDialog = remember { mutableStateOf(false) }
            val positionalDialog = remember { mutableStateOf(false) }

            if (inureDialog.value) {
                InureAppManagerDialog {
                    inureDialog.value = false
                }
            }

            if (positionalDialog.value) {
                ShowPositionalDialog {
                    positionalDialog.value = false
                }
            }

            SecondaryHeader(title = context.getString(R.string.other_apps))

            OtherApps(
                    title = context.getString(R.string.inure_app_manager),
                    description = context.getString(R.string.inure_app_manager_summary),
                    iconResId = R.drawable.inure,
            ) {
                inureDialog.value = true
            }

            OtherApps(
                    title = context.getString(R.string.positional),
                    description = context.getString(R.string.positional_summary),
                    iconResId = R.drawable.positional,
            ) {
                positionalDialog.value = true
            }

            OtherApps(
                    title = context.getString(R.string.felicity_music_player),
                    description = context.getString(R.string.felicity_music_player_summary),
                    iconResId = R.drawable.felicity,
            ) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = "https://github.com/Hamza417/Felicity".toUri()
                context.startActivity(intent)
            }
        }
    }
}
