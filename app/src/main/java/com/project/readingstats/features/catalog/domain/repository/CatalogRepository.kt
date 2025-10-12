package com.project.readingstats.features.catalog.domain.repository

import com.project.readingstats.features.catalog.domain.model.Book

interface CatalogRepository {
    suspend fun search(query: String, page: Int, pageSize: Int = 21): List<Book>
    suspend fun byCategory(category: String, page: Int, pageSize: Int = 18): List<Book>
}