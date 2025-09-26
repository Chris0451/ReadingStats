package com.project.readingstats.features.auth.domain.usecase

import com.project.readingstats.features.auth.domain.repository.AuthRepository

class CheckUsernameAvailableUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String): Boolean =
        if (username.isNotBlank()) repository.isUsernameAvailable(username) else false
}