package com.project.readingstats.features.shelves.domain.usecase

import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import javax.inject.Inject

class ObserveBooksByStatusUseCase @Inject constructor(
    private val repository: ShelvesRepository
) {
    operator fun invoke(status: ReadingStatus) = repository.observeBooks(status)
}