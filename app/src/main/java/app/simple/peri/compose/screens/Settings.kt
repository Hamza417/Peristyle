package app.simple.peri.compose.screens

import ClickablePreference
import OtherApps
import SecondaryHeader
import SwitchPreference
import android.content.Intent
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
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.dialogs.ShowInureAppManagerDialog
import app.simple.peri.compose.dialogs.ShowPositionalDialog
import app.simple.peri.preferences.MainPreferences

@Composable
fun Settings(navController: NavController? = null) {
    val context = LocalContext.current

    LazyColumn(
            modifier = Modifier
                .padding(COMMON_PADDING)
                .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        item { // Header
            TopHeader(title = "Settings", modifier = Modifier.padding(COMMON_PADDING))

            HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = COMMON_PADDING)
                        .fillMaxWidth()
            )
        }
        item { // Settings
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
                    title = context.getString(R.string.linear_auto_wallpaper),
                    checked = MainPreferences.isLinearAutoWallpaper()
            ) {
                MainPreferences.setLinearAutoWallpaper(it)
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