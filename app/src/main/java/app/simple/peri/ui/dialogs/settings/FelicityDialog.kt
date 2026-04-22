package app.simple.peri.ui.dialogs.settings

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import app.simple.peri.R

private const val FELICITY_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=app.simple.felicity"
private const val FELICITY_GITHUB_URL = "https://github.com/Hamza417/Felicity"
private const val FELICITY_TELEGRAM_URL = "https://t.me/felicity_music_player"

@Composable
fun FelicityDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val list = listOf("Play Store", "GitHub", "Telegram Channel")

    AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = { Text(text = stringResource(id = R.string.felicity_music_player)) },
            text = {
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    list.forEachIndexed { index, item ->
                        Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val url = when (index) {
                                        0 -> FELICITY_PLAY_STORE_URL
                                        1 -> FELICITY_GITHUB_URL
                                        2 -> FELICITY_TELEGRAM_URL
                                        else -> FELICITY_GITHUB_URL
                                    }

                                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
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
