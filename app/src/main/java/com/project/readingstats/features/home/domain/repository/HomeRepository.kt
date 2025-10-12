package com.project.readingstats.features.home.domain.repository

interface HomeRepository {
    suspend fun startBookTimer(userBookId: String)
    suspend fun setBookTimer(userBookId: String, duration: Long)
}