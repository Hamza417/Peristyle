package app.simple.peri.compose.screens

import ClickablePreference
import NumberSelectionDialog
import OtherApps
import SecondaryHeader
import SwitchPreference
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.activities.LegacyActivity
import app.simple.peri.activities.MainComposeActivity
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.dialogs.ShowInureAppManagerDialog
import app.simple.peri.compose.dialogs.ShowPositionalDialog
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.ConditionUtils.invert

@Composable
fun Settings(navController: NavController? = null) {
    val context = LocalContext.current

    LazyColumn(
            modifier = Modifier
                .padding(start = COMMON_PADDING, end = COMMON_PADDING)
                .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        item { // Header
            TopHeader(title = stringResource(R.string.settings), modifier = Modifier.padding(COMMON_PADDING))

            HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = COMMON_PADDING)
                        .fillMaxWidth()
            )
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
        item { // Accessibility
            SecondaryHeader(title = context.getString(R.string.accessibility))

            SwitchPreference(
                    title = context.getString(R.string.ignore_dot_files),
                    checked = MainPreferences.isIgnoreDotFiles()
            ) {
                MainPreferences.setIgnoreDotFiles(it)
            }

            SwitchPreference(
                    title = context.getString(R.string.ignore_subdirectories),
                    checked = MainPreferences.isIgnoreSubDirs()
            ) {
                MainPreferences.setIgnoreSubDirs(it)
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
            SecondaryHeader(title = context.getString(R.string.about))

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
