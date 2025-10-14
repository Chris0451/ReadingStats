package com.project.readingstats.features.home.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.readingstats.R
import com.project.readingstats.features.home.HomeViewModel
import com.project.readingstats.features.home.domain.model.HomeItemState
import com.project.readingstats.features.shelves.ui.components.ReadingProgressCircle
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.items.isEmpty()) {
        // placeholder quando non ci sono libri in lettura
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nessun libro in lettura")
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.items, key = { it.book.id }) { item ->
                ReadingCard(
                    item = item,
                    onStart = { viewModel.onStart(item.book) },
                    onStop = { viewModel.onStop(item.book) },
                )
            }
        }

        // dialog aggiornamento pagine alla fine della sessione
        state.pagesDialog?.let { dlg ->
            PagesDialog(
                book = dlg.book,
                initial = dlg.currentRead,
                onDismiss = { viewModel.closeDialog() },
                onConfirm = { pages -> viewModel.confirmPages(pages) }
            )
        }
    }
}

@Composable
private fun ReadingCard(
    item: HomeItemState,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Copertina
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.book.thumbnail?.takeUnless { it.isBlank() })
                    .crossfade(true)
                    .error(R.drawable.ic_book)
                    .fallback(R.drawable.ic_book)
                    .listener(onError = { _, r -> Log.e("Home", "cover error", r.throwable) })
                    .build(),
                contentDescription = item.book.title,
                modifier = Modifier
                    .width(160.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.height(12.dp))

            // Titolo
            Text(
                text = item.book.title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            // Progresso lettura + tempo totale
            val pages = item.book.pageCount ?: 0
            val read = item.book.pageInReading ?: 0
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val progress = if (pages > 0) read.toFloat() / pages else 0f
                ReadingProgressCircle(
                    read = item.book.pageInReading,
                    total = item.book.pageCount,
                    size = 56.dp,
                    stroke = 6.dp
                )
                Column {
                    Text(
                        text = if (pages > 0) "$read pagine lette su $pages" else "$read pagine lette",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val totalSeconds = item.totalWithSession
                    Text(
                        text = "Tempo totale: ${formatSeconds(totalSeconds)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pulsante timer
            val btnText = if (item.isRunning) "⏹️ Termina lettura" else "▶️ Riprendi lettura"
            Button(
                onClick = if (item.isRunning) onStop else onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(btnText)
            }

            // Timer live sotto al bottone quando in corso
            if (item.isRunning) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Sessione corrente: ${formatSeconds(item.sessionElapsedSec)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatSeconds(total: Long): String {
    val s = total.seconds
    val hh = s.inWholeHours
    val mm = (s - hh.hours).inWholeMinutes
    val ss = (s - hh.hours - mm.minutes).inWholeSeconds
    return "%02d:%02d:%02d".format(hh, mm, ss)
}
