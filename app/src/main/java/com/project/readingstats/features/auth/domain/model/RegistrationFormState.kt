package com.project.readingstats.features.auth.domain.model

import android.util.Patterns

data class RegistrationFormState(
    val name: String = "",
    val surname: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val usernameAvailable: Boolean? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() &&
                surname.isNotBlank() &&
                username.length >= 4 &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                password.length >= 6 &&
                password == confirmPassword &&
                (usernameAvailable == true)
}
