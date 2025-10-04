package com.project.readingstats.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun VerticalScrollBar(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,
    minThumbHeight: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
) {
    val layoutInfo = listState.layoutInfo
    val visible = layoutInfo.visibleItemsInfo
    if (visible.isEmpty()) return

    val viewportPx = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset).toFloat()
    val first = visible.first()
    val last = visible.last()

    // Stima dell’altezza totale contenuto basata sugli item visibili
    val visibleSpanPx = (last.offset + last.size - first.offset).toFloat().coerceAtLeast(1f)
    val avgItemSpanPx = visibleSpanPx / visible.size
    val estimatedTotalPx = max(
        avgItemSpanPx * layoutInfo.totalItemsCount,
        viewportPx
    )

    // Altezza del pollice proporzionale al rapporto viewport/contenuto
    val thumbHeightPx = (viewportPx * (viewportPx / estimatedTotalPx))
        .coerceAtLeast(with(LocalDensity.current) { minThumbHeight.toPx() })

    // Offset del pollice proporzionale alla posizione dello scroll
    val firstIndex = layoutInfo.visibleItemsInfo.first().index
    val scrollPx = firstIndex * avgItemSpanPx + listState.firstVisibleItemScrollOffset
    val maxScrollPx = (estimatedTotalPx - viewportPx).coerceAtLeast(1f)
    val thumbTopPx = (scrollPx / maxScrollPx) * (viewportPx - thumbHeightPx)

    val radius = with(LocalDensity.current) { (thickness / 2).toPx() }

    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(thickness)
            .alpha(0.9f)
    ) {
        drawRoundRect( // track (facoltativo, molto leggero)
            color = color.copy(alpha = 0.15f),
            size = size,
            cornerRadius = CornerRadius(radius, radius)
        )
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(
                x = 0f,
                y = thumbTopPx.coerceIn(0f, size.height - thumbHeightPx)
            ),
            size = androidx.compose.ui.geometry.Size(width = size.width, height = thumbHeightPx),
            cornerRadius = CornerRadius(radius, radius)
        )
    }
}
