package com.project.readingstats.features.bookdetail.ui.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.readingstats.R
import com.project.readingstats.features.bookdetail.BookDetailViewModel
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book,
    onBack: () -> Unit
){
    BackHandler { onBack() }
    val vm: BookDetailViewModel = hiltViewModel()
    val currentStatus by vm.status.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // listener eventi
    LaunchedEffect(Unit) {
        vm.events.collectLatest { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                msg,
                withDismissAction = false,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = book.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // HEADER: cover + info principali
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book.thumbnail)
                        .crossfade(true)
                        .listener(onError = { _, result ->
                            Log.e("BookDetail", "Image load error for ${book.title}", result.throwable)
                        })
                        .build(),
                    contentDescription = book.title,
                    modifier = Modifier
                        .size(width = 140.dp, height = 200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_book)
                )

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (book.authors.isNotEmpty()) {
                        Text(
                            text = book.authors.joinToString(),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!book.publishedDate.isNullOrBlank()) {
                        Text(
                            text = "Pubblicazione: "+book.publishedDate,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    PageCountText(book.pageCount)

                    BookStatusBar(
                        current = currentStatus,
                        onSet = { newStatus ->
                            // Payload minimo se il doc non esiste ancora
                            val payload = UserBook(
                                id = book.id,                // = volumeId
                                volumeId = book.id,
                                title = book.title,
                                thumbnail = book.thumbnail,
                                authors = book.authors,
                                categories = book.categories,
                                pageCount = book.pageCount,
                                status = newStatus           // il repo lo sovrascrive comunque
                            )
                            vm.onStatusIconClick(newStatus, payload)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            //INSERIMENTO ICONE CLICCABILI PER AGGIUNTA LIBRO


            ExpandableText(
                text = book.description ?: "Nessuna descrizione disponibile.",
            )
        }
    }
}

@Composable
private fun ExpandableText(
    text: String,
    collapsedLines: Int = 5
) {
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember (text, collapsedLines) { mutableStateOf(false) }
    Text(
        text = text,
        maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
        onTextLayout = { result ->
            if(!expanded) isOverflowing = result.hasVisualOverflow
        }
    )
    if(isOverflowing){
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Mostra meno" else "Mostra tutto")
        }
    }
}

@Composable
private fun PageCountText(pageCount: Int?) {
    val count = pageCount?.takeIf { it >= 1 } ?: return
    val nf = NumberFormat.getIntegerInstance()
    Text(
        text = "${nf.format(count)} pagine",
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun BookStatusBar(current: ReadingStatus?, onSet: (ReadingStatus) -> Unit){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        StatusIcon(
            checked = current == ReadingStatus.TO_READ,
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            label = "Da leggere",
            onClick = { onSet(ReadingStatus.TO_READ)}
        )
        StatusIcon(
            checked = current == ReadingStatus.READING,
            icon = Icons.Outlined.AutoStories,
            label = "In lettura",
            onClick = { onSet(ReadingStatus.READING)}
        )
        StatusIcon(
            checked = current == ReadingStatus.READ,
            icon = Icons.Outlined.CheckCircle,
            label = "Letto",
            onClick = { onSet(ReadingStatus.READ)}
        )
    }
}

@Composable
private fun StatusIcon(
  checked: Boolean,
  icon: ImageVector,
  label: String?,
  onClick: () -> Unit
){
    val color by animateColorAsState(
        if(checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        label = "statusTint"
    )

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color
        )
    }
}