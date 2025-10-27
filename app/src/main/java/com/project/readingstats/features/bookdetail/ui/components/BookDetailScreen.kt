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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.readingstats.R
import com.project.readingstats.features.bookdetail.BookDetailViewModel
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat

enum class ReadingFlowMode { START, UPDATE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book,
    onBack: () -> Unit
){
    BackHandler { onBack() }
    val vm: BookDetailViewModel = hiltViewModel()
    val savedTotal by vm.savedTotalPages.collectAsStateWithLifecycle()
    val currentStatus by vm.status.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var readingMode by remember { mutableStateOf<ReadingFlowMode?>(null) }
    var readingPayload by remember { mutableStateOf<UserBook?>(null) }

    val scope = rememberCoroutineScope()

    var totalOnlyFor by remember { mutableStateOf<ReadingStatus?>(null) }
    var totalOnlyValue by remember { mutableStateOf("") }
    var totalOnlyError by remember { mutableStateOf<String?>(null) }

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
    LaunchedEffect(book.id) { vm.bindVolume(book.id) }

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
                        .data(book.thumbnail?.takeUnless { it.isBlank() })
                        .crossfade(true)
                        .fallback(R.drawable.ic_book)
                        .error(R.drawable.ic_book)
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
                            val payload = UserBook(
                                id = book.id,
                                volumeId = book.id,
                                title = book.title,
                                thumbnail = book.thumbnail,
                                authors = book.authors,
                                categories = book.categories,
                                pageCount = book.pageCount,
                                description = book.description,
                                pageInReading = null,
                                status = newStatus,
                                isbn13 = book.isbn13,
                                isbn10 = book.isbn10
                            )

                            when (newStatus) {
                                ReadingStatus.READING -> {
                                    readingPayload = payload
                                    readingMode = if (currentStatus != ReadingStatus.READING)
                                        ReadingFlowMode.START else ReadingFlowMode.UPDATE
                                }
                                ReadingStatus.TO_READ -> {
                                    // toggle off: se è già TO_READ → rimuovi
                                    if (currentStatus == ReadingStatus.TO_READ) {
                                        vm.onStatusIconClick(ReadingStatus.TO_READ, null) // trigger rimozione legacy
                                        return@BookStatusBar
                                    }
                                    // se manca il totale → mostra SOLO dialog totali
                                    val total = savedTotal ?: book.pageCount
                                    if (total == null || total <= 0) {
                                        readingPayload = payload
                                        totalOnlyFor = ReadingStatus.TO_READ
                                        totalOnlyValue = ""
                                        totalOnlyError = null
                                    } else {
                                        // write atomica: stato + pagesRead=0
                                        vm.setStatusWithPages(ReadingStatus.TO_READ, payload, payload, pagesRead = 0, totalPages = null)
                                    }
                                }
                                ReadingStatus.READ -> {
                                    // toggle off: se è già READ → rimuovi
                                    if (currentStatus == ReadingStatus.READ) {
                                        vm.onStatusIconClick(ReadingStatus.READ, null) // trigger rimozione legacy
                                        return@BookStatusBar
                                    }
                                    val total = savedTotal ?: book.pageCount
                                    if (total == null || total <= 0) {
                                        // SOLO dialog totali (poi salveremo tot = lette = n)
                                        readingPayload = payload
                                        totalOnlyFor = ReadingStatus.READ
                                        totalOnlyValue = ""
                                        totalOnlyError = null
                                    } else {
                                        // write atomica: stato + pagesRead = total
                                        vm.setStatusWithPages(ReadingStatus.READ, payload, payload, pagesRead = total, totalPages = null)
                                    }
                                }
                            }

                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            ExpandableText(
                text = book.description ?: "Nessuna descrizione disponibile.",
            )

            if (readingMode != null && readingPayload != null) {
                val savedTotal: State<Int?> = vm.savedTotalPages.collectAsStateWithLifecycle()
                ReadingFlowDialogs(
                    vm = vm,
                    mode = readingMode!!,
                    apiPageCount = savedTotal.value ?: book.pageCount,
                    payload = readingPayload!!,
                    onClose = {
                        readingMode = null
                        readingPayload = null
                    },
                    onReachedTotal = {
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(
                                message = "Hai raggiunto il totale. Tocca l'icona \"Letto\" per completare.",
                                actionLabel = "OK",
                                withDismissAction = true,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }

            if (totalOnlyFor != null && readingPayload != null) {
                TotalPagesDialog(
                    value = totalOnlyValue,
                    onValue = { s ->
                        totalOnlyValue = s.filter { it.isDigit() }
                        val n = totalOnlyValue.toIntOrNull()
                        totalOnlyError = when {
                            totalOnlyValue.isBlank() -> "Inserisci un numero"
                            n == null || n <= 0 -> "Deve essere > 0"
                            else -> null
                        }
                    },
                    onDismiss = {
                        totalOnlyFor = null
                        readingPayload = null
                    },
                    onConfirm = {
                        val n = totalOnlyValue.toIntOrNull()
                        if (n != null && n > 0) {
                            when (totalOnlyFor) {
                                ReadingStatus.TO_READ -> vm.setStatusWithPages(
                                    status = ReadingStatus.TO_READ,
                                    userBook = readingPayload!!,
                                    payload = readingPayload!!,
                                    pagesRead = 0,
                                    totalPages = n
                                )

                                ReadingStatus.READ -> vm.setStatusWithPages(
                                    status = ReadingStatus.READ,
                                    userBook = readingPayload!!,
                                    payload = readingPayload!!,
                                    pagesRead = n,   // = totale
                                    totalPages = n
                                )

                                else -> {}
                            }
                            totalOnlyFor = null
                            readingPayload = null
                        }
                    },
                    isError = totalOnlyError != null,
                    supportingText = totalOnlyError
                )
            }

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

@Composable
private fun ReadingFlowDialogs(
    vm: BookDetailViewModel,
    mode: ReadingFlowMode,
    apiPageCount: Int?,
    payload: UserBook,
    onClose: () -> Unit,
    onReachedTotal: () -> Unit
) {
    // START: se pageCount mancante → chiedilo; poi chiedi pages read
    // UPDATE: chiedi solo pages read (non cambiare stato qui)
    val savedRead by vm.savedReadPages.collectAsStateWithLifecycle()

    val showTotalDialog = remember(mode, apiPageCount) {
        mutableStateOf(mode == ReadingFlowMode.START && (apiPageCount == null || apiPageCount <= 0))
    }
    val showReadDialog  = remember(mode, apiPageCount) {
        mutableStateOf(mode == ReadingFlowMode.UPDATE || (apiPageCount != null && apiPageCount > 0))
    }

    val totalPages = remember(apiPageCount) { mutableStateOf(apiPageCount?.toString() ?: "") }
    val initialRead = when(mode){
        ReadingFlowMode.UPDATE -> savedRead?.toString() ?: ""
        ReadingFlowMode.START  -> ""
    }

    val readPages  = remember(mode, savedRead) { mutableStateOf(initialRead) }

    val totalError = remember { mutableStateOf<String?>(null) }
    val readError  = remember { mutableStateOf<String?>(null) }

    if (showTotalDialog.value) {
        TotalPagesDialog(
            value = totalPages.value,
            onValue = { v ->
                totalPages.value = v.filter { it.isDigit() }
                totalError.value = when {
                    totalPages.value.isEmpty() -> "Inserisci un numero"
                    totalPages.value.toIntOrNull()?.let { it <= 0 } == true -> "Deve essere > 0"
                    else -> null
                }
            },
            onDismiss = {
                showTotalDialog.value = false
                onClose() // abbandona il flusso
            },
            onConfirm = {
                val count = totalPages.value.toIntOrNull()
                if (count != null && count > 0) {
                    vm.updatePageCount(count)
                    showTotalDialog.value = false
                    showReadDialog.value = true
                }
            },
            isError = totalError.value != null,
            supportingText = totalError.value
        )
    }

    // 2) Dialog pagine lette (START e UPDATE)
    if (showReadDialog.value) {
        val max = totalPages.value.toIntOrNull() ?: apiPageCount
        ReadPagesDialog(
            value = readPages.value,
            max = max,
            previousRead = savedRead,
            onValue = { v ->
                readPages.value = v.filter { it.isDigit() }
                val n = readPages.value.toIntOrNull()
                readError.value = when {
                    readPages.value.isEmpty() -> "Inserisci un numero"
                    n == null -> "Valore non valido"
                    n < 1 -> "Devi aver letto almeno una pagina"
                    max != null && n > max -> "Non può superare $max"
                    else -> null
                }
            },
            onDismiss = {
                showReadDialog.value = false
                onClose()
            },
            onConfirm = {
                val n = readPages.value.toIntOrNull()
                val valid = n != null && n >= 1 && (max == null || n <= max)
                if (valid) {
                    val totalFromDialog = totalPages.value.toIntOrNull()?.takeIf { it > 0 } // START quando hai chiesto il totale
                    vm.setStatusWithPages(
                        status = ReadingStatus.READING,
                        userBook = payload,
                        payload = payload,
                        pagesRead = n!!,
                        totalPages = totalFromDialog
                    )
                    showReadDialog.value = false
                    if (mode == ReadingFlowMode.UPDATE && max != null && n == max) onReachedTotal()
                    onClose()
                }
            },
            isError = readError.value != null,
            supportingText = readError.value
        )
    }
}


