package app.simple.peri.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.compose.theme.PeristyleTheme
import app.simple.peri.glide.effect.Effect
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ParcelUtils.parcelable
import app.simple.peri.viewmodels.EffectsViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

class EffectsActivity : ComponentActivity() {

    private val wallpaper: Wallpaper? by lazy {
        intent.parcelable("wallpaper")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val effectsViewModel: EffectsViewModel = viewModel()
            val effects = effectsViewModel.getEffects().observeAsState().value ?: emptyList()

            PeristyleTheme {
                Surface {
                    WallpaperGrid(effects, wallpaper!!) {
                        onEffectResult(it)
                    }
                }
            }
        }
    }

    private fun onEffectResult(effect: app.simple.peri.models.Effect) {
        val resultIntent = Intent()
        resultIntent.putExtra("effect", effect)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun WallpaperGrid(effects: List<app.simple.peri.models.Effect>, wallpaper: Wallpaper, onCardClick: (app.simple.peri.models.Effect) -> Unit = {}) {
        LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            items(effects.size) { index ->
                val effect = effects[index]
                val context = LocalContext.current
                ElevatedCard(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .aspectRatio(wallpaper.width?.toFloat()!! / wallpaper.height?.toFloat()!!),
                        onClick = { onCardClick(effect) }
                ) {
                    GlideImage(
                            model = Effect(LocalContext.current, effect, wallpaper),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
