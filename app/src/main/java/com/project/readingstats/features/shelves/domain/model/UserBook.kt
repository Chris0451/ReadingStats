package com.project.readingstats.features.shelves.domain.model

data class UserBook(
    val id: String,
    val volumeId: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?,
    val categories: List<String>,
    val pageCount: Int?,
    val description: String?,
    val pageInReading: Int? = null,
    val status: ReadingStatus,
    val isbn13: String? = null,
    val isbn10: String? = null
)
