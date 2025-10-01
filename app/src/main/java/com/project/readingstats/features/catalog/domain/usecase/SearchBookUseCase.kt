package com.project.readingstats.features.catalog.domain.usecase

import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.catalog.domain.repository.CatalogRepository
import javax.inject.Inject

class SearchBookUseCase @Inject constructor(
    private val repository: CatalogRepository
){
    suspend operator fun invoke(raw: String, page: Int=0): List<Book> {
        val q = if(raw.any { it.isDigit() }) {
            val token = raw.filter { it.isLetterOrDigit() }
            "isbn:$token OR $raw"
        } else raw
        return repository.search(q, page)
    }
}