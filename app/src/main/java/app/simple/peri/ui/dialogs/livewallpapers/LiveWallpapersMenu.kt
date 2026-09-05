package app.simple.peri.ui.dialogs.livewallpapers

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.simple.peri.R
import app.simple.peri.models.LiveWallpaperInfo
import app.simple.peri.ui.commons.MenuItemWithIcon

@Composable
fun LiveWallpapersMenu(liveWallpaperInfo: LiveWallpaperInfo? = null, onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {
    val options = listOf(
            stringResource(R.string.uninstall)
    )

    AlertDialog(
            title = {
                Text(
                        text = liveWallpaperInfo?.name ?: "",
                )
            },
            onDismissRequest = { onDismiss() },
            text = {
                Column {
                    options.forEach { option ->
                        MenuItemWithIcon(
                                icon = Icons.Rounded.Delete,
                                summary = stringResource(R.string.uninstall_live_wallpaper_summary),
                                text = option,
                                isDestructive = true,
                                onClick = {
                                    onOptionSelected(option)
                                    onDismiss()
                                }
                        )
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
    )
}
