package app.simple.peri.ui.dialogs.settings

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.simple.peri.R
import app.simple.peri.utils.FileUtils.toSize
import java.io.File

private const val GLIDE_CACHE_DIR = "image_manager_disk_cache"

@Composable
fun CacheDirectoryDialog(onDismiss: () -> Unit, onClearCache: () -> Unit) {
    val context = LocalContext.current
    val cacheFiles = remember { getCacheFiles(context).sortedBy { it.length() }.reversed() }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = cacheFiles.sumOf { it.length() }.toSize())
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxHeight(0.5f)) {
                    items(cacheFiles.size) { position ->
                        HorizontalDivider(modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))

                        Row(
                                modifier = Modifier
                                    .wrapContentHeight(),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(text = cacheFiles[position].absolutePath.substringAfter(context.cacheDir.absolutePath),
                                 modifier = Modifier
                                     .weight(1f)
                                     .padding(end = 16.dp))
                            Text(text = cacheFiles[position].length().toSize(),
                                 modifier = Modifier.run {
                                     wrapContentWidth()
                                         .align(androidx.compose.ui.Alignment.CenterVertically)
                                 },
                                 fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onClearCache) {
                    Text(stringResource(R.string.clear_cache))
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.close))
                }
            }
    )
}

fun getCacheFiles(context: Context): List<File> {
    val cacheDir = context.cacheDir
    return getFilesRecursively(cacheDir)
}

fun getFilesRecursively(dir: File): List<File> {
    val files = mutableListOf<File>()
    dir.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            files.addAll(getFilesRecursively(file))
        } else {
            files.add(file)
        }
    }
    return files
}
