package com.project.readingstats.features.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import com.project.readingstats.features.shelves.domain.usecase.ObserveBookStatusUseCase
import com.project.readingstats.features.shelves.domain.usecase.SetBookStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeBookStatusUseCase: ObserveBookStatusUseCase
    private val setBookStatusUseCase: SetBookStatusUseCase
): ViewModel(){
    // Extract the volumeId from the navigation arguments
    private val volumeId: String = checkNotNull(savedStateHandle["volumeId"])
    // Observe the book status using the volumeId
    val status: Flow<ReadingStatus?> = observeBookStatusUseCase(volumeId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Set the book status to the given status and payload
    fun setExclusiveStatus(status: ReadingStatus, payload: UserBook?) {
        viewModelScope.launch {
            setBookStatusUseCase(volumeId, status, payload)
        }
    }
}