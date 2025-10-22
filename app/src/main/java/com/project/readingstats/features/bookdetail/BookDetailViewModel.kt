package com.project.readingstats.features.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import com.project.readingstats.features.shelves.domain.usecase.ObserveBookStatusUseCase
import com.project.readingstats.features.shelves.domain.usecase.ObserveUserBookUseCase
import com.project.readingstats.features.shelves.domain.usecase.RemoveBookFromShelfUseCase
import com.project.readingstats.features.shelves.domain.usecase.SetBookStatusUseCase
import com.project.readingstats.features.shelves.domain.usecase.SetPageCountUseCase
import com.project.readingstats.features.shelves.domain.usecase.UpsertStatusBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeBookStatusUseCase: ObserveBookStatusUseCase,
    private val setBookStatusUseCase: SetBookStatusUseCase,
    private val removeBookFromShelfUseCase: RemoveBookFromShelfUseCase,
    private val setPageCountUseCase: SetPageCountUseCase,
    private val observeUserBook: ObserveUserBookUseCase,
    private val upsertStatusBookUseCase: UpsertStatusBookUseCase
): ViewModel(){
    // Extract the volumeId from the navigation arguments
    private val volumeId: String = checkNotNull(savedStateHandle["volumeId"])
    private val volumeIdFlow = MutableStateFlow(savedStateHandle.get<String>("volumeId"))
    fun bindVolume(volumeId: String){ volumeIdFlow.value = volumeId }


    @OptIn(ExperimentalCoroutinesApi::class)
    val status: StateFlow<ReadingStatus?> =
        volumeIdFlow.filterNotNull()
            .flatMapLatest { observeBookStatusUseCase(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val savedReadPages: StateFlow<Int?> =
        volumeIdFlow.filterNotNull()
            .flatMapLatest { observeUserBook(it) }   // Flow<UserBook?>
            .map { it?.pageInReading }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // (facoltativo) anche il pageCount se vuoi tenerlo aggiornato
    @OptIn(ExperimentalCoroutinesApi::class)
    val savedTotalPages: StateFlow<Int?> =
        volumeIdFlow.filterNotNull()
            .flatMapLatest { observeUserBook(it) }
            .map { it?.pageCount }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    private fun label(s: ReadingStatus) = when (s) {
        ReadingStatus.TO_READ -> "Da leggere"
        ReadingStatus.READING -> "In lettura"
        ReadingStatus.READ    -> "Letti"
    }

    /** Helper unico: write atomica stato + pagine (tot/lette) + metadati */
    fun setStatusWithPages(
        status: ReadingStatus,
        userBook: UserBook,
        // payload: puoi passare lo stesso userBook (dalla UI usiamo quello popolato con title/autori ecc.)
        payload: UserBook? = userBook,
        pagesRead: Int? = null,
        totalPages: Int? = null
    ) {
        viewModelScope.launch {
            runCatching {
                upsertStatusBookUseCase(
                    userBook = userBook,
                    payload = payload,
                    status = status,
                    pageCount = totalPages,
                    pageInReading = pagesRead
                )
            }.onSuccess {
                _events.tryEmit("Stato aggiornato: ${label(status)}")
            }.onFailure {
                _events.tryEmit(it.message ?: "Errore durante il salvataggio")
            }
        }
    }

    fun onStatusIconClick(clicked: ReadingStatus, payload: UserBook?){
        val id = volumeId
        val prev = status.value
        viewModelScope.launch{
            runCatching {
                if (prev == clicked) {
                    removeBookFromShelfUseCase(id)
                    _events.tryEmit("Libro rimosso dalla lista ${label(clicked)}")
                } else {
                    setBookStatusUseCase(id, clicked, payload)
                    if(prev == null){
                        _events.tryEmit("Libro aggiunto alla lista ${label(clicked)}")
                    } else {
                        _events.tryEmit("Stato del libro modificato da lista ${label(prev)} a ${label(clicked)}")
                    }
                }
            }.onFailure { exception ->
                _events.tryEmit(exception.message ?: "Errore durante il salvataggio")
            }
        }
    }

    fun updatePageCount(pageCount: Int){
        viewModelScope.launch {
            runCatching {
                setPageCountUseCase(volumeId, pageCount)
            }.onFailure { _events.tryEmit(it.message ?: "Errore durante l'aggiornamento del numero di pagine") }
        }
    }

}