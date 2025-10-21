package com.project.readingstats.features.auth.domain.model

import android.util.Patterns

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
){
    val canSubmit: Boolean
        get() = Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                password.length >= 6
}
