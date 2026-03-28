package com.example.food_tracker.feature.stats

import com.example.food_tracker.domain.model.DietHistory

data class StatsState(
    val dietHistories: List<DietHistory> = emptyList(),
    val weeklyStatsList: List<GoalStats> = emptyList(),
    val monthlyStatsList: List<GoalStats> = emptyList(),
    val graphData: List<DietHistory> = emptyList(),
    val isLoading: Boolean = false,
    val dietarySuggestion: String? = null,
    val isSuggestionLoading: Boolean = false,
    val lastSuggestionDate: String? = null
)

data class GoalStats(
    val label: String = "",
    val caloriesReachedCount: Int = 0,
    val proteinReachedCount: Int = 0,
    val carbsReachedCount: Int = 0,
    val fatReachedCount: Int = 0,
    val totalDays: Int = 0
)
