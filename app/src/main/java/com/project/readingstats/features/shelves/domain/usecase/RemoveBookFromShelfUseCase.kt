package com.project.readingstats.features.shelves.domain.usecase

import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import javax.inject.Inject

class RemoveBookFromShelfUseCase @Inject constructor(
    private val shelvesRepository: ShelvesRepository
) {
    suspend operator fun invoke(userBookId: String) {
        shelvesRepository.removeBook(userBookId)
    }
}