package app.simple.peri.compose.commons

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.viewmodels.WallpaperViewModel

@Composable
fun RequestDirectoryPermission(onCancel: () -> Unit) {
    val context = LocalContext.current
    val wallpaperViewModel: WallpaperViewModel = viewModel()

    val storageResult = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            Log.d("Setup", "Storage Uri: $uri")
            if (uri != null) {
                val modeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, modeFlags)
                MainPreferences.setStorageUri(uri.toString())
                wallpaperViewModel.refresh()
                Log.d("Setup", "Storage Uri: $uri")
            }
        } else {
            Log.d("Setup", "Storage Uri: null")
            onCancel()
        }
    }

    // Launch the directory selection intent
    LaunchedEffect(Unit) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        storageResult.launch(intent)
    }
}
