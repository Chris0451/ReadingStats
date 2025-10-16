package com.project.readingstats.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.readingstats.core.ui.components.AppScaffold
import com.project.readingstats.core.ui.components.BottomDest
import com.project.readingstats.core.ui.components.HeaderComponent
import com.project.readingstats.core.ui.components.NavBarComponent
import com.project.readingstats.features.auth.data.source.FirestoreUserDataSource
import com.project.readingstats.features.profile.ui.components.ProfileViewModel
import com.project.readingstats.features.profile.ui.components.Friend
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.home.ui.components.HomeScreen
import com.project.readingstats.features.catalog.ui.components.CatalogScreen
import com.project.readingstats.features.profile.ui.components.ProfileScreen
import com.project.readingstats.features.shelves.ui.components.SelectedShelfScreen
import com.project.readingstats.features.shelves.ui.components.ShelfType
import com.project.readingstats.features.shelves.ui.components.ShelvesScreen

/*
*
* NavHost code for navigation through screens
* Login and register screens are handled in LoginRoute and RegistrationRoute, implemented in navigation.Routes.kt
* Main screen (AppScaffold + NavBar + Tab NavHost) is handled in AppNavHost with HorizontalPager for swipe navigation
 */

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    start: Screen = Screen.Login,
    isAuthenticated: Boolean
) {
    val navController = rememberNavController()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate(Screen.Main.route) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) Screen.Main.route else start.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route) { launchSingleTop = true }
                }
            )
        }

        composable(Screen.Register.route) {
            RegistrationRoute(
                onRegistered = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                    navController.navigate(Screen.Login.route) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Screen.BookDetail.route,
            arguments = Screen.BookDetail.navArgs
        ) { backStackEntry ->
            val previousHandle = navController.previousBackStackEntry?.savedStateHandle
            val routedBook = remember(previousHandle) { previousHandle?.get<Book>("book") }

            LaunchedEffect(routedBook) {
                if (routedBook != null) {
                    previousHandle?.remove<Book>("book")
                }
            }

            val onBack: () -> Unit = { navController.navigateUp() }

            if (routedBook != null) {
                BookDetailRoute(
                    book = routedBook,
                    fromShelf = backStackEntry.arguments?.getString("fromShelf") != null,
                    onBack = onBack
                )
            } else {
                onBack()
            }
        }

        composable(Screen.FriendsList.route) {
            FriendsListRoute(
                onBack = { navController.navigateUp() },
                onNavigateToFriendDetails = { friend ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("friend", friend)
                    navController.navigate(Screen.FriendDetails.createRoute(friend.uid))
                }
            )
        }

        composable(
            route = Screen.FriendDetails.route,
            arguments = Screen.FriendDetails.navArgs
        ) { backStackEntry ->
            val previousHandle = navController.previousBackStackEntry?.savedStateHandle
            val friend = remember(previousHandle) { previousHandle?.get<Friend>("friend") }

            LaunchedEffect(friend) {
                if (friend != null) {
                    previousHandle?.remove<Friend>("friend")
                }
            }

            val onBack: () -> Unit = { navController.navigateUp() }

            if (friend != null) {
                FriendDetailsRoute(
                    friend = friend,
                    onBack = onBack,
                    onFriendRemoved = { navController.navigateUp() }
                )
            } else {
                LaunchedEffect(Unit) { navController.navigateUp() }
            }
        }

        composable(Screen.Main.route) {
            val tabsNavController = rememberNavController()
            val backStack by tabsNavController.currentBackStackEntryAsState()
            val isShelfBooks = backStack?.destination?.route?.startsWith("shelfBooks") == true
            val currentChildRoute = backStack?.destination?.route
            val hideTopBar = currentChildRoute?.startsWith("shelfBooks") == true

            val profileViewModel = ProfileViewModel(
                firestoreUserDataSource = FirestoreUserDataSource(FirebaseFirestore.getInstance())
            )

            val user by profileViewModel.user.collectAsState()

            val onLogout: () -> Unit = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                    restoreState = false
                }
            }

            val onOpenBook: (Book) -> Unit = { book ->
                navController.currentBackStackEntry?.savedStateHandle?.set("book", book)
                navController.navigate(Screen.BookDetail.createRoute(book.id))
            }

            AppScaffold(
                topBar = if (hideTopBar) null else { { HeaderComponent() } },
                bottomBar = {
                    NavBarComponent(navController = tabsNavController)
                }
            ) { innerPadding ->
                val layoutDir = LocalLayoutDirection.current
                val contentPadding = if (isShelfBooks) {
                    PaddingValues(
                        start = innerPadding.calculateStartPadding(layoutDir),
                        top = 0.dp,
                        end = innerPadding.calculateEndPadding(layoutDir),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                } else innerPadding

                NavHost(
                    navController = tabsNavController,
                    startDestination = BottomDest.Home.route,
                    modifier = Modifier.padding(contentPadding)
                ) {
                    composable(BottomDest.Home.route) { HomeScreen() }
                    composable(BottomDest.Catalog.route) { CatalogScreen(onOpenBook = onOpenBook) }
                    composable(BottomDest.Books.route) {
                        ShelvesScreen(
                            onShelfClick = { shelfType ->
                                navController.currentBackStackEntry?.savedStateHandle?.remove<Book>("book")
                                val status = when (shelfType) {
                                    ShelfType.TO_READ -> "TO_READ"
                                    ShelfType.READING -> "READING"
                                    ShelfType.READ -> "read"
                                }
                                tabsNavController.navigate(Screen.ShelfBooks.createRoute(status)) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenBook = onOpenBook
                        )
                    }

                    composable(BottomDest.Profile.route) {
                        ProfileScreen(
                            user = user,
                            profileViewModel = profileViewModel,
                            onLogout = onLogout,
                            onNavigateToFriends = {
                                navController.navigate(Screen.FriendsList.route)
                            }
                        )
                    }

                    composable(
                        route = Screen.ShelfBooks.route,
                        arguments = Screen.ShelfBooks.navArgs
                    ) { entry ->
                        val shelfStatus = entry.arguments?.getString(Screen.ShelfBooks.ARG_STATUS) ?: "TO_READ"
                        SelectedShelfScreen(
                            onOpenBookDetail = { uiBook ->
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "book",
                                    Book(
                                        id = uiBook.id,
                                        title = uiBook.title,
                                        authors = uiBook.authors,
                                        thumbnail = uiBook.thumbnail,
                                        categories = uiBook.categories,
                                        publishedDate = null,
                                        pageCount = uiBook.pageCount,
                                        description = uiBook.description,
                                        isbn13 = uiBook.isbn13,
                                        isbn10 = uiBook.isbn10
                                    )
                                )
                                navController.navigate(
                                    Screen.BookDetail.createRoute(uiBook.id, fromShelf = shelfStatus)
                                )
                            },
                            onBack = { tabsNavController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
