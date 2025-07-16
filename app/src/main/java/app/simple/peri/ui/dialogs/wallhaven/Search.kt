package app.simple.peri.ui.dialogs.wallhaven

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.simple.peri.R
import app.simple.peri.activities.main.LocalDisplaySize
import app.simple.peri.models.WallhavenFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
        onDismiss: () -> Unit,
        onSearch: (WallhavenFilter) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("111") }
    var purity by remember { mutableStateOf("100") }
    var atleast by remember { mutableStateOf("1920x1080") }
    var resolutionWidth by remember { mutableStateOf("1920") }
    var resolutionHeight by remember { mutableStateOf("1080") }
    var ratios by remember { mutableStateOf("portrait") }
    var sorting by remember { mutableStateOf("date_added") }
    var order by remember { mutableStateOf("desc") }

    val sortingOptions = listOf("date_added", "relevance", "random", "views", "favorites", "toplist")
    val catLabels = listOf(stringResource(R.string.general), stringResource(R.string.anime), stringResource(R.string.people))
    val orderLabels = listOf(stringResource(R.string.descending), stringResource(R.string.ascending))
    val portraitRatios = listOf("portrait", "landscape", "9x16", "3x4", "1x2")
    val landscapeRatios = listOf("portrait", "landscape", "16x9", "4x3", "21x9", "2x1")

    val ratioOptions = if (LocalDisplaySize.current.height > LocalDisplaySize.current.width) {
        portraitRatios
    } else {
        landscapeRatios
    }

    var ratioExpanded by remember { mutableStateOf(false) }
    var sortingExpanded by remember { mutableStateOf(false) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.search)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text(stringResource(R.string.query)) },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.category))
                    SingleChoiceSegmentedButtonRow {
                        catLabels.forEachIndexed { i, label ->
                            SegmentedButton(
                                    selected = categories[i] == '1',
                                    onClick = {
                                        categories = categories.toCharArray().also { it[i] = if (categories[i] == '1') '0' else '1' }.concatToString()
                                    },
                                    label = { Text(label) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                            index = i,
                                            count = catLabels.size)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                            text = stringResource(R.string.order)
                    )
                    SingleChoiceSegmentedButtonRow {
                        orderLabels.forEachIndexed { i, label ->
                            SegmentedButton(
                                    selected = order == if (i == 0) "desc" else "asc",
                                    onClick = {
                                        order = if (i == 0) "desc" else "asc"
                                    },
                                    label = { Text(label) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                            index = i,
                                            count = orderLabels.size)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                            expanded = sortingExpanded,
                            onExpandedChange = { sortingExpanded = !sortingExpanded }
                    ) {
                        OutlinedTextField(
                                value = sorting,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.sort)) },
                                trailingIcon = {
                                    Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor(
                                            type = MenuAnchorType.PrimaryNotEditable,
                                            enabled = true)
                                    .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                                expanded = sortingExpanded,
                                onDismissRequest = { sortingExpanded = false }
                        ) {
                            sortingOptions.forEach { option ->
                                DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            sorting = option
                                            sortingExpanded = false
                                        }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Resolution")
                    Row {
                        OutlinedTextField(
                                value = resolutionWidth,
                                onValueChange = { if (it.all(Char::isDigit)) resolutionWidth = it },
                                label = { Text("Width") },
                                modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                                value = resolutionHeight,
                                onValueChange = { if (it.all(Char::isDigit)) resolutionHeight = it },
                                label = { Text("Height") },
                                modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                            value = atleast,
                            onValueChange = { atleast = it },
                            label = { Text("At least (e.g., 1920x1080)") },
                            modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                            expanded = ratioExpanded,
                            onExpandedChange = { ratioExpanded = !ratioExpanded }
                    ) {
                        OutlinedTextField(
                                value = ratios,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ratios") },
                                trailingIcon = {
                                    Icon(
                                            imageVector = Icons.Filled.ArrowDropDown,
                                            contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor(
                                            type = MenuAnchorType.PrimaryEditable,
                                            enabled = true
                                    )
                                    .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                                expanded = ratioExpanded,
                                onDismissRequest = { ratioExpanded = false }
                        ) {
                            ratioOptions.forEach { option ->
                                DropdownMenuItem(
                                        text = { Text(option.replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            ratios = option
                                            ratioExpanded = false
                                        }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSearch(
                            WallhavenFilter(
                                    query = query,
                                    categories = categories,
                                    purity = purity,
                                    atleast = atleast,
                                    resolution = "${resolutionWidth}x${resolutionHeight}",
                                    ratios = ratios,
                                    sorting = sorting,
                                    order = order
                            )
                    )
                    onDismiss()
                }) {
                    Text("Search")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
    )
}