package app.simple.peri.activities.main

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import app.simple.peri.R
import app.simple.peri.extensions.BaseComponentActivity
import app.simple.peri.preferences.PathPickerPreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.dialogs.common.ShowWarningDialog
import app.simple.peri.ui.dialogs.folders.CreateFolderDialog
import app.simple.peri.ui.theme.PeristyleTheme
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.utils.SDCard
import java.io.File

class PathPickerActivity : BaseComponentActivity() {
    private var backPressedCallback: OnBackPressedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferences.init(applicationContext)

        setContent {
            // Resolve sdcard root and last picked path synchronously for initial state
            val sdCardRoot = remember {
                try {
                    SDCard.findSdCardPath(applicationContext).absolutePath
                } catch (_: NullPointerException) {
                    null
                }
            }
            val defaultMain = remember { Environment.getExternalStorageDirectory().absolutePath }
            val lastPicked = remember {
                PathPickerPreferences.getLastPickedPath().let { path ->
                    if (path.isNotEmpty()) {
                        val f = File(path)
                        if (f.exists() && f.isDirectory) path else ""
                    } else ""
                }
            }

            var sdcardMode by remember { mutableStateOf(sdCardRoot != null && lastPicked.isNotEmpty() && lastPicked.startsWith(sdCardRoot!!)) }
            var mainPath by remember { mutableStateOf(if (sdcardMode) (sdCardRoot ?: defaultMain) else defaultMain) }
            var selectedPath by remember { mutableStateOf(lastPicked.ifEmpty { mainPath }) }
            var statusBarHeight by remember { mutableIntStateOf(0) }
            var navigationBarHeight by remember { mutableIntStateOf(0) }
            var showNoSdCardWarning by remember { mutableStateOf(false) }
            var showCreateDirDialog by remember { mutableStateOf(false) }

            // Persist any path change immediately
            LaunchedEffect(selectedPath) {
                if (selectedPath.isNotEmpty()) {
                    PathPickerPreferences.setLastPickedPath(selectedPath)
                }
            }

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
                        Log.i("PathChooser", "Selected path: $selectedPath for main path: $mainPath")
                        if (selectedPath.split("/").size > 1) {
                            if (selectedPath.equals(mainPath).not()) {
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
                ) {
                    if (showNoSdCardWarning) {
                        ShowWarningDialog(
                                title = stringResource(id = R.string.error),
                                warning = stringResource(id = R.string.no_sd_card_found),
                                onDismiss = {
                                    showNoSdCardWarning = false
                                }
                        )
                    }

                    if (showCreateDirDialog) {
                        CreateFolderDialog(
                                selectedPath = selectedPath,
                                onDismiss = { showCreateDirDialog = false },
                                onFolderCreated = { created ->
                                    showCreateDirDialog = false
                                    selectedPath = created
                                }
                        )
                    }

                    Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 8.dp, end = 8.dp, top = topPadding)
                    ) {
                        TopHeader(
                                title = stringResource(id = R.string.select_folder),
                                isShowAdd = true,
                                isShowSdcard = true,
                                isShowSearch = false,
                                isHideSettings = true,
                                isHideAutoWallpaper = true,
                                modifier = Modifier.padding(start = COMMON_PADDING, end = COMMON_PADDING, top = COMMON_PADDING, bottom = 0.dp),
                                onAdd = {
                                    showCreateDirDialog = true
                                },
                                onSdcard = {
                                    sdcardMode = sdcardMode.not()
                                    try {
                                        mainPath = if (sdcardMode) {
                                            SDCard.findSdCardPath(applicationContext).absolutePath
                                        } else {
                                            Environment.getExternalStorageDirectory().absolutePath
                                        }

                                        selectedPath = mainPath
                                    } catch (_: NullPointerException) {
                                        sdcardMode = false
                                        mainPath = Environment.getExternalStorageDirectory().absolutePath
                                        selectedPath = mainPath
                                        showNoSdCardWarning = true
                                    }
                                }
                        )

                        Text(
                                text = selectedPath,
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .padding(start = COMMON_PADDING, end = COMMON_PADDING),
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp
                        )

                        if (sdcardMode) {
                            Text(
                                    text = stringResource(id = R.string.sdcard_issue),
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .padding(start = COMMON_PADDING, end = COMMON_PADDING),
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.error
                            )
                        }

                        HorizontalDivider(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                        )

                        DirectoryList(
                                directories = (selectedPath.takeIf { it.isNotEmpty() }
                                    ?.let { File(it).listFiles()?.toList() } ?: emptyList())
                                    .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name }),
                                onDirectorySelected = { path ->
                                    selectedPath = path
                                },
                                modifier = Modifier
                                    .weight(1f)
                                // .padding(top = 8.dp, bottom = 8.dp)
                        )

                        HorizontalDivider()

                        Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = bottomPadding),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                    modifier = Modifier
                                        .padding(COMMON_PADDING)
                                        .weight(1f),
                                    onClick = { finish() }) {
                                Text(
                                        text = stringResource(id = R.string.cancel),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(8.dp))
                            }
                            Button(
                                    modifier = Modifier
                                        .padding(COMMON_PADDING)
                                        .weight(1f),
                                    onClick = { onPathChosen(selectedPath) }) {
                                Text(
                                        text = stringResource(id = R.string.select),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onPathChosen(path: String) {
        PathPickerPreferences.setLastPickedPath(path) // ensure persistence on selection
        val resultIntent = Intent()
        resultIntent.putExtra("chosen_path", path)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @Composable
    fun DirectoryList(directories: List<File>, onDirectorySelected: (String) -> Unit, modifier: Modifier) {
        LazyColumn(
                modifier = modifier
        ) {
            items(directories.size) { index ->
                val directory = directories[index]
                Row(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .wrapContentHeight()
                            .then(
                                    if (directory.isDirectory) {
                                        Modifier.clickable {
                                            onDirectorySelected(directory.absolutePath)
                                        }
                                    } else {
                                        Modifier
                                    }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = directory.name,
                            modifier = Modifier
                                .weight(1F)
                                .padding(COMMON_PADDING),
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp,
                            color = if (directory.isDirectory) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            }
                    )
                    if (directory.isDirectory) {
                        Text(
                                text = directory.listFiles()?.size.toString(),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(8.dp),
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                                text = directory.length().toSize(),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(8.dp),
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
