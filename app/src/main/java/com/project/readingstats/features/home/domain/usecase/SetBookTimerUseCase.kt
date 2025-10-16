package com.project.readingstats.features.home.domain.usecase

import com.project.readingstats.features.home.domain.repository.HomeRepository
import javax.inject.Inject

class SetBookTimerUseCase @Inject constructor(
    private val homeRepository: HomeRepository
){
    suspend operator fun invoke(bookId: String, startMillis: Long, endMillis: Long) {
        homeRepository.addReadingSession(bookId, startMillis, endMillis)
    }
}