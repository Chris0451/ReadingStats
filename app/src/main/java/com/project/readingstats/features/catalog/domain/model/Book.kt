package com.project.readingstats.features.catalog.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val thumbnail: String?,
    val categories: List<String>,
    val publishedDate: String?,
    val pageCount: Int?,
    val description: String?,
    val isbn13: String? = null,
    val isbn10: String? = null
): Parcelable
