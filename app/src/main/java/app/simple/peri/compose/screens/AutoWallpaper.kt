package app.simple.peri.compose.screens

import ClickablePreference
import SwitchPreference
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.preferences.MainPreferences

@Composable
fun AutoWallpaper(navController: NavController? = null) {
    val context = LocalContext.current

    LazyColumn(
            modifier = Modifier
                .padding(start = COMMON_PADDING, end = COMMON_PADDING)
                .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        item { // Header
            TopHeader(title = stringResource(R.string.auto_wallpaper), modifier = Modifier.padding(COMMON_PADDING))

            HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = COMMON_PADDING)
                        .fillMaxWidth()
            )
        }
        item {
            val screenSelectionDialog = remember { mutableStateOf(false) }
            val autoWallpaperDialog = remember { mutableStateOf(false) }

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

            ClickablePreference(
                    title = context.getString(R.string.auto_wallpaper),
                    description = context.getString(R.string.auto_wallpaper_summary),
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

            SwitchPreference(
                    title = context.getString(R.string.crop_wallpaper),
                    description = context.getString(R.string.crop_wallpaper_summary),
                    checked = MainPreferences.getCropWallpaper(),
                    onCheckedChange = {
                        MainPreferences.setCropWallpaper(it)
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.different_wallpaper_for_lock_screen),
                    checked = MainPreferences.isDifferentWallpaperForLockScreen(),
                    onCheckedChange = {
                        MainPreferences.setDifferentWallpaperForLockScreen(it)
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.change_wallpaper_when_sleeping),
                    checked = MainPreferences.isWallpaperWhenSleeping(),
                    onCheckedChange = {
                        MainPreferences.setWallpaperWhenSleeping(it)
                    }
            )

            SwitchPreference(
                    title = context.getString(R.string.linear_auto_wallpaper),
                    checked = MainPreferences.isLinearAutoWallpaper()
            ) {
                MainPreferences.setLinearAutoWallpaper(it)
            }
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
