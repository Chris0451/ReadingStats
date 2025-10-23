package com.project.readingstats.features.shelves.domain.usecase

import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import javax.inject.Inject

class SetPageInReadingUseCase @Inject constructor(
    private val repository: ShelvesRepository
) {
    suspend operator fun invoke(userBookId: String, pageInReading: Int) {
        repository.setPageInReading(userBookId, pageInReading)
    }
}