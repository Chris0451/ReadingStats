package com.project.readingstats.features.shelves.domain.repository

import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import kotlinx.coroutines.flow.Flow

interface ShelvesRepository {
    fun observeBooks(status: ReadingStatus): Flow<List<UserBook>>
    fun observeBookStatus(userBookId: String): Flow<ReadingStatus?>
    suspend fun setStatus(userBookId: String, payload: UserBook?, status: ReadingStatus)

    suspend fun removeBook(userBookId: String)
}