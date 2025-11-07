package app.simple.peri.ui.dialogs.folders

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.simple.peri.R
import java.io.File

private const val TAG = "CreateFolderDialog"

@Composable
fun CreateFolderDialog(
        selectedPath: String,
        onDismiss: () -> Unit,
        onFolderCreated: (createdFolderPath: String) -> Unit,
) {
    var newFolderName by remember { mutableStateOf("") }
    var createNomedia by remember { mutableStateOf(true) }

    val trimmed = newFolderName.trim()
    val parent = remember(selectedPath) { File(selectedPath) }
    val parentValid = parent.exists() && parent.isDirectory
    val nameValid = trimmed.isNotEmpty() && !trimmed.contains('/')
    val targetDir = remember(trimmed, parentValid) { if (parentValid && nameValid) File(parent, trimmed) else null }
    val notExistsYet = targetDir?.exists() == false
    val canCreate = parentValid && nameValid && notExistsYet

    AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                if (canCreate) {
                    TextButton(onClick = {
                        val success = targetDir.mkdirs()
                        if (success) {
                            if (createNomedia) {
                                try {
                                    File(targetDir, ".nomedia").apply { if (!exists()) createNewFile() }
                                } catch (e: Exception) {
                                    Log.e(TAG, ".nomedia create failed: ${e.message}")
                                }
                            }
                            onFolderCreated(targetDir.absolutePath)
                        } else {
                            Log.w(TAG, "mkdirs() failed for ${targetDir.absolutePath}")
                        }
                    }) { Text(text = stringResource(R.string.add)) }
                } else {
                    // Reserve space so layout doesn't jump (optional small spacer)
                    Spacer(modifier = Modifier.height(0.dp))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(text = stringResource(R.string.cancel)) }
            },
            title = { Text(text = stringResource(R.string.create_new_folder)) },
            text = {
                Column {
                    OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            label = { Text(stringResource(R.string.name)) },
                            singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = createNomedia, onCheckedChange = { createNomedia = it })
                        Text(text = stringResource(R.string.add_nomedia))
                    }
                }
            }
    )
}
