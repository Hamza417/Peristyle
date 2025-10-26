import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.ConditionUtils.invert

@Composable
fun GridSpanSelectionDialog(onDismiss: () -> Unit, onNumberSelected: (Int) -> Unit) {
    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(R.string.grid_span)) },
            text = {
                val forLandscape = remember { mutableStateOf(false) }
                val storedNumber = if (forLandscape.value) {
                    MainComposePreferences.getGridSpanCountLandscape()
                } else {
                    MainComposePreferences.getGridSpanCountPortrait()
                }

                val selectedNumber = remember { mutableIntStateOf(storedNumber) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Orientation toggle as segmented buttons
                    val segments = listOf(
                            stringResource(R.string.landscape),
                            stringResource(R.string.portrait)
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        segments.forEachIndexed { index, label ->
                            val selected = (index == 0 && forLandscape.value) || (index == 1 && forLandscape.value.invert())
                            SegmentedButton(
                                    selected = selected,
                                    onClick = {
                                        val landscape = index == 0
                                        forLandscape.value = landscape
                                        selectedNumber.intValue = if (landscape) {
                                            MainComposePreferences.getGridSpanCountLandscape()
                                        } else {
                                            MainComposePreferences.getGridSpanCountPortrait()
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = segments.size),
                                    colors = SegmentedButtonDefaults.colors(
                                            activeContainerColor = MaterialTheme.colorScheme.primary,
                                            activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                            inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                            ) {
                                Text(text = label)
                            }
                        }
                    }

                    HorizontalDivider(
                            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
                    )

                    // Span options as choice chips
                    SpanChoiceChips(
                            current = selectedNumber.intValue,
                            onSelected = { number ->
                                selectedNumber.intValue = number
                                if (forLandscape.value) {
                                    MainComposePreferences.setGridSpanCountLandscape(number)
                                } else {
                                    MainComposePreferences.setGridSpanCountPortrait(number)
                                }
                                onNumberSelected(number)
                                onDismiss()
                            }
                    )
                }
            },
            confirmButton = {
                Button(
                        onClick = { onDismiss() },
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}

@Composable
private fun SpanChoiceChips(current: Int, onSelected: (Int) -> Unit) {
    LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(6) { index ->
            val number = index + 1
            val selected = current == number
            FilterChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = selected,
                    onClick = { onSelected(number) },
                    label = { Text(text = number.toString()) },
                    leadingIcon = if (selected) {
                        { Icon(imageVector = Icons.Rounded.Check, contentDescription = null) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            labelColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            iconColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                            selected = selected,
                            enabled = true
                    )
            )
        }
    }
}
