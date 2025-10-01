package com.project.readingstats.features.auth.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.auth.domain.repository.LoginResult
import com.project.readingstats.features.auth.domain.repository.RegisterResult
import com.project.readingstats.features.auth.domain.usecase.CheckUsernameAvailableUseCase
import com.project.readingstats.features.auth.domain.usecase.RegisterUserUseCase
import com.project.readingstats.features.auth.domain.usecase.LoginUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                password.length >= 6 &&
                password == confirmPassword &&
                (usernameAvailable == true)
}

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
){
    val canSubmit: Boolean
        get() = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                password.length >= 6
}
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val checkUsernameAvailableUseCase: CheckUsernameAvailableUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase
): ViewModel() {

    // --- Registration state ---
    private val _uiState = MutableStateFlow(RegistrationFormState())
    val uiState = _uiState.asStateFlow()

    private var usernameJob: Job? = null

    fun onNameChange(v: String){
        _uiState.value = _uiState.value.copy(name = v, error = null)
    }
    fun onSurnameChange(v: String){
        _uiState.value = _uiState.value.copy(surname = v, error = null)
    }
    fun onEmailChange(v: String){
        _uiState.value = _uiState.value.copy(email = v, error = null)
    }
    fun onPasswordChange(v: String){
        _uiState.value = _uiState.value.copy(password = v, error = null)
    }
    fun onConfirmPasswordChange(v: String){
        _uiState.value = _uiState.value.copy(confirmPassword = v, error = null)
    }

    fun onUsernameChange(v: String){
        val normalized = v.trim().lowercase()
        _uiState.value = _uiState.value.copy(username = normalized, usernameAvailable = null, error = null)
        usernameJob?.cancel()
        usernameJob = viewModelScope.launch {
            delay(250)
            if(normalized.length >= 4) {
                val available = checkUsernameAvailableUseCase(normalized)
                _uiState.value = _uiState.value.copy(usernameAvailable = available)
            }
        }
    }

    fun submitRegister() {
        val state = _uiState.value
        if(!state.canSubmit || state.isSubmitting) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            when (val result = registerUserUseCase(state.name, state.surname, state.username, state.email, state.password)) {
                is RegisterResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        success = true
                    )
                }
                is RegisterResult.Error -> {
                    val human =
                        when (result.code) {
                            "EMAIL_IN_USE" -> "Email già registrata."
                            "USERNAME_TAKEN" -> "Username non disponibile."
                            "NETWORK" -> "Problema di rete, riprova."
                            else -> "Auth error: ${result.code}\n${result.message ?: ""}" // <-- mostra codice reale (es. CONFIGURATION_NOT_FOUND)
                        }
                    _uiState.value = _uiState.value.copy(isSubmitting = false, error = human.trim())
                }
            }
        }
    }

    // --- Login state ---
    private val _loginState = MutableStateFlow(LoginFormState())
    val loginState = _loginState.asStateFlow()

    fun onLoginEmailChange(v: String){
        _loginState.value = _loginState.value.copy(email = v, error = null)
    }
    fun onLoginPasswordChange(v: String){
        _loginState.value = _loginState.value.copy(password = v, error = null)
    }

    fun submitLogin(){
        val state = _loginState.value
        if(!state.canSubmit || state.isSubmitting) return

        viewModelScope.launch {
            _loginState.value = state.copy(isSubmitting = true, error = null)
            when (val result = loginUserUseCase(state.email.trim(), state.password)) {
                is LoginResult.Success -> {
                    _loginState.value = _loginState.value.copy(isSubmitting = false, success = true)
                }
                is LoginResult.Error -> {
                    val msg = when (result.code) {
                        "INVALID_EMAIL"     -> "Email non valida."
                        "USER_NOT_FOUND",
                        "INVALID_CREDENTIALS",
                        "INVALID_LOGIN_CREDENTIALS",
                        "WRONG_PASSWORD"    -> "Email o Password non corretti."
                        "USER_DISABLED"     -> "Utente disabilitato."
                        "USER_PROFILE_MISSING"    -> "Profilo utente mancante. Completa la registrazione."
                        "NETWORK"                 -> "Problema di rete, riprova."
                        "CONFIGURATION_NOT_FOUND" -> "Configurazione Firebase non valida."
                        else -> result.message ?: "Auth error: ${result.code}"
                    }
                    _loginState.value = _loginState.value.copy(isSubmitting = false, error = msg.trim())
                }
            }
        }
    }

    fun clearLoginError(){
        _loginState.value = _loginState.value.copy(error = null)
    }
}