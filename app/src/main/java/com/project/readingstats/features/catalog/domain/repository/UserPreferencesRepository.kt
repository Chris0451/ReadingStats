package com.project.readingstats.features.catalog.domain.repository

interface UserPreferencesRepository {
    suspend fun getSelectedCategories(uid: String): Set<String>
    suspend fun setSelectedCategories(uid: String, categories: Set<String>)
}