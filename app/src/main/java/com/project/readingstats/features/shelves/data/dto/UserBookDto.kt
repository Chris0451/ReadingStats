package com.project.readingstats.features.shelves.data.dto

import com.project.readingstats.features.shelves.domain.model.ReadingStatus

data class UserBookDto(
    val volumeId: String = "",
    val title: String = "",
    val thumbnail: String? = null,
    val authors: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val pageCount: Int? = null,
    val description: String? = null,
    val pageInReading: Int? = null,
    val status: ReadingStatus = ReadingStatus.TO_READ,
    val totalReadSeconds: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val isbn10: String? = null,
    val isbn13: String? = null
)
