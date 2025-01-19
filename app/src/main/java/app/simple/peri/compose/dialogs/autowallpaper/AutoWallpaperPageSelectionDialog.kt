package app.simple.peri.compose.dialogs.autowallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R

@Composable
fun AutoWallpaperPageSelectionDialog(onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {
    val options = listOf(
            stringResource(R.string.wallpaper_manager),
            stringResource(R.string.live_auto_wallpaper),
    )

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Column {
                    Text(text = stringResource(R.string.auto_wallpaper))
                    Text(
                            text = stringResource(R.string.auto_wallpaper_api_summary),
                            style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            text = {
                Column {
                    options.forEach { option ->
                        Button(
                                onClick = {
                                    onOptionSelected(option)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                    text = option,
                                    color = MaterialTheme.colorScheme.onSurface,
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
