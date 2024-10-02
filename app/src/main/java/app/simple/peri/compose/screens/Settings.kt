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
import androidx.navigation.NavController
import app.simple.peri.BuildConfig
import app.simple.peri.R
import app.simple.peri.activities.LegacyActivity
import app.simple.peri.activities.MainComposeActivity
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.dialogs.common.ShowWarningDialog
import app.simple.peri.compose.dialogs.settings.DeveloperProfileDialog
import app.simple.peri.compose.dialogs.settings.OrderDialog
import app.simple.peri.compose.dialogs.settings.ShowInureAppManagerDialog
import app.simple.peri.compose.dialogs.settings.ShowPositionalDialog
import app.simple.peri.compose.dialogs.settings.SortDialog
import app.simple.peri.glide.modules.GlideApp
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.toSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun Settings(navController: NavController? = null) {
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
                            MainComposePreferences.setGridSpanCount(it)
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
        }
        item { // Data
            val showSortDialog = remember { mutableStateOf(false) }
            val showOrderDialog = remember { mutableStateOf(false) }
            val showClearCacheDialog = remember { mutableStateOf(false) }
            var totalCache = remember { mutableLongStateOf(0L) }

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
                    title = context.getString(R.string.clear_cache),
                    description = context.getString(R.string.clear_cache_summary)
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    val imagesCachePath = File("${context.cacheDir}/image_manager_disk_cache/")
                    totalCache.longValue = imagesCachePath.walkTopDown().sumOf { it.length() }
                    GlideApp.get(context).clearDiskCache()
                    withContext(Dispatchers.Main) {
                        showClearCacheDialog.value = true
                    }
                }
            }
        }
        item { // Accessibility
            SecondaryHeader(title = context.getString(R.string.accessibility))

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
                    title = context.getString(R.string.telegram_group),
                    description = context.getString(R.string.telegram_summary))
            {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://t.me/peristyle_app")
                context.startActivity(intent)
            }

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
