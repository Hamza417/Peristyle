package app.simple.peri.ui.dialogs.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.WallpaperSort

@Composable
fun OrderDialog(onDismiss: () -> Unit) {
    val list = listOf(
            Pair(stringResource(R.string.ascending), WallpaperSort.ASC),
            Pair(stringResource(R.string.descending), WallpaperSort.DESC)
    )

    AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = { Text(text = stringResource(id = R.string.order)) },
            text = {
                Column(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    list.forEachIndexed { _, item ->
                        Button(
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                onClick = {
                                    MainPreferences.setOrder(item.second)
                                    onDismiss()
                                },
                                colors = if (MainPreferences.getOrder() == item.second) {
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                } else {
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                        ) {
                            Text(text = item.first,
                                 color = if (MainPreferences.getOrder() == item.second) {
                                     MaterialTheme.colorScheme.onPrimary
                                 } else {
                                     MaterialTheme.colorScheme.onSurface
                                 })
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
                    Text(text = stringResource(id = R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
