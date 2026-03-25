package com.example.food_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.food_tracker.core.navigation.Screen
import com.example.food_tracker.core.ui.theme.LemonWhite
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.domain.usecase.AddFoodUseCase
import com.example.food_tracker.feature.addfood.AddFoodScreen
import com.example.food_tracker.feature.addfood.AddFoodViewModel
import com.example.food_tracker.feature.camera.CameraScreen
import com.example.food_tracker.feature.camera.CameraViewModel
import com.example.food_tracker.feature.camera.CameraViewModelFactory
import com.example.food_tracker.feature.home.HomeScreen
import com.example.food_tracker.feature.home.HomeViewModel
import com.example.food_tracker.feature.onboarding.OnboardingScreen
import com.example.food_tracker.feature.profile.ProfileScreen
import com.example.food_tracker.feature.profile.ProfileViewModel
import com.example.food_tracker.feature.stats.StatsScreen
import kotlinx.coroutines.flow.take

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
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            bottomBar = {
                if (currentRoute in listOf("home", "dashboard", "profile")) {
                    BottomBar(navController)
                }
            }
        ) { padding ->

            NavHost(
                navController = navController,
                startDestination = startRoute!!,
                modifier = Modifier.padding(padding)
            ) {

                // ONBOARDING
                composable("onboarding") {
                    OnboardingScreen(
                        userDataStore = userDataStore,
                        onFinished = {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                // ✅ ADD FOOD (FIX PARAM)
                composable("add_food?name={name}") { backStackEntry ->
                    val name = backStackEntry.arguments?.getString("name")

                    AddFoodScreen(
                        viewModel = addFoodViewModel,
                        prefillName = name
                    )
                }

                // HOME
                composable("home") {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        addFoodViewModel = addFoodViewModel,

                        // ✅ FIX: encode biar aman
                        onFoodClick = { food ->
                            val encoded = Uri.encode(food.name)
                            navController.navigate("add_food?name=$encoded")
                        },

                        onNavigateToAddFood = { name ->
                            val encoded = Uri.encode(name)
                            navController.navigate("add_food?name=$encoded")
                        }
                    )
                }

                composable("dashboard") {
                    StatsScreen(homeViewModel)
                }

                // CAMERA
                composable("camera") {
                    CameraPermissionWrapper {

                        val cameraViewModel: CameraViewModel = viewModel(
                            factory = CameraViewModelFactory(context.applicationContext)
                        )

                        CameraScreen(
                            viewModel = cameraViewModel,
                            homeViewModel = homeViewModel,

                            // ✅ FIX TOTAL (INI YANG BIKIN STUCK KEMARIN)
                            onImageCaptured = {
                            },

                            onNavigateToAddFood = { name ->
                                val encoded = Uri.encode(name)
                                navController.navigate("add_food?name=$encoded")
                            },

                            onClose = { navController.popBackStack() }
                        )
                    }
                }

                composable("profile") {
                    ProfileScreen(profileViewModel)
                }
            }
        }
    }
}

@Composable
fun CameraPermissionWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        hasPermission = it
    }

    if (hasPermission) {
        content()
    } else {
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.CAMERA)
        }
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

                selected = currentDestination?.hierarchy?.any {
                    it.route == screen.route
                } == true,

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