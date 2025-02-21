package app.simple.peri.compose.screens

import ClickablePreference
import DescriptionPreference
import NumberSelectionDialog
import OtherApps
import SecondaryHeader
import SwitchPreference
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.BuildConfig
import app.simple.peri.R
import app.simple.peri.activities.main.LegacyActivity
import app.simple.peri.activities.main.MainComposeActivity
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.dialogs.common.ShowWarningDialog
import app.simple.peri.compose.dialogs.common.SureDialog
import app.simple.peri.compose.dialogs.settings.CacheDirectoryDialog
import app.simple.peri.compose.dialogs.settings.ConcurrencyDialog
import app.simple.peri.compose.dialogs.settings.DeveloperProfileDialog
import app.simple.peri.compose.dialogs.settings.OrderDialog
import app.simple.peri.compose.dialogs.settings.ShowInureAppManagerDialog
import app.simple.peri.compose.dialogs.settings.ShowPositionalDialog
import app.simple.peri.compose.dialogs.settings.SortDialog
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.viewmodels.ComposeWallpaperViewModel
import app.simple.peri.viewmodels.HomeScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Settings(navController: NavController? = null) {
    val context = LocalContext.current
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val composeWallpaperViewModel: ComposeWallpaperViewModel = viewModel(LocalActivity.current as ComponentActivity)
    val homeViewModel: HomeScreenViewModel = viewModel(LocalActivity.current as ComponentActivity)

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val navigationBarHeightPx = navigationBarHeight
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }

    val topPadding = COMMON_PADDING + statusBarHeightDp
    val bottomPadding = 8.dp + navigationBarHeightDp

    LazyColumn(
            contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = topPadding,
                    bottom = bottomPadding)
    ) {
        item { // Header
            TopHeader(title = stringResource(R.string.settings), modifier = Modifier.padding(COMMON_PADDING), isSettings = true)
        }
        item { // Interface
            val numberSelectionDialog = remember { mutableStateOf(false) }

            if (numberSelectionDialog.value) {
                NumberSelectionDialog(
                        onDismiss = { numberSelectionDialog.value = false },
                        onNumberSelected = {
                            numberSelectionDialog.value = false
                        }
                )
            }

            SecondaryHeader(title = context.getString(R.string.interface_settings))

            ClickablePreference(
                    title = context.getString(R.string.grid_span),
                    description = context.getString(R.string.grid_span_summary),
                    onClick = {
                        numberSelectionDialog.value = true
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.warning_indicator_title),
                    description = context.getString(R.string.warning_indicator_summary),
                    checked = MainComposePreferences.getShowWarningIndicator().invert()
            ) {
                MainComposePreferences.setShowWarningIndicator(it.invert())
            }

            SwitchPreference(
                    title = context.getString(R.string.image_shadow_title),
                    description = context.getString(R.string.image_shadow_summary),
                    checked = MainComposePreferences.getShowImageShadow()
            ) {
                MainComposePreferences.setShowImageShadow(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.original_aspect_ratio),
                    description = context.getString(R.string.original_aspect_ratio_summary),
                    checked = MainComposePreferences.isOriginalAspectRatio(),
                    topPadding = 8.dp
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
                        onDismissRequest = {
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

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
                SwitchPreference(
                        title = context.getString(R.string.show_lock_screen_wallpaper),
                        description = context.getString(R.string.show_lock_screen_wallpaper_summary),
                        checked = MainComposePreferences.getShowLockScreenWallpaper()
                ) {
                    MainComposePreferences.setShowLockScreenWallpaper(it)
                }
            }

            SwitchPreference(
                    title = context.getString(R.string.go_back_to_legacy_interface),
                    description = context.getString(R.string.go_back_to_legacy_interface_summary),
                    checked = context.packageManager.getComponentEnabledSetting(ComponentName(context, LegacyActivity::class.java))
                            == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            ) {
                val newState = when {
                    it -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    else -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                val composeState = when {
                    it -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    else -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                }

                context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, LegacyActivity::class.java), newState, PackageManager.DONT_KILL_APP)

                context.packageManager.setComponentEnabledSetting(
                        ComponentName(context, MainComposeActivity::class.java), composeState, PackageManager.DONT_KILL_APP)
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
                intent.data = Uri.parse("https://github.com/Hamza417/Peristyle")
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
                ShowInureAppManagerDialog {
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
        }
    }
}
