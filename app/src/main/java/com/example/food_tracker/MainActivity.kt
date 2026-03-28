package com.example.food_tracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.food_tracker.core.navigation.Screen
import com.example.food_tracker.core.ui.theme.FoodTrackerTheme
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.data.local.database.AppDatabase
import com.example.food_tracker.data.repository.AppPreferencesRepository
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.data.repository.ProfileRepository
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.domain.model.TrackedFood
import com.example.food_tracker.domain.usecase.*
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
import com.example.food_tracker.feature.stats.StatsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val userDataStore = remember { UserDataStore(context.applicationContext) }
            val appPreferencesRepository = remember { AppPreferencesRepository(userDataStore) }
            val getAppPreferencesUseCase = remember { GetAppPreferencesUseCase(appPreferencesRepository) }
            
            val appPreferences by getAppPreferencesUseCase().collectAsState(initial = null)
            
            LaunchedEffect(appPreferences?.languageCode) {
                appPreferences?.languageCode?.let { lang ->
                    updateLocale(this@MainActivity, lang)
                }
            }

            FoodTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FoodTrackerApp(userDataStore, appPreferencesRepository)
                }
            }
        }
    }

    private fun updateLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

@Composable
fun FoodTrackerApp(
    userDataStore: UserDataStore,
    appPreferencesRepository: AppPreferencesRepository
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "food_tracker_db"
        ).fallbackToDestructiveMigration().build()
    }

    val repository = remember { FoodRepositoryImpl(context.applicationContext, userDataStore, db.foodDao) }
    val profileRepository = remember { ProfileRepository(userDataStore, repository) }

    val getDietHistoryUseCase = remember { GetDietHistoryUseCase(repository) }
    val getAllDietHistoriesUseCase = remember { GetAllDietHistoriesUseCase(repository) }
    val deleteTrackedFoodUseCase = remember { DeleteTrackedFoodUseCase(repository) }
    
    val setLanguageUseCase = remember { SetLanguageUseCase(appPreferencesRepository) }
    val getAppPreferencesUseCase = remember { GetAppPreferencesUseCase(appPreferencesRepository) }

    val homeViewModel = remember { HomeViewModel(repository, getDietHistoryUseCase, deleteTrackedFoodUseCase) }
    val statsViewModel = remember { StatsViewModel(getAllDietHistoriesUseCase, userDataStore) }
    val profileViewModel = remember { ProfileViewModel(profileRepository, setLanguageUseCase, getAppPreferencesUseCase) }

    val getFoodResultsUseCase = remember { GetFoodResultsUseCase(repository) }
    val addFoodUseCase = remember { AddFoodUseCase(repository) }
    val addFoodViewModel = remember { AddFoodViewModel(addFoodUseCase, getFoodResultsUseCase) }

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
                        profileRepository = profileRepository,
                        onFinished = {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    )
                }

                // ADD FOOD
                composable(
                    route = "add_food?name={name}&meal={meal}&id={id}&date={date}",
                    arguments = listOf(
                        navArgument("name") { nullable = true },
                        navArgument("meal") { defaultValue = "Lunch" },
                        navArgument("id") { nullable = true },
                        navArgument("date") { nullable = true }
                    )
                ) { backStackEntry ->
                    val name = backStackEntry.arguments?.getString("name")
                    val meal = backStackEntry.arguments?.getString("meal") ?: "Lunch"
                    val id = backStackEntry.arguments?.getString("id")
                    val date = backStackEntry.arguments?.getString("date")

                    AddFoodScreen(
                        viewModel = addFoodViewModel,
                        prefillName = name,
                        prefillMeal = meal,
                        trackedFoodId = id,
                        prefillDate = date,
                        onSaveSuccess = {
                            navController.previousBackStackEntry?.savedStateHandle?.set("food_added", true)
                            navController.popBackStack()
                        }
                    )
                }

                // HOME
                composable("home") {
                    HomeScreen(
                        homeViewModel = homeViewModel,
                        addFoodViewModel = addFoodViewModel,
                        navController = navController,
                        onFoodClick = { foodOrTracked, mealType, date ->
                            when (foodOrTracked) {
                                is Food -> {
                                    val encoded = Uri.encode(foodOrTracked.name)
                                    navController.navigate("add_food?name=$encoded&meal=$mealType&date=$date")
                                }
                                is TrackedFood -> {
                                    val encoded = Uri.encode(foodOrTracked.name)
                                    navController.navigate("add_food?name=$encoded&meal=$mealType&id=${foodOrTracked.id}&date=${foodOrTracked.date}")
                                }
                            }
                        },
                        onNavigateToAddFood = { name ->
                            val encoded = Uri.encode(name)
                            navController.navigate("add_food?name=$encoded")
                        }
                    )
                }

                composable("dashboard") {
                    StatsScreen(statsViewModel)
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
                            navController = navController,
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
        Screen("home", Icons.Rounded.Home, stringResource(R.string.nav_home)),
        Screen("dashboard", Icons.Rounded.BarChart, stringResource(R.string.nav_stats)),
        Screen("camera", Icons.Rounded.PhotoCamera, stringResource(R.string.nav_add)),
        Screen("profile", Icons.Rounded.Person, stringResource(R.string.nav_profile))
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {

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
