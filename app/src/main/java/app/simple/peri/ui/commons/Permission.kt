package app.simple.peri.ui.commons

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import app.simple.peri.activities.main.PathPickerActivity

@Composable
fun FolderBrowser(onCancel: () -> Unit, onStorageGranted: (String) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val chosenPath = result.data?.getStringExtra("chosen_path")
                ?: return@rememberLauncherForActivityResult
            Log.i("PathChooserActivity", "Chosen path: $chosenPath")
            onStorageGranted(chosenPath)
        } else {
            onCancel()
        }
    }

    // Launch the directory selection intent
    LaunchedEffect(Unit) {
        val intent = Intent(context, PathPickerActivity::class.java)
        launcher.launch(intent)
    }
}
