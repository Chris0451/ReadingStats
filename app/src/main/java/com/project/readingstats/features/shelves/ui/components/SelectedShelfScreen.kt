package com.project.readingstats.features.shelves.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.project.readingstats.R
import com.project.readingstats.features.shelves.ShelvesViewModel
import com.project.readingstats.features.shelves.UiShelfBook
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedShelfScreen(
    onOpenBookDetail: (UiShelfBook) -> Unit,
    viewModel: ShelvesViewModel = hiltViewModel(),
    onBack: () -> Unit
){
    val books by viewModel.books.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = viewModel.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ){ paddingValues ->
        if(books.isEmpty()){
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ){
                Text(text = "Nessun libro presente in lista")
            }
        }else{
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ){
                items(books.size, key = {books[it].id}){ index ->
                    ShelfBookRow(books[index]) { onOpenBookDetail(books[index]) }
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
        }
    }
}

@Composable
private fun ShelfBookRow(book: UiShelfBook, onClick: ()->Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick=onClick)
            .padding(horizontal=16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            BookThumb(url = book.thumbnail, cd = "Copertina del libro ${book.title}")
            Text(text = book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val subtitle = buildList {
                if(book.pageInReading!=null) book.pageCount?.let { add("${book.pageInReading} pagine lette su $it pagine") } else book.pageCount?.let {add("$it pagine")}
            }.joinToString(separator = " - ")
            if (subtitle.isNotBlank()){
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        ReadingProgressCircle(
            read = book.pageInReading,
            total = book.pageCount,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
@Composable
private fun BookThumb(url: String?, cd: String) {
    val shape = RoundedCornerShape(8.dp)
    val preview = LocalInspectionMode.current
    Box(Modifier.size(64.dp).clip(shape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (preview || url.isNullOrBlank()) {
            Image(painterResource(R.drawable.ic_book), cd, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Image(rememberAsyncImagePainter(url), cd, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
fun ReadingProgressCircle(
    read: Int?,              // pagine lette
    total: Int?,             // pagine totali
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    stroke: Dp = 6.dp,
    progressColor: Color? = null,
    trackColor: Color? = null
    ) {
    val pColor = progressColor ?: MaterialTheme.colorScheme.primary
    val tColor = trackColor ?: MaterialTheme.colorScheme.outlineVariant
    val safeTotal = if (total != null && total > 0) total else null
    val clampedRead = if (safeTotal != null) (read ?: 0).coerceIn(0, safeTotal) else 0
    val raw = if (safeTotal != null) clampedRead.toFloat() / safeTotal else 0f
    val progress by animateFloatAsState(targetValue = raw, label = "readingProgress")

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val strokePx = stroke.toPx()
            val canvasSize = this.size
            // inset per non tagliare il tratto al bordo della canvas
            val arcSize = Size(
                canvasSize.width - strokePx,
                canvasSize.height - strokePx
            )
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // traccia di fondo
            drawArc(
                color = tColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            // progresso
            drawArc(
                color = pColor,
                startAngle = -90f, // parte dall'alto
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        val percent = (progress * 100).roundToInt()
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}