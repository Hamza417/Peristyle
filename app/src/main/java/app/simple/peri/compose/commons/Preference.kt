import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PREFERENCE_TITLE_SIZE = 18.sp
val PREFERENCE_DESCRIPTION_SIZE = 14.sp
val PREF_HORIZONTAL_PADDING = 16.dp

@Composable
fun SwitchPreference(title: String,
                     description: String = "",
                     checked: Boolean,
                     topPadding: Dp = 24.dp,
                     onCheckedChange: (Boolean) -> Unit) {
    var isChecked by remember { mutableStateOf(checked) }

    Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = PREF_HORIZONTAL_PADDING, end = PREF_HORIZONTAL_PADDING, top = topPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
                modifier = Modifier.weight(1f)
        ) {
            Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = PREFERENCE_TITLE_SIZE,
                    modifier = Modifier.padding(bottom = 4.dp)
            )
            if (description.isNotEmpty()) {
                Text(
                        text = description,
                        fontWeight = FontWeight.Normal,
                        fontSize = PREFERENCE_DESCRIPTION_SIZE,
                        modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        Switch(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    onCheckedChange(it)
                }
        )
    }
}

@Composable
fun ClickablePreference(@SuppressLint("ModifierParameter") modifier: Modifier? = null,
                        title: String,
                        description: String = "",
                        onClick: () -> Unit) {

    val verticalPadding = 16.dp

    Card(
            modifier = modifier ?: Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(PREF_HORIZONTAL_PADDING),
            colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
            ),
            onClick = onClick
    ) {
        Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = PREFERENCE_TITLE_SIZE,
                modifier = Modifier
                    .padding(start = PREF_HORIZONTAL_PADDING,
                             end = PREF_HORIZONTAL_PADDING,
                             top = verticalPadding,
                             bottom = if (description.isNotEmpty()) 0.dp else verticalPadding),
        )
        if (description.isNotEmpty()) {
            Text(
                    text = description,
                    fontWeight = FontWeight.Normal,
                    fontSize = PREFERENCE_DESCRIPTION_SIZE,
                    modifier = Modifier
                        .padding(start = PREF_HORIZONTAL_PADDING,
                                 end = PREF_HORIZONTAL_PADDING,
                                 top = 4.dp,
                                 bottom = verticalPadding),
            )
        }
    }
}

@Composable
fun SecondaryClickablePreference(@SuppressLint("ModifierParameter") modifier: Modifier? = null,
                                 title: String,
                                 description: String = "",
                                 onClick: () -> Unit) {

    val verticalPadding = 16.dp

    Card(
            modifier = modifier ?: Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(PREF_HORIZONTAL_PADDING),
            colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
            ),
            onClick = onClick
    ) {
        Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = PREFERENCE_TITLE_SIZE,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(start = PREF_HORIZONTAL_PADDING,
                             end = PREF_HORIZONTAL_PADDING,
                             top = verticalPadding,
                             bottom = if (description.isNotEmpty()) 0.dp else verticalPadding),
        )
        if (description.isNotEmpty()) {
            Text(
                    text = description,
                    fontWeight = FontWeight.Normal,
                    fontSize = PREFERENCE_DESCRIPTION_SIZE,
                    modifier = Modifier
                        .padding(start = PREF_HORIZONTAL_PADDING,
                                 end = PREF_HORIZONTAL_PADDING,
                                 top = 4.dp,
                                 bottom = verticalPadding),
            )
        }
    }
}

@Composable
fun SecondaryHeader(title: String) {
    Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = PREF_HORIZONTAL_PADDING, end = PREF_HORIZONTAL_PADDING, top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun OtherApps(title: String, description: String, iconResId: Int, onClick: () -> Unit) {
    val verticalPadding = 16.dp
    val iconSize = 72.dp

    Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(PREF_HORIZONTAL_PADDING),
            colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
            ),
            onClick = onClick
    ) {
        Row(
                modifier = Modifier.padding(start = PREF_HORIZONTAL_PADDING, end = PREF_HORIZONTAL_PADDING, top = verticalPadding, bottom = verticalPadding),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(iconSize)
            )
            Column(
                    modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = PREFERENCE_TITLE_SIZE,
                        modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                        text = description,
                        fontWeight = FontWeight.Normal,
                        fontSize = PREFERENCE_DESCRIPTION_SIZE,
                        modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DescriptionPreference(description: String) {
    Column(
            modifier = Modifier
                .padding(start = PREF_HORIZONTAL_PADDING,
                         end = PREF_HORIZONTAL_PADDING,
                         top = 8.dp,
                         bottom = 8.dp)
    ) {
        Text(
                text = description,
                fontWeight = FontWeight.Normal,
                fontSize = PREFERENCE_DESCRIPTION_SIZE,
                modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
