package com.project.readingstats.features.shelves.domain.usecase

import com.project.readingstats.features.shelves.domain.model.UserBook
import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserBookUseCase @Inject constructor(
    private val repo: ShelvesRepository
) {
    operator fun invoke(userBookId: String): Flow<UserBook?> = repo.observeUserBook(userBookId)
}