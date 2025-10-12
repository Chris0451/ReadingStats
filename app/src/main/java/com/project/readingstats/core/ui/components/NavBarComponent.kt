package com.project.readingstats.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomDest(val route: String, val label: String, val icon: ImageVector){
    data object Books   : BottomDest("books", "Scaffale", Icons.AutoMirrored.Outlined.MenuBook)
    data object Catalog : BottomDest("catalog", "Catalogo libri", Icons.Outlined.Explore)
    data object Home    : BottomDest("home", "Home", Icons.Outlined.Home)
    data object Profile : BottomDest("profile", "Profilo", Icons.Outlined.Person)
}

private val BottomItems = listOf(
    BottomDest.Books,    // Indice 0
    BottomDest.Catalog,  // Indice 1
    BottomDest.Home,     // Indice 2
    BottomDest.Profile   // Indice 3
)

@Composable
fun NavBarComponent(
    selectedTabIndex: Int, // Indice del tab selezionato (da HorizontalPager)
    onTabSelected: (Int) -> Unit, // Callback quando un tab viene selezionato
    modifier: Modifier = Modifier
){
    NavigationBar(
        modifier = modifier
    ) {
        BottomItems.forEachIndexed { index, dest ->
            val selected = selectedTabIndex == index
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if(!selected){
                        onTabSelected(index) // Chiama il callback con l'indice
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
