package com.project.readingstats.features.catalog.data

import com.project.readingstats.features.catalog.data.dto.VolumeResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {
    @GET("volumes")
    suspend fun search(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int = 0,
        @Query("maxResults") maxResults: Int = 20,
        @Query("orderBy") orderBy: String? = "relevance",
        @Query("projection") projection: String? = "lite",
        @Query("langRestrict") langRestrict: String? = null,
        @Query("printType") printType: String? = "books",
        @Query("fields") fields: String? = null
    ) : VolumeResponse
}