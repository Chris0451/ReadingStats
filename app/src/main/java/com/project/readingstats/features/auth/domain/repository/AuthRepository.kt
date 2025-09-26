package com.project.readingstats.features.auth.domain.repository

sealed interface RegisterResult {
    data object Success: RegisterResult
    data class Error(val code: String, val message: String?): RegisterResult
}

interface AuthRepository {
    suspend fun isUsernameAvailable(username: String): Boolean
    suspend fun register(
        name: String,
        surname: String,
        username: String,
        email: String,
        password: String
    ): RegisterResult
}