package com.example.food_tracker.feature.home

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.data.local.CsvNutritionReader
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.data.ml.FoodClassifier
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.domain.model.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: FoodRepositoryImpl,
    private val userDataStore: UserDataStore
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    private val _allFoods = mutableStateOf<List<Food>>(emptyList())
    private val _searchResults = mutableStateOf<List<Food>>(emptyList())
    val searchResults: State<List<Food>> = _searchResults

    private val foodClassifier = FoodClassifier()

    init {

        // Load CSV makanan
        viewModelScope.launch {
            val foods = withContext(Dispatchers.IO) {
                repository.getAllFoodFromCsv()
            }
            _allFoods.value = foods
        }

        // Observe DataStore
        observeNutritionData()
    }

    private fun updateState(
        calories: Double? = null,
        protein: Double? = null,
        carbs: Double? = null,
        fat: Double? = null
    ) {
        state = state.copy(
            suppliedCalories = calories ?: state.suppliedCalories,
            proteinCount = protein ?: state.proteinCount,
            carbsCount = carbs ?: state.carbsCount,
            fatCount = fat ?: state.fatCount
        )
    }

    private fun observeNutritionData() {

        viewModelScope.launch {

            userDataStore.getSuppliedCals().collectLatest { cals ->
                Log.d("FOOD_TRACKER", "Calories updated: $cals")
                updateState(calories = cals ?: 0.0)
            }
        }

        viewModelScope.launch {

            userDataStore.getProtein().collectLatest { protein ->
                updateState(protein = protein ?: 0.0)
            }
        }

        viewModelScope.launch {

            userDataStore.getCarbs().collectLatest { carbs ->
                updateState(carbs = carbs ?: 0.0)
            }
        }

        viewModelScope.launch {

            userDataStore.getFat().collectLatest { fat ->
                updateState(fat = fat ?: 0.0)
            }
        }
    }

    fun processFoodPhoto(bitmap: Bitmap, context: Context) {

        val csvReader = CsvNutritionReader(context)

        foodClassifier.classifyImage(bitmap) { detectedName ->

            val nutrition = csvReader.getNutritionData(detectedName)

            nutrition?.let { data ->

                viewModelScope.launch {

                    repository.insertFoodHistory(
                        Food(
                            name = detectedName,
                            calories = data.calories.toInt(),
                            protein = data.protein.toString(),
                            carbs = data.carbs.toString(),
                            fat = data.fat.toString(),
                            unit = "100g"
                        )
                    )
                }
            }
        }
    }

    fun addFoodDirect(food: Food, portion: Int) {

        viewModelScope.launch {

            val calories = food.calories * portion

            val protein =
                (food.protein.replace(",", ".").toDoubleOrNull() ?: 0.0) * portion

            val carbs =
                (food.carbs.replace(",", ".").toDoubleOrNull() ?: 0.0) * portion

            val fat =
                (food.fat.replace(",", ".").toDoubleOrNull() ?: 0.0) * portion

            Log.d("FOOD_TRACKER", "Adding food: ${food.name}")
            Log.d("FOOD_TRACKER", "Calories added: $calories")

            repository.insertFoodHistory(
                Food(
                    name = food.name,
                    calories = calories,
                    protein = protein.toString(),
                    carbs = carbs.toString(),
                    fat = fat.toString(),
                    unit = food.unit
                )
            )
        }
    }

    fun searchFood(query: String) {

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        _searchResults.value = _allFoods.value.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }

    fun getMacroAchievement(): Triple<Boolean, Boolean, Boolean> {

        val proteinGoal = 120
        val carbsGoal = 250
        val caloriesGoal = 2000

        val proteinReached = state.proteinCount >= proteinGoal
        val carbsReached = state.carbsCount >= carbsGoal
        val caloriesReached = state.suppliedCalories >= caloriesGoal

        return Triple(
            proteinReached,
            carbsReached,
            caloriesReached
        )
    }
}