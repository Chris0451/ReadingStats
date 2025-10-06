package com.project.readingstats.features.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import com.project.readingstats.features.shelves.domain.usecase.ObserveBookStatusUseCase
import com.project.readingstats.features.shelves.domain.usecase.RemoveBookFromShelfUseCase
import com.project.readingstats.features.shelves.domain.usecase.SetBookStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeBookStatusUseCase: ObserveBookStatusUseCase,
    private val setBookStatusUseCase: SetBookStatusUseCase,
    private val removeBookFromShelfUseCase: RemoveBookFromShelfUseCase
): ViewModel(){
    // Extract the volumeId from the navigation arguments
    private val volumeId: String = checkNotNull(savedStateHandle["volumeId"])
    // Observe the book status using the volumeId
    val status: StateFlow<ReadingStatus?> =
        observeBookStatusUseCase(volumeId).
        stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events

    private fun label(s: ReadingStatus) = when (s) {
        ReadingStatus.TO_READ -> "Da leggere"
        ReadingStatus.READING -> "In lettura"
        ReadingStatus.READ    -> "Letti"
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
}