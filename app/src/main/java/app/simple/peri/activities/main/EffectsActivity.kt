package app.simple.peri.activities.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.constants.DIALOG_OPTION_FONT_SIZE
import app.simple.peri.compose.constants.DIALOG_TITLE_FONT_SIZE
import app.simple.peri.compose.theme.PeristyleTheme
import app.simple.peri.extensions.BaseComponentActivity
import app.simple.peri.models.Effect
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ParcelUtils.parcelable
import app.simple.peri.viewmodels.EffectsViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import app.simple.peri.glide.effect.Effect as GlideEffect

class EffectsActivity : BaseComponentActivity() {

    private val wallpaper: Wallpaper by lazy {
        intent.parcelable("wallpaper")!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PeristyleTheme {
                Surface {
                    WallpaperGrid() {
                        onEffectResult(it)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        Glide.get(applicationContext).clearMemory()
        super.onDestroy()
    }

    private fun onEffectResult(effect: Effect) {
        val resultIntent = Intent()
        resultIntent.putExtra("effect", effect)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    @OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
    @Composable
    fun WallpaperGrid(onCardClick: (Effect) -> Unit = {}) {
        val effectsViewModel: EffectsViewModel = viewModel()
        val effects = effectsViewModel.getEffects().observeAsState().value ?: emptyList()
        val context = LocalContext.current

        LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TopHeader(
                        title = stringResource(R.string.saved_effects),
                        count = effects.size,
                        modifier = Modifier.padding(COMMON_PADDING),
                        isSettings = true
                )
            }
            if (effects.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                            text = stringResource(R.string.no_effects_summary),
                            modifier = Modifier.padding(COMMON_PADDING)
                    )
                }
            }
            items(effects.size, key = { effects[it].id }) { index ->
                val showMenu = remember { mutableStateOf(false) }

                if (showMenu.value) {
                    EffectMenu(
                            onDismiss = { showMenu.value = false },
                            onOptionSelected = { option ->
                                when (option) {
                                    context.getString(R.string.delete) -> {
                                        effectsViewModel.deleteEffect(effects[index]) {
                                            showMenu.value = false
                                        }
                                    }
                                }
                            }
                    )
                }

                ElevatedCard(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .aspectRatio(wallpaper.width?.toFloat()!! / wallpaper.height?.toFloat()!!)
                            .combinedClickable(
                                    onLongClick = { showMenu.value = true },
                                    onClick = { onCardClick(effects[index]) }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 16.dp,
                        )
                ) {
                    GlideImage(
                            model = GlideEffect(applicationContext, effects[index], wallpaper),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    @Composable
    fun EffectMenu(onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {
        val options = listOf(
                stringResource(R.string.delete),
        )

        AlertDialog(
                title = {
                    Text(
                            text = stringResource(R.string.effects),
                            fontSize = DIALOG_TITLE_FONT_SIZE,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            style = TextStyle.Default,
                    )
                },
                onDismissRequest = { onDismiss() },
                text = {
                    Column {
                        options.forEach { option ->
                            Button(
                                    onClick = {
                                        onOptionSelected(option)
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                        text = option,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = DIALOG_OPTION_FONT_SIZE,
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
                        Text(text = stringResource(R.string.close))
                    }
                },
                properties = DialogProperties(dismissOnClickOutside = true)
        )
    }
}
