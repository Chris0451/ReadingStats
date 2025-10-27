package com.project.readingstats.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.project.readingstats.features.auth.AuthViewModel
import com.project.readingstats.features.auth.ui.components.LoginScreen
import com.project.readingstats.features.auth.ui.components.RegistrationScreen
import com.project.readingstats.features.bookdetail.ui.components.BookDetailScreen
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.profile.data.model.Friend
import com.project.readingstats.features.profile.ui.components.FriendDetailsScreen
import com.project.readingstats.features.profile.domain.manager.FriendsManager
import com.project.readingstats.features.profile.ui.components.ListaAmici
import com.project.readingstats.features.shelves.domain.model.UserBook


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
    BookDetailScreen(book = book, onBack = onBack)
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
    var friendBooks by remember { mutableStateOf<List<UserBook>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Carica i libri da FriendsManager
    LaunchedEffect(friend.uid) {
        FriendsManager.loadUserWithBooksIfFriend(friend.uid) { _, books ->
            friendBooks = books ?: emptyList()
            loading = false
        }
    }

    if (loading) {
        // Mostra un indicatore di caricamento
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        FriendDetailsScreen(
            friend = friend,
            friendBooks = friendBooks,
            onBack = onBack,
            onRemoveFriend = { uid: String, onComplete: () -> Unit ->
                FriendsManager.removeFriend(uid) { success, error ->
                    onComplete()
                    if (success) {
                        // naviga indietro o mostra Snackbar di successo
                        onBack()
                    } else {
                        // mostra Snackbar di errore
                    }
                }
            }
        )
    }
}