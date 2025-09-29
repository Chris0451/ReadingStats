package com.project.readingstats.navigation

import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.project.readingstats.features.home.ui.components.HomeScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    start: Screen = Screen.Register
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = start.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
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
                    navController.navigate(Screen.Home.route) {
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
        composable(Screen.Home.route) {
            HomeScreen(onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0)
                }
            })
        }
    }
}