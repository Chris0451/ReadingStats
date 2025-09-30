package com.project.readingstats.features.catalog.data

import androidx.compose.ui.input.key.Key
import retrofit2.http.GET
import retrofit2.http.Query
import com.project.readingstats.features.catalog.BuildConfig

interface GoogleBooksApi {
    @GET("volumes")
    suspend fun search(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int = 0,
        @Query("maxResults") maxResults: Int = 20,
        @Query("orderBy") orderBy: String? = "relevance",
        @Query("langRestrict") langRestrict: String? = "it",
        @Query("projection") projection: String? = "lite",
        @Query("key") key: String = BuildConfig.GOOGLE_BOOKS_API_KEY
    )
}