package com.project.readingstats.features.home.domain.model

data class UiHomeBook(
    val id: String,
    val title: String,
    val thumbnail: String?,
    val authors: List<String>,
    val pageCount: Int?,
    val description: String?,
    val pageInReading: Int?,
    val totalReadSeconds: Long,
    val isbn13: String?,
    val isbn10: String?
)
