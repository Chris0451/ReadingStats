package com.project.readingstats.features.auth.domain.repository

import com.project.readingstats.features.auth.data.model.UserModelDto

sealed interface RegisterResult {
    data object Success: RegisterResult
    data class Error(val code: String, val message: String?): RegisterResult
}

sealed interface LoginResult {
    data object Success: LoginResult
    data class Error(val code: String, val message: String?): LoginResult
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
    suspend fun getCurrentUserProfile(): UserModelDto?

    suspend fun login(email: String, password: String): LoginResult
}