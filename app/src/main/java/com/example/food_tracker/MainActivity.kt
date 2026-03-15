package com.example.food_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*

import com.example.food_tracker.core.navigation.Screen
import com.example.food_tracker.core.ui.theme.LemonWhite
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.feature.home.HomeScreen
import com.example.food_tracker.feature.home.HomeViewModel
import com.example.food_tracker.feature.camera.CameraScreen
import com.example.food_tracker.feature.profile.ProfileScreen
import com.example.food_tracker.feature.profile.ProfileViewModel
import com.example.food_tracker.feature.onboarding.OnboardingScreen
import com.example.food_tracker.domain.usecase.AddFoodUseCase
import com.example.food_tracker.feature.addfood.AddFoodViewModel
import com.example.food_tracker.feature.dashboard.DashboardScreen
import kotlinx.coroutines.flow.take
import com.example.food_tracker.feature.stats.StatsScreen

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
    val context = LocalContext.current
    val userDataStore = remember { UserDataStore(context.applicationContext) }

    val repository = remember { FoodRepositoryImpl(context.applicationContext, userDataStore) }
    val homeViewModel = remember { HomeViewModel(repository, userDataStore) }
    val profileViewModel = remember { ProfileViewModel(userDataStore) }

    val addFoodUseCase = remember { AddFoodUseCase(repository) }
    val addFoodViewModel = remember { AddFoodViewModel(addFoodUseCase) }

    var startRoute by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userDataStore.userProfileFlow.take(1).collect { profile ->
            startRoute = if (profile.weight == 0.0) "onboarding" else "home"
        }
    }

    if (startRoute == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
        }
    } else {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            bottomBar = {
                if (
                    currentRoute == "home" ||
                    currentRoute == "dashboard" ||
                    currentRoute == "profile"
                ) {
                    BottomBar(navController)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = startRoute!!,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("onboarding") {
                    // FIX: Oper parameter userDataStore ke OnboardingScreen
                    OnboardingScreen(
                        userDataStore = userDataStore,
                        onFinished = {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                composable("home") {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        addFoodViewModel = addFoodViewModel
                    )
                }

                composable("dashboard") {
                    StatsScreen(homeViewModel)
                }

                composable("camera") {

                    CameraPermissionWrapper {

                        CameraScreen(

                            onImageCaptured = { bitmap ->

                                homeViewModel.processFoodPhoto(bitmap, context)

                                navController.navigate("home") {

                                    popUpTo("home") { inclusive = true }

                                }

                            },

                            onClose = { navController.popBackStack() }

                        )

                    }

                }

                composable("profile") { ProfileScreen(viewModel = profileViewModel) }
            }
        }
    }
}

@Composable
fun CameraPermissionWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }

    if (hasPermission) {
        content()
    } else {
        LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CAMERA) }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        Screen("home", Icons.Rounded.Home, "Home"),
        Screen("dashboard", Icons.Rounded.BarChart, "Stats"),
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
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}