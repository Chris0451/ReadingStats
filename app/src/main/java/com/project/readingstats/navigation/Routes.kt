package com.project.readingstats.navigation

sealed interface Screen {
    val route: String

    data object Register : Screen { override val route = "register"}
    data object Home : Screen { override val route = "home"}

    data object Profile : Screen {
        private const val ARG_USER_ID = "userId"
        override val route = "profile/{$ARG_USER_ID}"
        fun createRoute(userId: String) = "profile/$userId"
    }

}