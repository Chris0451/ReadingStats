package com.project.readingstats.features.shelves

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.catalog.domain.usecase.SearchBookUseCase
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.usecase.ObserveBooksByStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class UiShelfBook(
    val id: String,
    val title: String,
    val thumbnail: String?,
    val authors: List<String>,
    val categories: List<String>,
    val pageCount: Int?,
    val description: String?,
    val pageInReading: Int? = null,
    val isbn13: String?,
    val isbn10: String?
)

@HiltViewModel
class ShelvesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeBooksByStatus: ObserveBooksByStatusUseCase,
    private val searchBook: SearchBookUseCase
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
                    pageCount = it.pageCount,
                    description = it.description,
                    pageInReading = it.pageInReading,
                    isbn13 = it.isbn13,
                    isbn10 = it.isbn10
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun findBookByScan(raw: String): Book? = withContext(Dispatchers.IO) {
        val digits = raw.filter(Char::isDigit)
        val candidates = buildIsbnCandidates(digits)

        // 1) prova prima con query ISBN “pulite”
        for (c in candidates) {
            val res: List<Book> = searchBook("isbn:$c")
            res.firstOrNull()?.let { return@withContext it }
        }

        // 2) fallback: ricerca libera col raw (magari lo scanner ha letto altro testo)
        val fallback = searchBook(raw)
        return@withContext fallback.firstOrNull()
    }

    private fun buildIsbnCandidates(eanDigits: String): List<String> {
        val out = mutableListOf<String>()
        if (eanDigits.length == 13 && (eanDigits.startsWith("978") || eanDigits.startsWith("979"))) {
            out += eanDigits                 // sempre provare l'ISBN-13
            if (eanDigits.startsWith("978")) {
                toIsbn10OrNull(eanDigits)?.let { out += it } // solo 978 → ISBN-10
            }
        } else if (eanDigits.length == 10) {
            out += eanDigits                 // se mai arrivasse un 10 cifre
        }
        return out.distinct()
    }

    private fun toIsbn10OrNull(isbn13: String): String? {
        if (isbn13.length != 13 || !isbn13.startsWith("978")) return null
        val core = isbn13.substring(3, 12) // 9 cifre
        val sum = core.mapIndexed { idx, ch -> (10 - idx) * (ch - '0') }.sum()
        val checkVal = 11 - (sum % 11)
        val check = when (checkVal) { 10 -> 'X'; 11 -> '0'; else -> ('0' + checkVal) }
        return core + check
    }
}