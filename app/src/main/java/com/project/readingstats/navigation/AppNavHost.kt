package com.project.readingstats.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.project.readingstats.features.profile.ui.components.ProfileScreen
import com.project.readingstats.features.profile.ui.components.ProfileViewModel
import com.project.readingstats.features.shelves.ui.components.ShelvesScreen
import kotlinx.coroutines.launch

/*
*
* NavHost code for navigation through screens
* Login and register screens are handled in LoginRoute and RegistrationRoute, implemented in navigation.Routes.kt
* Main screen (AppScaffold + NavBar + Tab NavHost) is handled in AppNavHost with HorizontalPager for swipe navigation
 */

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    start: Screen = Screen.Login, //Login screen by default if user not logged in
    isAuthenticated: Boolean //Check if user is logged in (managed in MainActivity)
) {
    val navController = rememberNavController()

    // Effetto per gestire la navigazione automatica al login
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
        // ---- GRAFO MAIN (AppScaffold + NavBar + HorizontalPager) ----
        composable(Screen.Main.route) { //Main screen route
            MainScreenWithPager()
        }
    }
}

@Composable
private fun MainScreenWithPager() {
    // Lista delle destinazioni nell'ordine in cui devono apparire nel pager
    val bottomDestinations = remember {
        listOf(
            BottomDest.Books,    // Indice 0
            BottomDest.Catalog,  // Indice 1
            BottomDest.Home,     // Indice 2
            BottomDest.Profile   // Indice 3
        )
    }

    // Stato del pager - inizia dalla Home (indice 2)
    val pagerState = rememberPagerState(
        initialPage = 2, // Home come pagina iniziale
        pageCount = { bottomDestinations.size }
    )

    // Coroutine scope per animazioni programmatiche
    val coroutineScope = rememberCoroutineScope()

    // Crea ViewModel e stato utente
    val profileViewModel = ProfileViewModel(
        firestoreUserDataSource = FirestoreUserDataSource(FirebaseFirestore.getInstance())
    )
    val user by profileViewModel.user.collectAsState()

    // Funzione di logout
    val onLogout: () -> Unit = remember {
        {
            FirebaseAuth.getInstance().signOut()
            // La navigazione al login sarà gestita dal LaunchedEffect in AppNavHost
        }
    }

    AppScaffold(
        bottomBar = {
            NavBarComponent(
                selectedTabIndex = pagerState.currentPage, // Passa l'indice corrente
                onTabSelected = { index ->
                    // Anima verso la pagina selezionata
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    ) { innerPadding ->

        // HorizontalPager per la navigazione con swipe
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { pageIndex ->

            // Mostra la schermata corrispondente alla pagina corrente
            when (bottomDestinations[pageIndex]) {
                BottomDest.Books -> {
                    ShelvesScreen(onLogout = onLogout)
                }
                BottomDest.Catalog -> {
                    CatalogScreen(onLogout = onLogout)
                }
                BottomDest.Home -> {
                    HomeScreen(onLogout = onLogout)
                }
                BottomDest.Profile -> {
                    ProfileScreen(
                        user = user,
                        profileViewModel = profileViewModel,
                        onLogout = onLogout
                    )
                }
            }
        }
    }
}
