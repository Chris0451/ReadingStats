package com.project.readingstats.features.shelves

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.usecase.ObserveBooksByStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class UiShelfBook(
    val id: String,
    val title: String,
    val thumbnail: String?,
    val authors: List<String>,
    val categories: List<String>,
    val pageCount: Int?
)

@HiltViewModel
class ShelvesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeBooksByStatus: ObserveBooksByStatusUseCase,
): ViewModel(){
    private val status: ReadingStatus =
        runCatching { ReadingStatus.valueOf(checkNotNull(savedStateHandle["shelfStatus"]))}
            .getOrElse { ReadingStatus.TO_READ }

    val title: String = when (status){
        ReadingStatus.TO_READ -> "Da leggere"
        ReadingStatus.READING -> "In lettura"
        ReadingStatus.READ -> "Letti"
    }

    val books: StateFlow<List<UiShelfBook>> =
        observeBooksByStatus(status).map { list ->
            list.map {
                UiShelfBook(
                    id = it.id,
                    title = it.title,
                    thumbnail = it.thumbnail,
                    authors = it.authors,
                    categories = it.categories,
                    pageCount = it.pageCount
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}