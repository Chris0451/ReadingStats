package com.project.readingstats.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.project.readingstats.core.ui.components.AppScaffold
import com.project.readingstats.core.ui.components.BottomDest
import com.project.readingstats.core.ui.components.NavBarComponent
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.home.ui.components.HomeScreen
import com.project.readingstats.features.catalog.ui.components.CatalogScreen
import com.project.readingstats.features.profile.ui.components.ProfileScreen
import com.project.readingstats.features.shelves.ui.components.SelectedShelfScreen
import com.project.readingstats.features.shelves.ui.components.ShelfType
import com.project.readingstats.features.shelves.ui.components.ShelvesScreen

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
        // ---- AUTH ----
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

        val safeNavigateUp: () -> Unit = { navController.navigateUp() }

        // ---- DETTAGLIO LIBRO (stack esterno) ----
        composable(
            route = Screen.BookDetail.route,
            arguments = listOf(navArgument("volumeId") { type = NavType.StringType }, navArgument("fromShelf") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val fromShelf: String? = backStackEntry.arguments?.getString("fromShelf")
            val previousHandle = navController.previousBackStackEntry?.savedStateHandle
            val routedBook = remember(previousHandle) { previousHandle?.get<Book>("book") }

            LaunchedEffect(routedBook) {
                if (routedBook != null) {
                    previousHandle?.remove<Book>("book")
                }
            }

            val onBack: () -> Unit = {
                if (fromShelf != null) {
                    navController.popBackStack(
                        Screen.ShelfBooks.createRoute(fromShelf),
                        inclusive = false
                    )
                }else{
                    safeNavigateUp()
                }
            }

            if (routedBook != null) {
                BookDetailScreenRoute(
                    book = routedBook,
                    onBack = onBack
                )
            } else {
                onBack()
            }
        }

        // ---- LISTA LIBRI SALVATI (stack esterno) ----
        composable(
            route = Screen.ShelfBooks.route,
            arguments = listOf(navArgument("shelfStatus") { type = NavType.StringType })
        ) { backStackEntry ->
            LaunchedEffect(backStackEntry) {
                backStackEntry.savedStateHandle.remove<Book>("book")
            }
            val shelfStatus = backStackEntry.arguments?.getString("shelfStatus") ?: "TO_READ"

            SelectedShelfScreen(
                onBack = { safeNavigateUp() },
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
                            description = null,
                            isbn13 = uiBook.isbn13,
                            isbn10 = uiBook.isbn10
                        )
                    )
                    navController.navigate(Screen.BookDetail.createRoute(uiBook.id, fromShelf = shelfStatus))
                }
            )
        }

        // ---- MAIN + BottomBar (stack esterno con NavHost interno) ----
        composable(Screen.Main.route) {
            val tabsNavController = rememberNavController()

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
                bottomBar = { NavBarComponent(navController = tabsNavController) }
            ) { innerPadding ->
                NavHost(
                    navController = tabsNavController,
                    startDestination = BottomDest.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(BottomDest.Home.route) { HomeScreen(onLogout = onLogout) }
                    composable(BottomDest.Catalog.route) { CatalogScreen(onOpenBook = onOpenBook) }
                    composable(BottomDest.Books.route) {
                        ShelvesScreen(
                            onShelfClick = { shelfType ->
                                navController.currentBackStackEntry?.savedStateHandle?.remove<Book>("book")
                                val status = when (shelfType) {
                                    ShelfType.TO_READ -> "TO_READ"
                                    ShelfType.READING -> "READING"
                                    ShelfType.READ -> "READ"
                                }
                                navController.navigate(Screen.ShelfBooks.createRoute(status))
                            },
                            onOpenBook = onOpenBook
                        )
                    }
                    composable(BottomDest.Profile.route) { ProfileScreen(onLogout = onLogout) }
                }
            }
        }
    }
}
