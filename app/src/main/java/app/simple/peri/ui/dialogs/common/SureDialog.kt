package app.simple.peri.ui.dialogs.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.simple.peri.R
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

@Composable
fun SureDialog(
        title: String = stringResource(R.string.delete),
        message: String,
        onSure: () -> Unit,
        onDismiss: () -> Unit
) {
    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = title)
            },
            text = {
                Column {
                    Text(
                            text = message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(onClick = { onSure() }) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text(stringResource(id = R.string.no))
                }
            },
    )
}

@Composable
fun HazeSureDialog(
        title: String = stringResource(R.string.delete),
        message: String,
        hazeState: HazeState,
        onSure: () -> Unit,
        onDismiss: () -> Unit
) {
    Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent.copy(alpha = 0.3f))
                .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
    ) {
        Card(
                modifier = Modifier
                    .padding(24.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .hazeEffect(
                            state = hazeState,
                            style = HazeDefaults.style(backgroundColor = Color(0x66000000), blurRadius = 20.dp))
                    .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                    ),
                colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                ),
                border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                    modifier = Modifier.padding(24.dp)
            ) {
                Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.no))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = onSure) {
                        Text(stringResource(id = R.string.yes))
                    }
                }
            }
        }
    }
}
