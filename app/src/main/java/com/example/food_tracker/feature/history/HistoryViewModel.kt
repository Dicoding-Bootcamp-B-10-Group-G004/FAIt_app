package com.example.food_tracker.feature.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.domain.model.Food
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: FoodRepositoryImpl
) : ViewModel() {

    var foodList by mutableStateOf<List<Food>>(emptyList())
        private set

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            foodList = repository.getFoodHistory()
        }
    }
}