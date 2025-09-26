package com.project.readingstats.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import com.project.readingstats.features.auth.ui.components.AuthViewModel
import com.project.readingstats.features.auth.ui.components.RegistrationScreen

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
        composable(Screen.Register.route) {
            RegistrationRoute(
                onRegistered = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Register.route) {
                    popUpTo(0)
                }
            })
        }

    }

}

@Composable
fun RegistrationRoute(
    onRegistered: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    RegistrationScreen(
        viewModel = viewModel,
        onRegistered = onRegistered
    )
}

@Composable
private fun HomeScreen(onLogout: () -> Unit){
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ){
            Text(text = "Home Screen")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onLogout) {
                Text(text = "Logout")
            }
        }
    }
}