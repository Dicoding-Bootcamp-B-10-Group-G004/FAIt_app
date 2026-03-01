package com.example.food_tracker.feature.home

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.domain.model.Food

class HomeViewModel(private val repository: FoodRepositoryImpl) : ViewModel() {

    // Simpan semua data dari CSV di sini
    private val _allFoods = mutableStateOf<List<Food>>(emptyList())

    // State untuk hasil pencarian
    private val _searchResults = mutableStateOf<List<Food>>(emptyList())
    val searchResults: State<List<Food>> = _searchResults

    init {
        // Pas pertama kali dibuka, langsung load data dari CSV
        _allFoods.value = repository.getAllFoodFromCsv()
    }

    fun searchFood(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        // Filter data berdasarkan nama yang diketik user
        _searchResults.value = _allFoods.value.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}