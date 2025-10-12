package com.project.readingstats.features.catalog.domain.usecase

import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.catalog.domain.repository.CatalogRepository
import javax.inject.Inject

class SearchBookUseCase @Inject constructor(
    private val repository: CatalogRepository
){
    private val isbnQueryPattern = Regex("^\\s*isbn:\\s*[0-9Xx-]+\\s*$")

    suspend operator fun invoke(raw: String, page: Int = 0): List<Book> {
        val t = raw.trim()
        if (isbnQueryPattern.matches(t)) {
            return repository.search(t, page)
        }

        val digits = t.filter(Char::isDigit)
        val q = when {
            digits.length == 13 && (digits.startsWith("978") || digits.startsWith("979")) ->
                "isbn:$digits OR $t"
            digits.length == 10 ->
                "isbn:$digits OR $t"
            else -> t
        }

        return repository.search(q, page)
    }
}