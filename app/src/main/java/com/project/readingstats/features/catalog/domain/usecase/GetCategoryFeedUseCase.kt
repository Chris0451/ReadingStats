package com.project.readingstats.features.catalog.domain.usecase

import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.catalog.domain.repository.CatalogRepository
import javax.inject.Inject

class GetCategoryFeedUseCase @Inject constructor(
    private val repo: CatalogRepository
) {
    suspend operator fun invoke(category: String, page: Int=0): List<Book> =
        repo.byCategory(category, page)
}