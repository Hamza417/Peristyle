package app.simple.peri.compose.dialogs.settings

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

private const val INURE_GITHUB_URL = "https://github.com/Hamza417/Inure"
private const val INURE_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=app.simple.inure.play"
private const val INURE_F_DROID_URL = "https://f-droid.org/packages/app.simple.inure"

@Composable
fun ShowInureAppManagerDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val list = listOf("GitHub", "Play Store", "F-Droid")

    AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = { Text(text = stringResource(id = R.string.inure_app_manager)) },
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
                                        0 -> INURE_GITHUB_URL
                                        1 -> INURE_PLAY_STORE_URL
                                        2 -> INURE_F_DROID_URL
                                        else -> INURE_GITHUB_URL
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }) {
                            Text(text = item)
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
