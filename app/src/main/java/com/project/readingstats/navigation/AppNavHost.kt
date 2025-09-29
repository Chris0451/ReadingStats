package com.project.readingstats.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.project.readingstats.core.ui.components.AppScaffold
import com.project.readingstats.core.ui.components.BottomDest
import com.project.readingstats.core.ui.components.NavBarComponent
import com.project.readingstats.features.home.ui.components.HomeScreen
import com.project.readingstats.features.catalog.ui.components.CatalogScreen
import com.project.readingstats.features.profile.ui.components.ProfileScreen
import com.project.readingstats.features.shelves.ui.components.ShelvesScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    start: Screen = Screen.Login,
    isAuthenticated: Boolean
) {
    val navController = rememberNavController()

    LaunchedEffect(isAuthenticated) {
        if(isAuthenticated){
            navController.navigate(Screen.Main.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) Screen.Main.route else start.route,
        modifier = modifier
    ) {
        // ---- GRAFO AUTH ----
        composable(Screen.Login.route) {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route){
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegistrationRoute(
                onRegistered = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                    navController.navigate(Screen.Login.route){
                        launchSingleTop = true
                    }
                }
            )
        }
        // ---- GRAFO MAIN (AppScaffold + NavBar + Tab NavHost) ----
        composable(Screen.Main.route) {
            val tabsNavController = rememberNavController()

            val onLogout: () -> Unit = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Main.route) { inclusive = true }
                    launchSingleTop = true
                }
            }

            AppScaffold(
                bottomBar = { NavBarComponent(navController = tabsNavController) }
            ) { innerPadding ->
                NavHost(
                    navController = tabsNavController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(BottomDest.Home.route) { HomeScreen(onLogout = onLogout) }
                    composable(BottomDest.Catalog.route) { CatalogScreen(onLogout = onLogout) }
                    composable(BottomDest.Books.route) { ShelvesScreen(onLogout = onLogout) }
                    composable(BottomDest.Profile.route) { ProfileScreen(onLogout = onLogout) }
                }

            }
        }


    }
}