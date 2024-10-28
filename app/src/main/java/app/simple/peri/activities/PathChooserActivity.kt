package app.simple.peri.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import app.simple.peri.R
import app.simple.peri.compose.theme.PeristyleTheme
import java.io.File

class PathChooserActivity : ComponentActivity() {
    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var selectedPath by remember { mutableStateOf(Environment.getExternalStorageDirectory().absolutePath) }
            var statusBarHeight by remember { mutableIntStateOf(0) }
            var navigationBarHeight by remember { mutableIntStateOf(0) }

            statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
                    LocalView.current.rootWindowInsets
            ).getInsets(WindowInsetsCompat.Type.statusBars()).top
            navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
                    LocalView.current.rootWindowInsets
            ).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            val statusBarHeightPx = statusBarHeight
            val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
            val navigationBarHeightPx = navigationBarHeight
            val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }

            val topPadding = 8.dp + statusBarHeightDp
            val bottomPadding = 8.dp + navigationBarHeightDp

            if (backPressedCallback == null) {
                backPressedCallback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (selectedPath.split("/").size > 1) {
                            if (selectedPath.equals(Environment.getExternalStorageDirectory().absolutePath).not()) {
                                selectedPath = selectedPath.substringBeforeLast("/")
                            } else {
                                finish()
                            }
                        } else {
                            finish()
                        }
                    }
                }

                onBackPressedDispatcher.addCallback(backPressedCallback!!)
            }

            PeristyleTheme {
                Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topPadding, bottom = bottomPadding)
                ) {
                    Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 16.dp, end = 16.dp)
                    ) {
                        DirectoryList(
                                modifier = Modifier.weight(1f),
                                directories = (selectedPath.takeIf { it.isNotEmpty() }
                                    ?.let { File(it).listFiles()?.toList() }
                                    ?: getInternalStorageDirectories()).sortedBy { it.name },
                                selectedPath = selectedPath,
                                onDirectorySelected = { path ->
                                    selectedPath = path
                                }
                        )
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .weight(1f),
                                    onClick = { finish() }) {
                                Text(stringResource(id = R.string.cancel))
                            }
                            Button(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .weight(1f),
                                    onClick = { onPathChosen(selectedPath) }) {
                                Text(stringResource(id = R.string.select))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getInternalStorageDirectories(): List<File> {
        val internalStorage = Environment.getExternalStorageDirectory()
        return internalStorage.listFiles()?.filter { it.isDirectory } ?: emptyList()
    }

    private fun onPathChosen(path: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("chosen_path", path)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

@Composable
fun DirectoryList(directories: List<File>, onDirectorySelected: (String) -> Unit, modifier: Modifier, selectedPath: String) {
    LazyColumn(
            modifier = modifier
    ) {
        item {
            Text(
                    text = stringResource(id = R.string.select_folder),
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
            )

            Text(
                    text = selectedPath,
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
            )
        }
        items(directories.size) { index ->
            val directory = directories[index]
            Row(modifier = Modifier
                .wrapContentHeight()
                .clickable {
                    if (directory.isDirectory) {
                        onDirectorySelected(directory.absolutePath)
                    }
                }
            ) {
                Text(
                        text = directory.name,
                        modifier = Modifier
                            .weight(1F)
                            .padding(8.dp),
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                )
                if (directory.isDirectory) {
                    Text(
                            text = directory.listFiles()?.size.toString(),
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(8.dp),
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                    )
                }
            }
        }
    }
}
