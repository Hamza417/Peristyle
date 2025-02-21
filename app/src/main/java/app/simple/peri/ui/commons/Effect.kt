package app.simple.peri.ui.commons

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.IntentCompat
import app.simple.peri.activities.main.EffectsActivity
import app.simple.peri.models.Effect
import app.simple.peri.models.Wallpaper

@Composable
fun LaunchEffectActivity(wallpaper: Wallpaper, onEffect: (Effect) -> Unit, onCanceled: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val effect = IntentCompat.getParcelableExtra(result.data!!, "effect", Effect::class.java)
            onEffect(effect!!)
        } else {
            onCanceled()
        }
    }

    // Launch the effect activity
    LaunchedEffect(Unit) {
        val intent = Intent(context, EffectsActivity::class.java)
        intent.putExtra("wallpaper", wallpaper as Parcelable)
        launcher.launch(intent)
    }
}
