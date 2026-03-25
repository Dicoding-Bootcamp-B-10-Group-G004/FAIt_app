package com.example.food_tracker.feature.home

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Goal kalori harian (Bisa diatur dinamis nanti)
    var calorieGoal by mutableStateOf(2000.0)
        private set

    // State internal untuk menyimpan semua data makanan dari CSV
    private var _allFoods by mutableStateOf<List<Food>>(emptyList())

    private val foodClassifier = FoodClassifier()

    init {
        loadFoods()
        observeNutritionData()
    }

    private fun loadFoods() {
        viewModelScope.launch {
            val foods = withContext(Dispatchers.IO) {
                repository.getAllFoodFromCsv()
            }
            _allFoods = foods
        }
    }

    private fun observeNutritionData() {
        viewModelScope.launch {
            userDataStore.getSuppliedCals().collectLatest {
                state = state.copy(suppliedCalories = it ?: 0.0)
            }
        }
        viewModelScope.launch {
            userDataStore.getProtein().collectLatest {
                state = state.copy(proteinCount = it ?: 0.0)
            }
        }
        viewModelScope.launch {
            userDataStore.getCarbs().collectLatest {
                state = state.copy(carbsCount = it ?: 0.0)
            }
        }
        viewModelScope.launch {
            userDataStore.getFat().collectLatest {
                state = state.copy(fatCount = it ?: 0.0)
            }
        }
    }

    // FIX: Fungsi ini yang dicari oleh DetectionResultScreen
    fun getFoodByName(name: String): Food? {
        // Mencocokkan nama label dari ML dengan kolom 'name' di CSV (Case Insensitive)
        return _allFoods.find { it.name.equals(name, ignoreCase = true) }
    }

    fun addFoodDirect(food: Food, portion: Int) {
        viewModelScope.launch {
            // Biasanya data di CSV itu per 100g, jadi kita bagi 100 untuk porsinya
            val multiplier = portion.toDouble() / 100.0

            val calories = (food.calories * multiplier).toInt()
            val protein = (food.protein.replace(",", ".").toDoubleOrNull() ?: 0.0) * multiplier
            val carbs = (food.carbs.replace(",", ".").toDoubleOrNull() ?: 0.0) * multiplier
            val fat = (food.fat.replace(",", ".").toDoubleOrNull() ?: 0.0) * multiplier

            repository.insertFoodHistory(
                food.copy(
                    calories = calories,
                    protein = String.format("%.1f", protein),
                    carbs = String.format("%.1f", carbs),
                    fat = String.format("%.1f", fat)
                )
            )
        }
    }

    fun getMacroAchievement(): Triple<Boolean, Boolean, Boolean> {
        return Triple(
            state.proteinCount >= 96,
            state.carbsCount >= 385,
            state.suppliedCalories >= calorieGoal
        )
    }
}