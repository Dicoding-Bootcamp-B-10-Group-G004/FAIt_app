package com.example.food_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

// IMPORT CORE & DATA
import com.example.food_tracker.core.navigation.Screen
import com.example.food_tracker.core.ui.theme.LemonWhite
import com.example.food_tracker.data.repository.FoodRepositoryImpl

// IMPORT FEATURE
import com.example.food_tracker.feature.home.HomeScreen
import com.example.food_tracker.feature.home.HomeViewModel
import com.example.food_tracker.feature.camera.CameraScreen
import com.example.food_tracker.feature.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF4CAF50),
                    background = LemonWhite,
                    surface = LemonWhite
                )
            ) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    FoodTrackerApp()
                }
            }
        }
    }
}

@Composable
fun FoodTrackerApp() {
    val navController = rememberNavController()

    // --- TAMBAHKAN INI UNTUK FIX ERROR 'homeViewModel' ---
    val context = LocalContext.current
    // Inisialisasi repository dan viewModel agar bisa dipassing ke screen
    val repository = remember { FoodRepositoryImpl(context) }
    val homeViewModel = remember { HomeViewModel(repository) }
    // ----------------------------------------------------

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            // Sekarang homeViewModel sudah didefinisikan di atas, jadi tidak error lagi
            composable("home") {
                HomeScreen(viewModel = homeViewModel)
            }

            composable("camera") {
                CameraScreen(
                    onImageCaptured = { /* nanti arahkan ke detail */ },
                    onClose = { navController.popBackStack() }
                )
            }

            composable("profile") { ProfileScreen() }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        Screen("home", Icons.Rounded.Home, "Home"),
        Screen("camera", Icons.Rounded.PhotoCamera, "Add"),
        Screen("profile", Icons.Rounded.Person, "Profile")
    )

    NavigationBar(containerColor = Color.White) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}