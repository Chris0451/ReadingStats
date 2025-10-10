package com.project.readingstats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.auth.FirebaseAuth
import com.project.readingstats.navigation.AppNavHost
import com.project.readingstats.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val bg = MaterialTheme.colorScheme.background
            val nav = MaterialTheme.colorScheme.surface
            val darkStatusBarIcons = bg.luminance() > 0.5f
            val darkNavBarIcons = nav.luminance() > 0.5f

            SideEffect {
                window.statusBarColor = bg.toArgb()
                window.navigationBarColor = nav.toArgb()
                val controller = WindowInsetsControllerCompat(window, window.decorView)
                controller.isAppearanceLightStatusBars = darkStatusBarIcons
                controller.isAppearanceLightNavigationBars = darkNavBarIcons
            }
        }
        setContent {
            MaterialTheme{
                /*
                * Check if user is authenticated (logged in)
                * */
                val isAuthenticated by remember {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }
                /*
                * NavHost composable (AppNavHost) for navigation through screens
                * It starts from Login screen (Screen.Login) if user is not authenticated (isAuthenticated == false)
                */
                Surface{
                    AppNavHost(
                        start = Screen.Login,
                        isAuthenticated = isAuthenticated
                    )
                }
            }
        }
    }
}

