package app.simple.peri.ui.dialogs.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.simple.peri.R
import app.simple.peri.models.PostWallpaperData
import app.simple.peri.utils.FileUtils.toSize
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PostScalingChangeDialog(onDismiss: () -> Unit, postWallpaperData: PostWallpaperData) {
    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                        text = stringResource(id = R.string.done),
                )
            },
            text = {
                Column(
                        modifier = Modifier.fillMaxWidth()
                ) {
                    ElevatedCard(
                            modifier = Modifier
                                .height(300.dp)
                                .align(Alignment.CenterHorizontally)
                                .aspectRatio(postWallpaperData.newAspectRatio),
                            elevation = CardDefaults.cardElevation(
                                    defaultElevation = 16.dp,
                            ),
                    ) {
                        GlideImage(
                                model = postWallpaperData.path,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "Size: ${postWallpaperData.oldSize.toSize()}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.wrapContentWidth()
                        )
                        Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(start = 4.dp, end = 4.dp)
                                    .size(12.dp)
                        )
                        Text(
                                text = postWallpaperData.newSize.toSize(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.wrapContentWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "Resolution: ${postWallpaperData.oldWidth}x${postWallpaperData.oldHeight}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.wrapContentWidth()
                        )
                        Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(start = 4.dp, end = 4.dp)
                                    .size(12.dp)
                        )
                        Text(
                                text = "${postWallpaperData.newWidth}x${postWallpaperData.newHeight}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.wrapContentWidth()
                        )
                    }
                }
            },
            confirmButton = {

            },
            dismissButton = {
                Button(
                        onClick = { onDismiss() },
                        content = {
                            Text(
                                    text = stringResource(id = R.string.close),
                            )
                        }
                )
            },
    )
}
