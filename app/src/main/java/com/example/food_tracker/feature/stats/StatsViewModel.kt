package com.example.food_tracker.feature.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.BuildConfig
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.domain.model.DietHistory
import com.example.food_tracker.domain.usecase.GetAllDietHistoriesUseCase
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatsViewModel(
    private val getAllDietHistoriesUseCase: GetAllDietHistoriesUseCase,
    private val userDataStore: UserDataStore
) : ViewModel() {

    var state by mutableStateOf(StatsState())
        private set

    private var statsJob: Job? = null

    private fun getSdf() = SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag(state.languageCode))

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }

    init {
        loadAppPreferences()
        loadStats()
        loadSavedSuggestion()
    }

    private fun loadAppPreferences() {
        viewModelScope.launch {
            userDataStore.appPreferencesFlow.collect { prefs ->
                if (state.languageCode != prefs.languageCode) {
                    state = state.copy(languageCode = prefs.languageCode)
                    // Re-calculate stats to update localized labels (months, etc.)
                    loadStats()
                }
            }
        }
    }

    private fun loadSavedSuggestion() {
        viewModelScope.launch {
            val savedSuggestion = userDataStore.dietarySuggestionFlow.first()
            val savedDate = userDataStore.lastSuggestionDateFlow.first()
            state = state.copy(
                dietarySuggestion = savedSuggestion,
                lastSuggestionDate = savedDate
            )
            
            val today = getSdf().format(Date())
            if (savedDate != today && state.dietHistories.isNotEmpty()) {
                getDietarySuggestion()
            }
        }
    }

    private fun loadStats() {
        statsJob?.cancel()
        state = state.copy(isLoading = true)
        statsJob = getAllDietHistoriesUseCase().onEach { histories ->
            val weeklyStats = (0..3).map { weeksAgo ->
                val weekCalendar = Calendar.getInstance()
                weekCalendar.add(Calendar.WEEK_OF_YEAR, -weeksAgo)
                val weekOfYear = weekCalendar.get(Calendar.WEEK_OF_YEAR)
                val year = weekCalendar.get(Calendar.YEAR)
                
                val weekHistories = histories.filter {
                    val date = try { getSdf().parse(it.date) } catch (e: Exception) { null }
                    if (date != null) {
                        val c = Calendar.getInstance()
                        c.time = date
                        c.get(Calendar.WEEK_OF_YEAR) == weekOfYear && c.get(Calendar.YEAR) == year
                    } else false
                }
                
                calculateGoalStats("W$weekOfYear", weekHistories)
            }.reversed()

            val monthlyStats = (0..5).map { monthsAgo ->
                val monthCalendar = Calendar.getInstance()
                monthCalendar.add(Calendar.MONTH, -monthsAgo)
                val month = monthCalendar.get(Calendar.MONTH)
                val year = monthCalendar.get(Calendar.YEAR)
                
                val monthHistories = histories.filter {
                    val date = try { getSdf().parse(it.date) } catch (e: Exception) { null }
                    if (date != null) {
                        val c = Calendar.getInstance()
                        c.time = date
                        c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
                    } else false
                }
                
                val monthName = SimpleDateFormat("MMM", Locale.forLanguageTag(state.languageCode)).format(monthCalendar.time)
                calculateGoalStats(monthName, monthHistories)
            }.reversed()

            val graphData = histories.take(30).sortedBy { it.date }

            state = state.copy(
                dietHistories = histories,
                weeklyStatsList = weeklyStats,
                monthlyStatsList = monthlyStats,
                graphData = graphData,
                isLoading = false
            )

            val today = getSdf().format(Date())
            if (state.lastSuggestionDate != today && histories.isNotEmpty()) {
                getDietarySuggestion()
            }
        }.launchIn(viewModelScope)
    }

    fun getDietarySuggestion() {
        if (state.dietHistories.isEmpty()) return

        if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
            state = state.copy(dietarySuggestion = "Error: API Key is empty. Please check local.properties and Rebuild Project.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isSuggestionLoading = true)
            try {
                val profile = userDataStore.userProfileFlow.first()
                val isIndonesian = state.languageCode == "in"
                
                val top5Meals = state.dietHistories
                    .flatMap { it.trackedFoods }
                    .groupBy { it.name }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)
                    .joinToString { it.first }

                val prompt = if (isIndonesian) {
                    """
                        Berikan saran diet berdasarkan profil user dan data aktivitas berikut dalam Bahasa Indonesia yang singkat, padat, dan memotivasi.
                        
                        Profil User:
                        - Berat: ${profile.weight} kg
                        - Tinggi: ${profile.height} cm
                        - Umur: ${profile.age} tahun
                        - Jenis Kelamin: ${if (profile.isMale) "Laki-laki" else "Perempuan"}
                        - Level Aktivitas: ${profile.activityLevel}
                        - Goal: ${profile.goal}
                        
                        Data Mingguan (Pencapaian Target):
                        ${state.weeklyStatsList.joinToString("\n") { "${it.label}: Kalori ${it.caloriesReachedCount}/${it.totalDays}, Protein ${it.proteinReachedCount}/${it.totalDays}" }}
                        
                        Data Bulanan (Pencapaian Target):
                        ${state.monthlyStatsList.joinToString("\n") { "${it.label}: Kalori ${it.caloriesReachedCount}/${it.totalDays}, Protein ${it.proteinReachedCount}/${it.totalDays}" }}
                        
                        Tren Kalori (30 hari terakhir):
                        Rata-rata kalori harian: ${state.graphData.map { it.totalCalories }.average().toInt()} kcal
                        
                        5 Makanan yang paling sering dimakan:
                        $top5Meals
                        
                        Berikan saran personal yang spesifik untuk profil saya guna meningkatkan kualitas diet saya.
                    """.trimIndent()
                } else {
                    """
                        Provide dietary advice based on the following user profile and activity data in English. Keep it concise, punchy, and motivating.
                        
                        User Profile:
                        - Weight: ${profile.weight} kg
                        - Height: ${profile.height} cm
                        - Age: ${profile.age} years
                        - Gender: ${if (profile.isMale) "Male" else "Female"}
                        - Activity Level: ${profile.activityLevel}
                        - Goal: ${profile.goal}
                        
                        Weekly Data (Target Achievement):
                        ${state.weeklyStatsList.joinToString("\n") { "${it.label}: Calories ${it.caloriesReachedCount}/${it.totalDays}, Protein ${it.proteinReachedCount}/${it.totalDays}" }}
                        
                        Monthly Data (Target Achievement):
                        ${state.monthlyStatsList.joinToString("\n") { "${it.label}: Calories ${it.caloriesReachedCount}/${it.totalDays}, Protein ${it.proteinReachedCount}/${it.totalDays}" }}
                        
                        Calorie Trend (Last 30 days):
                        Average daily calories: ${state.graphData.map { it.totalCalories }.average().toInt()} kcal
                        
                        Top 5 most frequent foods:
                        $top5Meals
                        
                        Provide specific personal advice for my profile to improve my diet quality.
                    """.trimIndent()
                }

                val response = generativeModel.generateContent(prompt)
                val suggestion = response.text ?: (if (isIndonesian) "Gagal mendapatkan saran." else "Failed to get suggestion.")
                
                val today = getSdf().format(Date())
                userDataStore.saveDietarySuggestion(suggestion, today)
                
                state = state.copy(
                    dietarySuggestion = suggestion,
                    lastSuggestionDate = today,
                    isSuggestionLoading = false
                )
            } catch (e: Exception) {
                val errorMsg = if (state.languageCode == "in") {
                    "Galat: ${e.message}\nPastikan model gemini-1.5-flash tersedia di wilayah Anda."
                } else {
                    "Error: ${e.message}\nMake sure gemini-1.5-flash model is available in your region."
                }
                state = state.copy(
                    dietarySuggestion = errorMsg,
                    isSuggestionLoading = false
                )
            }
        }
    }

    private fun calculateGoalStats(label: String, histories: List<DietHistory>): GoalStats {
        var calCount = 0
        var proteinCount = 0
        var carbsCount = 0
        var fatCount = 0

        histories.forEach { history ->
            if (history.calorieGoalReached) calCount++
            if (history.proteinGoalReached) proteinCount++
            if (history.carbsGoalReached) carbsCount++
            if (history.fatGoalReached) fatCount++
        }

        return GoalStats(
            label = label,
            caloriesReachedCount = calCount,
            proteinReachedCount = proteinCount,
            carbsReachedCount = carbsCount,
            fatReachedCount = fatCount,
            totalDays = histories.size
        )
    }
}
