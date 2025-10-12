package com.project.readingstats.features.shelves.domain.usecase

import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBookStatusUseCase @Inject constructor(
    private val repository: ShelvesRepository
) {
    operator fun invoke(userBookId: String): Flow<ReadingStatus?> = repository.observeBookStatus(userBookId)
}