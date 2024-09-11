package app.simple.peri.compose.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R

private const val POSITIONAL_GITHUB_URL = "https://github.com/Hamza417/Positional"
private const val POSITIONAL_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=app.simple.positional"

@Composable
fun ShowPositionalDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val list = listOf("GitHub", "Play Store")

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(id = R.string.positional)) },
            text = {
                Column(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    list.forEachIndexed { index, item ->
                        Button(
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                onClick = {
                                    val url = when (index) {
                                        0 -> POSITIONAL_GITHUB_URL
                                        1 -> POSITIONAL_PLAY_STORE_URL
                                        else -> POSITIONAL_GITHUB_URL
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    onDismiss()
                                }) {
                            Text(text = item)
                        }
                    }
                }
            },
            confirmButton = {},
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
