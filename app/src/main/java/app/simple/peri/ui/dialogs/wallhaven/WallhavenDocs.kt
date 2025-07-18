package app.simple.peri.ui.dialogs.wallhaven

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WallhavenDocsDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                Button(onClick = onDismissRequest) {
                    Text("Close")
                }
            },
            title = { Text("🔍 Search Guide") },
            text = {
                Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState())
                ) {
                    Text(
                            text = wallhavenDocsAnnotated(),
                    )
                }
            }
    )
}

fun wallhavenDocsAnnotated(): AnnotatedString = buildAnnotatedString {
    // Query
    append("\n")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("Query\n")
    pop()
    append("Search using any keyword like a topic or tag.\n")
    append("• Use ")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("-keyword")
    pop()
    append(" to exclude\n")
    append("• Use ")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("+keyword")
    pop()
    append(" to require\n")
    append("• Use ")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("@username")
    pop()
    append(" to find user uploads\n")
    append("• Use ")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("type:png / type:jpg")
    pop()
    append(" to filter by file type\n")
    append("• Use ")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("like:ID")
    pop()
    append(" to find similar wallpapers\n")

    // Category
    append("\n")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("Category\n")
    pop()
    append("Choose one or more types of wallpapers:\n")
    append("• General: Abstracts, landscapes, tech, etc.\n")
    append("• Anime: Anime-themed wallpapers\n")
    append("• People: Real or illustrated people\n")

    // Order
    append("\n")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("Order\n")
    pop()
    append("Set the order of results:\n")
    append("• Descending: Newest or most popular first\n")
    append("• Ascending: Oldest or least popular first\n")

    // Sort
    append("\n")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("Sort\n")
    pop()
    append("Choose how results are sorted:\n")
    append("• Options include relevance, upload date, popularity, etc.\n")

    // Resolution
    append("\n")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("Resolution\n")
    pop()
    append("Filter wallpapers by specific resolutions like 1920x1080 or 2560x1440.\n")
    append("Invalid resolutions will be ignored.\n")

    // At least
    append("\n")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("At least\n")
    pop()
    append("Only show wallpapers equal to or larger than the size you enter.\n")
    append("Example: 1920x1080\n")

    // Ratios
    append("\n")
    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    append("Ratios\n")
    pop()
    append("Filter wallpapers by screen shape:\n")
    append("• Common ratios: 16:9, 16:10, portrait, landscape\n")
}
