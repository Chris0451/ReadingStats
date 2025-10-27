package com.project.readingstats.features.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.home.domain.model.HomeItemState
import com.project.readingstats.features.home.domain.model.PagesDialogState
import com.project.readingstats.features.home.domain.model.UiHomeBook
import com.project.readingstats.features.home.domain.model.HomeUiState
import com.project.readingstats.features.home.domain.repository.HomeRepository
import com.project.readingstats.features.home.domain.usecase.SetBookTimerUseCase
import com.project.readingstats.features.home.domain.usecase.StartBookTimerUseCase
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: HomeRepository,
    private val startTimer: StartBookTimerUseCase,
    private val setTimer: SetBookTimerUseCase,
) : ViewModel() {

    // bookId -> startMillis della sessione corrente
    private val running = MutableStateFlow<Map<String, Long>>(emptyMap())

    // bookId -> secondi trascorsi in questa sessione (contatore in-memory)
    private val ticking = MutableStateFlow<Map<String, Long>>(emptyMap())

    private var tickerJob: Job? = null

    // Dialog di aggiornamento pagine al termine della sessione
    private val _dialog = MutableStateFlow<PagesDialogState?>(null)

    // Libri in lettura
    private val booksFlow: Flow<List<UiHomeBook>> = repo.observeReadingBooks()

    // UI state combinato
    val uiState: StateFlow<HomeUiState> =
        combine(booksFlow, running, ticking, _dialog) { books, runningMap, tickMap, dialog ->
            val items = books.map { b ->
                HomeItemState(
                    book = b,
                    isRunning = runningMap.containsKey(b.id),
                    sessionStartMillis = runningMap[b.id],
                    sessionElapsedSec = tickMap[b.id] ?: 0L,
                    totalReadSec = b.totalReadSeconds
                )
            }
            HomeUiState(items = items, pagesDialog = dialog)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /**
     * Avvio sessione timer per un libro.
     * Incapsula eventuali suspend del use case nel viewModelScope.
     */
    fun onStart(book: UiHomeBook) {
        if (running.value.isNotEmpty() && !running.value.containsKey(book.id)) {
            Log.w("HomeVM", "Tentativo di avviare una seconda sessione: bloccato.")
            return
        }
        if (running.value.containsKey(book.id)) return

        val startedAt = startTimer() // <-- senza parametri
        running.update { it + (book.id to startedAt) }
        startTickerIfNeeded()
    }

    fun onStop(book: UiHomeBook) {
        val start = running.value[book.id] ?: return
        val end = System.currentTimeMillis()

        // 1) Ferma SUBITO il timer in UI
        running.update { it - book.id }
        ticking.update { it - book.id }
        stopTickerIfIdle()

        // 2) Mostra SUBITO la dialog per le pagine
        _dialog.value = PagesDialogState(
            book = book,
            currentRead = book.pageInReading ?: 0
        )

        // 3) Salva la sessione in background (anche se fallisce, la dialog resta aperta)
        viewModelScope.launch {
            runCatching { setTimer(book.id, start, end) }
                .onFailure { e ->
                    // opzionale: log/snackbar
                    Log.e("HomeVM", "Salvataggio sessione fallito per ${book.id}", e)
                }
        }
    }


    fun closeDialog() { _dialog.value = null }

    /**
     * Conferma delle pagine lette a fine sessione.
     * Se raggiunge/oltrepassa il totale, segna il libro come READ.
     */
    fun confirmPages(pages: Int) {
        val dialog = _dialog.value ?: return
        val total = dialog.book.pageCount ?: 0

        viewModelScope.launch {
            runCatching {
                repo.updatePagesRead(dialog.book.id, pages)

                if (total > 0 && pages >= total) {
                    repo.setStatus(
                        bookId = dialog.book.id,
                        status = ReadingStatus.READ,
                        payload = UserBook(
                            id = dialog.book.id,
                            volumeId = dialog.book.id,
                            title = dialog.book.title,
                            thumbnail = dialog.book.thumbnail,
                            authors = dialog.book.authors,
                            categories = emptyList(),
                            pageCount = dialog.book.pageCount,
                            pageInReading = pages,
                            description = dialog.book.description,
                            status = ReadingStatus.READ,
                            totalReadSeconds = dialog.book.totalReadSeconds,
                            isbn13 = dialog.book.isbn13,
                            isbn10 = dialog.book.isbn10
                        )
                    )
                }
            }
            _dialog.value = null
        }
    }

    // Avvia un ticker 1Hz solo quando serve
    private fun startTickerIfNeeded() {
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (isActive) {
                // se non ci sono timer attivi, esci
                val current = running.value
                if (current.isEmpty()) break

                val now = System.currentTimeMillis()
                val updates = current.mapValues { (_, start) -> (now - start) / 1000L }
                ticking.value = updates

                delay(1000)
            }
        }
    }

    private fun stopTickerIfIdle() {
        if (running.value.isEmpty()) {
            tickerJob?.cancel()
            tickerJob = null
        }
    }
}
