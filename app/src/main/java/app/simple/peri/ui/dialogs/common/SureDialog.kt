package app.simple.peri.ui.dialogs.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import app.simple.peri.R

@Composable
fun SureDialog(
        title: String = stringResource(R.string.delete),
        message: String,
        onSure: () -> Unit,
        onDismissRequest: () -> Unit
) {
    AlertDialog(
            onDismissRequest = { onDismissRequest() },
            title = {
                Text(text = title)
            },
            text = {
                Column {
                    Text(
                            text = message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onSure() }) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { onDismissRequest() }) {
                    Text(stringResource(id = R.string.no))
                }
            },
    )
}
