package com.project.readingstats.features.shelves.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.project.readingstats.features.shelves.data.dto.UserBookDto
import com.project.readingstats.features.shelves.domain.model.UserBook
import kotlin.String

fun DocumentSnapshot.toUserBook(): UserBook? {
    val dto = toObject(UserBookDto::class.java) ?: return null
    return UserBook(
        id = id,
        volumeId = dto.volumeId,
        title = dto.title,
        authors = dto.authors,
        thumbnail = dto.thumbnail,
        categories = dto.categories,
        pageCount = dto.pageCount,
        description = dto.description,
        pageInReading = dto.pageInReading,
        status = dto.status,
        totalReadSeconds = dto.totalReadSeconds,
        isbn13 = dto.isbn10,
        isbn10 = dto.isbn13
    )
}

fun UserBook.toDto() = UserBookDto(
    volumeId = volumeId,
    title = title,
    thumbnail = thumbnail,
    authors = authors,
    categories = categories,
    pageCount = pageCount,
    description = description,
    pageInReading = pageInReading,
    status = status,
    isbn13 = isbn13,
    isbn10 = isbn10
)