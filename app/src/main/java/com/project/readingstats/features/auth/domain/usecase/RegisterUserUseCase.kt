package com.project.readingstats.features.auth.domain.usecase

import com.project.readingstats.features.auth.domain.repository.AuthRepository
import com.project.readingstats.features.auth.domain.repository.RegisterResult

class RegisterUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        name: String,
        surname: String,
        username: String,
        email: String,
        password: String
    ): RegisterResult = repository.register(name, surname, username, email, password)
}