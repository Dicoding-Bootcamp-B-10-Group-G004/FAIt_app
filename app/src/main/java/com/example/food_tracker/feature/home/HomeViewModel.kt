package com.example.food_tracker.feature.home

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.domain.model.TrackedFood
import com.example.food_tracker.domain.usecase.GetDietHistoryUseCase
import com.example.food_tracker.domain.usecase.DeleteTrackedFoodUseCase
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val repository: FoodRepositoryImpl,
    private val getDietHistoryUseCase: GetDietHistoryUseCase,
    private val deleteTrackedFoodUseCase: DeleteTrackedFoodUseCase
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    private val _selectedDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    private var observeJob: Job? = null

    init {
        ensureTodayHistory()
        loadAppPreferences()
        observeData()
    }

    private fun loadAppPreferences() {
        viewModelScope.launch {
            repository.appPreferencesFlow.collect { prefs ->
                if (state.languageCode != prefs.languageCode) {
                    state = state.copy(languageCode = prefs.languageCode)
                    // Restart observation to refresh localized display strings
                    observeData()
                }
            }
        }
    }

    private fun ensureTodayHistory() {
        viewModelScope.launch {
            repository.ensureDietHistoryForToday()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag(state.languageCode)).format(Date())
            
            _selectedDate
                .flatMapLatest { date ->
                    getDietHistoryUseCase(date)
                }
                .flowOn(Dispatchers.Default)
                .map { dietHistory ->
                    val locale = Locale.forLanguageTag(state.languageCode)
                    val isToday = dietHistory.date == todayStr
                    val displayDate = try {
                        val parsed = SimpleDateFormat("yyyy-MM-dd", locale).parse(dietHistory.date)
                        parsed?.let { SimpleDateFormat("MMMM d, yyyy", locale).format(it) } ?: dietHistory.date
                    } catch (e: Exception) {
                        dietHistory.date
                    }

                    val categoryNames = listOf("Breakfast", "Lunch", "Dinner", "Snack")
                    val categories = categoryNames.map { name ->
                        val foods = dietHistory.trackedFoods.filter { it.mealType == name }
                        MealCategory(
                            name = name,
                            foods = foods,
                            totalCalories = foods.sumOf { it.calories }.toInt()
                        )
                    }
                    Triple(dietHistory, categories, displayDate)
                }
                .collect { (dietHistory, categories, displayDate) ->
                    state = state.copy(
                        selectedDate = dietHistory.date,
                        displayDate = displayDate,
                        isToday = dietHistory.date == todayStr,
                        suppliedCalories = dietHistory.totalCalories,
                        proteinCount = dietHistory.totalProtein,
                        carbsCount = dietHistory.totalCarbs,
                        fatCount = dietHistory.totalFat,
                        calorieGoal = dietHistory.calorieGoal,
                        proteinGoal = dietHistory.proteinGoal,
                        carbsGoal = dietHistory.carbsGoal,
                        fatGoal = dietHistory.fatGoal,
                        proteinReached = dietHistory.proteinGoalReached,
                        carbsReached = dietHistory.carbsGoalReached,
                        caloriesReached = dietHistory.calorieGoalReached,
                        categories = categories
                    )
                }
        }
    }

    fun onDateSelected(date: String) {
        _selectedDate.value = date
    }

    fun resetToToday() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag(state.languageCode)).format(Date())
        _selectedDate.value = today
    }

    fun deleteFood(food: TrackedFood) {
        viewModelScope.launch {
            deleteTrackedFoodUseCase(food.id)
        }
    }

    suspend fun getFoodByName(name: String): Food? {
        return repository.getFoodByName(name)
    }

    fun getMacroAchievement(): Triple<Boolean, Boolean, Boolean> {
        return Triple(
            state.proteinCount >= state.proteinGoal,
            state.carbsCount >= state.carbsGoal,
            state.suppliedCalories >= state.calorieGoal
        )
    }

    fun addFoodDirect(food: Food, portion: Double, mealType: String = "Lunch") {
        viewModelScope.launch {
            val multiplier = portion / food.portion
            
            val trackedFood = TrackedFood(
                name = food.name,
                calories = food.calories * multiplier,
                protein = food.protein * multiplier,
                carbs = food.carbs * multiplier,
                fat = food.fat * multiplier,
                portion = portion,
                mealType = mealType,
                date = _selectedDate.value
            )
            repository.insertTrackedFood(trackedFood)
        }
    }
}
