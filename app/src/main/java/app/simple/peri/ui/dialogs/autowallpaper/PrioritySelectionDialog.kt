package app.simple.peri.ui.dialogs.autowallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences

@Composable
fun PrioritySelectionDialog(onDismiss: () -> Unit) {
    val options = listOf(
            stringResource(id = R.string.default_priority) to MainComposePreferences.PRIORITY_DEFAULT,
            stringResource(id = R.string.new_wallpapers_first) to MainComposePreferences.PRIORITY_NEW_FIRST,
            stringResource(id = R.string.old_wallpapers_first) to MainComposePreferences.PRIORITY_OLD_FIRST
    )

    val storedOption = MainComposePreferences.getWallpaperPickPriority()
    val selectedOption = remember { mutableStateOf(options.find { it.second == storedOption }) }

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Column {
                    Text(text = stringResource(id = R.string.wallpaper_pick_priority))
                    Text(
                            text = stringResource(id = R.string.wallpaper_pick_priority_summary),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 16.sp
                    )
                }
            },
            text = {
                Column {
                    options.forEach { option ->
                        Button(
                                onClick = {
                                    selectedOption.value = option
                                    MainComposePreferences.setWallpaperPickPriority(option.second)
                                    onDismiss()
                                },
                                colors = when (selectedOption.value) {
                                    option -> {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    }

                                    else -> {
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurface
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
                        }
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
