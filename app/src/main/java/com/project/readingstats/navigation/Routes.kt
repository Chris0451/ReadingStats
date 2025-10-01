package com.project.readingstats.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.readingstats.features.auth.AuthViewModel
import com.project.readingstats.features.auth.ui.components.LoginScreen
import com.project.readingstats.features.auth.ui.components.RegistrationScreen

sealed interface Screen {
    val route: String

    data object Login : Screen { override val route = "login"}
    data object Register : Screen { override val route = "register"}
    data object Main : Screen { override val route = "main"}

    data object Catalog : Screen { override val route = "catalog"}

    data object BookDetail : Screen { override val route = "bookDetail"}

    data object Profile : Screen {
        private const val ARG_USER_ID = "userId"
        override val route = "profile/{$ARG_USER_ID}"
        fun createRoute(userId: String) = "profile/$userId"
    }

}

@Composable
fun LoginRoute(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    LoginScreen(
        viewModel = viewModel,
        onLoginSuccess = onLoginSuccess,
        onRegisterClick = onRegisterClick
    )
}

@Composable
fun RegistrationRoute(
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    RegistrationScreen(
        viewModel = viewModel,
        onRegistered = onRegistered,
        onLoginClick = onLoginClick
    )
}