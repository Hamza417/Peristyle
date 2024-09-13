package app.simple.peri.compose.commons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.nav.Routes

val COMMON_PADDING = 16.dp

@Composable
fun TopHeader(title: String, modifier: Modifier = Modifier, count: Int = 0, navController: NavController? = null, isSettings: Boolean = false) {
    Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 32.sp, // Set the font size
                modifier = Modifier.weight(1f), // Set the weight
                fontWeight = FontWeight.Bold, // Make the text bold
        )

        if (count > 0) {
            Text(
                    text = count.toString(),
                    textAlign = TextAlign.End,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Thin,
            )
        }

        if (isSettings.not()) {
            IconButton(
                    onClick = {
                        navController?.navigate(Routes.SETTINGS)
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                )
            }
        }
    }
}
