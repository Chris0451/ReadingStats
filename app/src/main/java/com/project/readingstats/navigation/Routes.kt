package com.project.readingstats.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.project.readingstats.features.auth.AuthViewModel
import com.project.readingstats.features.auth.ui.components.LoginScreen
import com.project.readingstats.features.auth.ui.components.RegistrationScreen
import com.project.readingstats.features.bookdetail.ui.components.BookDetailScreen
import com.project.readingstats.features.catalog.domain.model.Book

sealed interface Screen {
    val route: String

    data object Login : Screen { override val route = "login"}
    data object Register : Screen { override val route = "register"}
    data object Main : Screen { override val route = "main"}
    data object BookDetail : Screen {
        private const val ARG_VOLUME_ID = "volumeId"
        private const val ARG_FROM_SHELF = "fromShelf"
        override val route = "bookDetail/{$ARG_VOLUME_ID}?$ARG_FROM_SHELF={$ARG_FROM_SHELF}"
        fun createRoute(volumeId: String, fromShelf: String? = null): String =
            if(fromShelf != null) "bookDetail/$volumeId?$ARG_FROM_SHELF=$fromShelf"
            else "bookDetail/$volumeId"
    }

    data object ShelfBooks : Screen {
        const val ARG_STATUS = "shelfStatus" // TO_READ | READING | READ
        override val route = "shelfBooks/{$ARG_STATUS}"
        fun createRoute(status: String) = "shelfBooks/$status"

        val navArgs: List<NamedNavArgument> =
            listOf(navArgument(ARG_STATUS) { type = NavType.StringType })
    }

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

@Composable
fun BookDetailScreenRoute(
    book: Book,
    onBack: () -> Unit
){
    BookDetailScreen(
        book = book,
        onBack = onBack
    )
}