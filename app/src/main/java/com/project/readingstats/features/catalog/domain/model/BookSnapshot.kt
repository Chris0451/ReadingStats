package com.project.readingstats.features.catalog.domain.model

data class BookSnapshot(
    val title: String,
    val authors: List<String>,
    val thumbnail: String?,
    val categories: List<String>,
    val publishedDate: String?,
    val pageCount: Int?,
    val description: String?
)
