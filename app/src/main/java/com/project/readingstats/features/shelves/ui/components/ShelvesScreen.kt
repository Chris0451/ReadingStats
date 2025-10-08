package com.project.readingstats.features.shelves.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class ShelfType { TO_READ, READING, READ }
private data class ShelfRowUi(
    val type: ShelfType,
    val title: String,
    val subtitle: String? = null,
    val icon: @Composable () -> Unit
)

@Composable
fun ShelvesScreen(
    counts: Map<ShelfType, Int> = emptyMap(),
    modifier: Modifier = Modifier,
    onShelfClick: (ShelfType) -> Unit = {}
) {
    // Dati statici della lista; i testi possono essere sostituiti con string resources.
    val rows = listOf(
        ShelfRowUi(
            type = ShelfType.TO_READ,
            title = "Da Leggere",
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null) }
        ),
        ShelfRowUi(
            type = ShelfType.READING,
            title = "In Lettura",
            icon = { Icon(Icons.Outlined.AutoStories, contentDescription = null) }
        ),
        ShelfRowUi(
            type = ShelfType.READ,
            title = "Letti",
            icon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) }
        )
    )

    Column(
        modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            rows.forEachIndexed { index, item ->
                ShelfRow(
                    title = item.title,
                    subtitle = item.subtitle,
                    count = counts[item.type],
                    leading = item.icon,
                    onClick = { onShelfClick(item.type) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "${item.title} ${counts[item.type] ?: 0} elementi"
                        }
                        .padding(horizontal = 12.dp)
                )
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    ) // allinea al testo
                }
            }
        }
    }
}

@Composable
private fun ShelfRow(
    title: String,
    subtitle: String? = null,
    count: Int? = null,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp),
            contentAlignment = Alignment.Center
        ) { leading() }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (count != null && count >= 0) {
            BadgedBox(badge = {
                Badge { Text(count.toString()) }
            }) {
                // elemento “ancora” per il badge: teniamo uno spacer
                Spacer(Modifier.size(1.dp))
            }
        }
    }
}