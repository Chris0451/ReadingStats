package com.project.readingstats.features.shelves.data

import com.project.readingstats.features.catalog.domain.model.Book

data class ShelvesModelDto(
    val uid: String = "",
    val name: String = "",
    val books: List<Book> = emptyList()
)
