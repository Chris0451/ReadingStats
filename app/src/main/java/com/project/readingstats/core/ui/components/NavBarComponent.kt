package com.project.readingstats.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class BottomDest(val route: String, val label: String, val icon: ImageVector){
    data object Books   : BottomDest("books", "Lista libri personale", Icons.AutoMirrored.Outlined.MenuBook)
    data object Catalog : BottomDest("catalog", "Catalogo libri", Icons.Outlined.Explore)
    data object Home    : BottomDest("home", "Home", Icons.Outlined.Home)
    data object Profile : BottomDest("profile", "Profilo", Icons.Outlined.Person)
}

private val BottomItems = listOf(
    BottomDest.Books,
    BottomDest.Catalog,
    BottomDest.Home,
    BottomDest.Profile
)

@Composable
fun NavBarComponent(
    navController: NavController,
    modifier: Modifier = Modifier
){
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier
    ) {
        BottomItems.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if(!selected){
                        navController.navigate(dest.route){
                            popUpTo(navController.graph.startDestinationId){
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(dest.icon, contentDescription = dest.label)
                },
                label = {
                    Text(text = dest.label)
                },
                alwaysShowLabel = true
            )
        }
    }
}