package com.example.food_tracker.data.repository

import com.example.food_tracker.R
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.domain.model.UserProfile
import com.example.food_tracker.domain.usecase.CalculateNutritionUseCase
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class ProfileRepository(
    private val userDataStore: UserDataStore,
    private val foodRepository: FoodRepositoryImpl,
    private val calculateNutritionUseCase: CalculateNutritionUseCase = CalculateNutritionUseCase()
) {
    val userProfileFlow: Flow<UserProfile> = userDataStore.userProfileFlow

    suspend fun saveProfile(profile: UserProfile) {
        // 1. Save raw profile to DataStore
        userDataStore.saveProfile(
            profile.weight,
            profile.height,
            profile.age,
            profile.isMale,
            profile.activityLevel,
            profile.goal
        )

        // 2. Calculate new goals based on Mifflin-St Jeor
        val result = calculateNutritionUseCase(profile)

        // 3. Save goals to DataStore for global access
        userDataStore.saveCalorieGoal(result.calories)
        userDataStore.saveProteinGoal(result.protein)
        userDataStore.saveCarbsGoal(result.carbs)
        userDataStore.saveFatGoal(result.fat)

        // 4. Update today's record in the database so stats are immediate
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        foodRepository.updateDietHistoryGoalsForDate(
            date = today,
            calories = result.calories,
            protein = result.protein,
            carbs = result.carbs,
            fat = result.fat
        )
    }

    fun getActivityLevelResId(level: Double): Int {
        return when {
            level <= 1.2 -> R.string.sedentary
            level <= 1.375 -> R.string.slightly_active
            level <= 1.55 -> R.string.moderately_active
            level <= 1.725 -> R.string.very_active
            else -> R.string.extra_active
        }
    }
}
