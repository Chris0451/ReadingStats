package com.project.readingstats.features.shelves.domain.usecase

import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import javax.inject.Inject

class SetPageCountUseCase @Inject constructor(
    private val repository: ShelvesRepository
) {
    suspend operator fun invoke(userBookId: String, pageCount: Int) {
        repository.setPageCount(userBookId, pageCount)
    }
}