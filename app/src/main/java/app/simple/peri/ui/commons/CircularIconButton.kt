package app.simple.peri.ui.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularIconButton(
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
        imageVector: ImageVector,
        contentDescription: String? = null,
        backgroundColor: Color = Color.Black.copy(alpha = 0.3f),
        iconTint: Color = Color.White,
        size: Dp = 40.dp
) {
    IconButton(
            onClick = onClick,
            modifier = modifier
    ) {
        Box(
                modifier = Modifier
                    .size(size)
                    .background(
                            color = backgroundColor,
                            shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = iconTint
            )
        }
    }
}