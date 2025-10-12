package com.project.readingstats.features.shelves.domain.usecase

import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import javax.inject.Inject

class UpsertStatusBookUseCase @Inject constructor(
    private val repository: ShelvesRepository
){
    suspend operator fun invoke(
        userBook: UserBook,
        payload: UserBook?,
        status: ReadingStatus,
        pageCount: Int?,
        pageInReading: Int?
    ) = repository.upsertStatusBook(userBook, payload, status, pageCount, pageInReading)
}