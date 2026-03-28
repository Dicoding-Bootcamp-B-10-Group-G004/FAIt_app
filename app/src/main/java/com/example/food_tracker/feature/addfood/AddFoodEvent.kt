package com.example.food_tracker.feature.addfood

import com.example.food_tracker.domain.model.Food

sealed class AddFoodEvent {
    data class OnSearchQueryChange(val query: String) : AddFoodEvent()

    data class OnFoodSelected(val food: Food) : AddFoodEvent()

    data class OnPortionChange(val portion: Double) : AddFoodEvent()

    data class OnMealChange(val mealType: String) : AddFoodEvent()

    data class OnDateChange(val date: String) : AddFoodEvent()

    data class OnLoadTrackedFood(val id: String) : AddFoodEvent()

    object OnSave : AddFoodEvent()

    object OnReset : AddFoodEvent()
}
