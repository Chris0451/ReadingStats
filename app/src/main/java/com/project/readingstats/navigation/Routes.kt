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
import com.project.readingstats.features.profile.ui.components.Friend
import com.project.readingstats.features.profile.ui.components.FriendDetailsScreen
import com.project.readingstats.features.profile.ui.components.ListaAmici

sealed interface Screen {
    val route: String

    data object Login : Screen {
        override val route = "login"
    }

    data object Register : Screen {
        override val route = "register"
    }

    data object Main : Screen {
        override val route = "main"
    }

    data object BookDetail : Screen {
        private const val ARG_VOLUME_ID = "volumeId"
        private const val ARG_FROM_SHELF = "fromShelf"
        override val route = "bookDetail/{$ARG_VOLUME_ID}?$ARG_FROM_SHELF={$ARG_FROM_SHELF}"

        fun createRoute(volumeId: String, fromShelf: String? = null) =
            if (fromShelf != null) "bookDetail/$volumeId?$ARG_FROM_SHELF=$fromShelf"
            else "bookDetail/$volumeId"

        val navArgs: List<NamedNavArgument> = listOf(
            navArgument(ARG_VOLUME_ID) { type = NavType.StringType },
            navArgument(ARG_FROM_SHELF) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    }

    data object ShelfBooks : Screen {
        const val ARG_STATUS = "shelfStatus"
        override val route = "shelfBooks/{$ARG_STATUS}"

        fun createRoute(status: String) = "shelfBooks/$status"

        val navArgs: List<NamedNavArgument> = listOf(
            navArgument(ARG_STATUS) { type = NavType.StringType }
        )
    }

    data object Profile : Screen {
        override val route = "profile"
    }

    data object FriendsList : Screen {
        override val route = "friends_list"
    }

    data object FriendDetails : Screen {
        private const val ARG_FRIEND_UID = "friendUid"
        override val route = "friend_details/{$ARG_FRIEND_UID}"

        fun createRoute(friendUid: String) = "friend_details/$friendUid"

        val navArgs: List<NamedNavArgument> = listOf(
            navArgument(ARG_FRIEND_UID) { type = NavType.StringType }
        )
    }
}

@Composable
fun LoginRoute(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    LoginScreen(viewModel, onLoginSuccess, onRegisterClick)
}

@Composable
fun RegistrationRoute(
    viewModel: AuthViewModel = hiltViewModel(),
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit
) {
    RegistrationScreen(viewModel, onRegistered, onLoginClick)
}

@Composable
fun BookDetailRoute(
    book: Book,
    fromShelf: Boolean,
    onBack: () -> Unit
) {
    BookDetailScreen(book = book, fromShelf = fromShelf, onBack = onBack)
}

@Composable
fun FriendsListRoute(
    onBack: () -> Unit,
    onNavigateToFriendDetails: (Friend) -> Unit
) {
    ListaAmici(
        onBack = onBack,
        onNavigateToFriendDetails = onNavigateToFriendDetails
    )
}

@Composable
fun FriendDetailsRoute(
    friend: Friend,
    onBack: () -> Unit,
    onFriendRemoved: () -> Unit
) {
    FriendDetailsScreen(
        friend = friend,
        onBack = onBack,
        onRemoveFriend = onFriendRemoved
    )
}
