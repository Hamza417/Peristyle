package app.simple.peri.ui.dialogs.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R

private const val GITHUB_URL = "https://github.com/Hamza417"
private const val PLAY_STORE_URL = "https://play.google.com/store/apps/dev?id=9002962740272949113"

@Composable
fun DeveloperProfileDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val list = listOf(
            stringResource(R.string.github),
            stringResource(R.string.play_store))

    AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = { Text(text = stringResource(id = R.string.developer_profile)) },
            text = {
                Column(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    list.forEachIndexed { _, item ->
                        Button(
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                onClick = {
                                    when (item) {
                                        context.getString(R.string.github) -> {
                                            openURL(GITHUB_URL, context)
                                        }

                                        context.getString(R.string.play_store) -> {
                                            openURL(PLAY_STORE_URL, context)
                                        }
                                    }
                                    onDismiss()
                                },
                        ) {
                            Text(text = item,
                                 color = MaterialTheme.colorScheme.onPrimary
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
                    Text(text = stringResource(id = R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}

fun openURL(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}
