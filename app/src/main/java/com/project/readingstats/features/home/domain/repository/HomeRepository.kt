package com.project.readingstats.features.home.domain.repository

import com.project.readingstats.features.home.domain.model.UiHomeBook
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun observeReadingBooks(): Flow<List<UiHomeBook>>

    suspend fun addReadingSession(bookId: String, startMillis: Long, endMillis: Long)

    suspend fun updatePagesRead(bookId: String, pagesRead: Int)

    suspend fun setStatus(bookId: String, status: ReadingStatus, payload: UserBook?)
}