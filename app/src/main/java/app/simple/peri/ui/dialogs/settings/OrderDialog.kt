package app.simple.peri.ui.dialogs.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    list.forEach { item ->
                        val label = item.first
                        val value = item.second
                        val selected = MainPreferences.getOrder() == value

                        Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    MainPreferences.setOrder(value)
                                    onDismiss()
                                },
                                colors = if (selected) {
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                } else {
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                        ) {
                            val textColor = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }

                            val icon = when (value) {
                                WallpaperSort.ASC -> Icons.Rounded.ArrowUpward
                                else -> Icons.Rounded.ArrowDownward
                            }

                            val iconColor = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = iconColor)
                                Spacer(Modifier.width(24.dp))
                                Text(text = label, color = textColor, textAlign = TextAlign.Start)
                            }
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
