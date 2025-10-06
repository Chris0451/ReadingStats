package com.project.readingstats.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.readingstats.core.ui.components.AppScaffold
import com.project.readingstats.core.ui.components.BottomDest
import com.project.readingstats.core.ui.components.NavBarComponent
import com.project.readingstats.features.auth.data.source.FirestoreUserDataSource
import com.project.readingstats.features.home.ui.components.HomeScreen
import com.project.readingstats.features.catalog.ui.components.CatalogScreen
import com.project.readingstats.features.profile.ui.components.ProfileRoot
import com.project.readingstats.features.profile.ui.components.ProfileScreen
import com.project.readingstats.features.profile.ui.components.ProfileViewModel
import com.project.readingstats.features.shelves.ui.components.ShelvesScreen
/*
*
* NavHost code for navigation through screens
* Login and register screens are handled in LoginRoute and RegistrationRoute, implemented in navigation.Routes.kt
* Main screen (AppScaffold + NavBar + Tab NavHost) is handled in AppNavHost
 */

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    start: Screen = Screen.Login, //Login screen by default if user not logged in
    isAuthenticated: Boolean //Check if user is logged in (managed in MainActivity)
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
        composable(Screen.Login.route) { //Login screen route
            LoginRoute(
                onLoginSuccess = { //Login successful
                    navController.navigate(Screen.Main.route) { //Destination call to Main Screen after login successful
                        popUpTo(Screen.Login.route) { inclusive = true } //Remove login screen after login successful
                        launchSingleTop = true //No main duplication in order to avoid multiple instances of Main screen
                    }
                },
                onRegisterClick = { //Register button clicked
                    navController.navigate(Screen.Register.route){ //Destination call to Register Screen from Login Screen
                        launchSingleTop = true //Does not return to registration screen if users come back after multiple clicks
                    }
                }
            )
        }
        composable(Screen.Register.route) { //Register screen route
            RegistrationRoute(
                onRegistered = { //Registration successful
                    navController.navigate(Screen.Main.route) { //Destination call to Main Screen after registration successful
                        popUpTo(Screen.Register.route) { inclusive = true } //Remove registration screen after registration successful
                        launchSingleTop = true //No Main duplication in order to avoid multiple instances of Main screen
                    }
                },
                onLoginClick = { //Login button clicked
                    navController.popBackStack()
                    navController.navigate(Screen.Login.route){
                        launchSingleTop = true //Does not return to login screen if users come back after multiple clicks
                    }
                }
            )
        }
        // ---- GRAFO MAIN (AppScaffold + NavBar + Tab NavHost) ----
        composable(Screen.Main.route) { //Main screen route
            val tabsNavController = rememberNavController()

            val profileViewModel = ProfileViewModel(
                firestoreUserDataSource = FirestoreUserDataSource(FirebaseFirestore.getInstance())
            )

            val user by profileViewModel.user.collectAsState()

            val onLogout: () -> Unit = { //Logout button function
                FirebaseAuth.getInstance().signOut() //Logout from Firebase
                navController.navigate(Screen.Login.route) { //Destination call to Login Screen after logout
                    popUpTo(Screen.Main.route) { inclusive = true } //Remove Main screen after logout
                    launchSingleTop = true //No Login duplication in order to avoid multiple instances of Login screen
                }
            }

            AppScaffold(
                bottomBar = { NavBarComponent(navController = tabsNavController) }
            ) { innerPadding ->
                NavHost(
                    navController = tabsNavController,
                    startDestination = BottomDest.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    // ---- GRAPH TAB NAVHOST ----
                    composable(BottomDest.Home.route) { HomeScreen(onLogout = onLogout) }
                    composable(BottomDest.Catalog.route) { CatalogScreen(onLogout = onLogout) }
                    composable(BottomDest.Books.route) { ShelvesScreen(onLogout = onLogout) }
                    composable(BottomDest.Profile.route) { ProfileScreen (user = user,profileViewModel = profileViewModel, onLogout = onLogout) }
                }
            }
        }
    }
}