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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences

@Composable
fun ThemeDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    val currentThemeMode = MainComposePreferences.getThemeMode()
    
    val list = listOf(
        Pair(context.getString(R.string.theme_mode_auto), MainComposePreferences.THEME_MODE_AUTO),
        Pair(context.getString(R.string.theme_mode_light), MainComposePreferences.THEME_MODE_LIGHT),
        Pair(context.getString(R.string.theme_mode_dark), MainComposePreferences.THEME_MODE_DARK)
    )

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = { Text(text = stringResource(id = R.string.theme)) },
        text = {
            Column(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                list.forEachIndexed { _, item ->
                    Button(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        onClick = {
                            MainComposePreferences.setThemeMode(item.second)
                            onDismiss()
                        },
                        colors = if (currentThemeMode == item.second) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        } else {
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    ) {
                        Text(text = item.first,
                             color = if (currentThemeMode == item.second) {
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