package com.project.readingstats.features.catalog.data.dto

data class VolumeResponse(
    val totalItems: Int? = 0,
    val items: List<VolumeItem> = emptyList<VolumeItem>()
)

data class VolumeItem(
    val id: String,
    val volumeInfo: VolumeInfo? = null
)

data class VolumeInfo(
    val title: String? = null,
    val authors: List<String>? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val pageCount: Int? = null,
    val categories: List<String>? = null,
    val imageLinks: ImageLinks? = null
)

data class ImageLinks(val thumbnail: String? = null, val smallThumbnail: String? = null)