package app.simple.peri.ui.dialogs.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.activities.main.LocalDisplaySize
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.ConditionUtils.invert
import kotlin.math.max
import kotlin.math.min

@Composable
fun GridSpanSelectionDialog(onDismiss: () -> Unit, onNumberSelected: (Int) -> Unit) {
    val width = LocalDisplaySize.current.width
    val height = LocalDisplaySize.current.height
    val isDeviceLandscape = width > height

    AlertDialog(
            modifier = if (isDeviceLandscape) {
                Modifier
                    .fillMaxWidth(0.75f)
                    .fillMaxHeight(0.98F)
            } else {
                Modifier
            },
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(R.string.grid_span)) },
            text = {
                val forLandscape = remember(isDeviceLandscape) { mutableStateOf(isDeviceLandscape) }
                val storedNumber = if (forLandscape.value) {
                    MainComposePreferences.getGridSpanCountLandscape()
                } else {
                    MainComposePreferences.getGridSpanCountPortrait()
                }

                val selectedNumber = remember { mutableIntStateOf(storedNumber) }

                if (isDeviceLandscape) {
                    // Landscape device layout: preview on the left, controls on the right
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier
                            .weight(1.2f)
                            .padding(end = 12.dp)) {
                            GridPreviewSkeleton(
                                    forLandscape = forLandscape.value,
                                    columns = selectedNumber.intValue,
                                    deviceWidthPx = width,
                                    deviceHeightPx = height
                            )
                        }

                        VerticalDivider()

                        Column(modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)) {
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

                            HorizontalDivider(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))

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
                                    }
                            )
                        }
                    }
                } else {
                    // Portrait device layout: stacked vertically (existing behavior)
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

                        // Preview skeleton showing how the grid span will look
                        GridPreviewSkeleton(
                                forLandscape = forLandscape.value,
                                columns = selectedNumber.intValue,
                                deviceWidthPx = width,
                                deviceHeightPx = height
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider()

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
                                }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onDismiss() },
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true, usePlatformDefaultWidth = !isDeviceLandscape)
    )
}

@Composable
private fun GridPreviewSkeleton(
        forLandscape: Boolean,
        columns: Int,
        deviceWidthPx: Int,
        deviceHeightPx: Int,
) {
    val short = min(deviceWidthPx, deviceHeightPx).coerceAtLeast(1)
    val long = max(deviceWidthPx, deviceHeightPx)
    // Screen aspect ratio (width/height) respecting chosen orientation
    val screenAspect = if (forLandscape) long.toFloat() / short.toFloat() else short.toFloat() / long.toFloat()

    val containerShape = RoundedCornerShape(12.dp)
    val tileShape = RoundedCornerShape(6.dp)
    val bg = MaterialTheme.colorScheme.surfaceVariant
    val tile = MaterialTheme.colorScheme.surfaceContainerHigh

    Column(modifier = Modifier
        .fillMaxWidth()
    ) {
        Text(
                text = if (forLandscape) stringResource(R.string.landscape) else stringResource(R.string.portrait),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val longEdge = 220.dp
            val shortEdge = 120.dp
            Box(
                    modifier = Modifier
                        .size(
                                width = if (forLandscape) longEdge else shortEdge,
                                height = if (forLandscape) shortEdge else longEdge
                        )
                        .clip(containerShape)
                        .background(bg)
                        .padding(8.dp)
            ) {
                val rows = 16 // indicative number of rows
                LazyVerticalGrid(
                        columns = GridCells.Fixed(columns.coerceIn(1, 6)),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(columns.coerceIn(1, 6) * rows) {
                        Box(
                                modifier = Modifier
                                    .clip(tileShape)
                                    .background(tile)
                                    .aspectRatio(screenAspect)
                        )
                    }
                }
            }
        }
    }
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
