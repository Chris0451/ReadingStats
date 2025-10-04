package com.project.readingstats.features.shelves.domain.usecase

import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import javax.inject.Inject

class SetBookStatusUseCase @Inject constructor(
    private val repository: ShelvesRepository
){
    suspend operator fun invoke(userBookId: String, status: ReadingStatus, payload: UserBook? = null){
        repository.setStatus(userBookId, payload, status)
    }

}