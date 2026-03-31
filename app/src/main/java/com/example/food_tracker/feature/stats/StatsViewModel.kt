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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
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
            
            if (shouldRequestWeeklySuggestion()) {
                getDietarySuggestion()
            }
        }
    }

    private fun shouldRequestWeeklySuggestion(): Boolean {
        if (state.dietHistories.isEmpty()) return false

        val now = Calendar.getInstance()
        // Sunday is used as the end of the week check.
        val isEndOfWeek = now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        if (!isEndOfWeek) return false

        val lastDateStr = state.lastSuggestionDate ?: return true
        val lastDate = try {
            getSdf().parse(lastDateStr)
        } catch (e: Exception) {
            null
        } ?: return true

        val lastCal = Calendar.getInstance().apply { time = lastDate }

        // Only request if the last suggestion was from a different week or year
        return now.get(Calendar.WEEK_OF_YEAR) != lastCal.get(Calendar.WEEK_OF_YEAR) ||
                now.get(Calendar.YEAR) != lastCal.get(Calendar.YEAR)
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

            if (shouldRequestWeeklySuggestion()) {
                getDietarySuggestion()
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Fetches the list of available models directly from the Google AI API
     */
    private suspend fun fetchAvailableModels(): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models?key=${BuildConfig.GEMINI_API_KEY}")
            val connection = url.openConnection() as java.net.HttpURLConnection
            val responseText = try {
                connection.inputStream.bufferedReader().readText()
            } catch (e: Exception) {
                connection.errorStream?.bufferedReader()?.readText() ?: ""
            }
            if (responseText.isEmpty()) return@withContext emptyList()
            
            val json = JSONObject(responseText)
            if (!json.has("models")) return@withContext emptyList()
            
            val modelsArray = json.getJSONArray("models")
            val textModels = mutableListOf<String>()
            for (i in 0 until modelsArray.length()) {
                val modelObj = modelsArray.getJSONObject(i)
                val name = modelObj.getString("name")
                val shortName = name.removePrefix("models/")
                val methods = modelObj.getJSONArray("supportedGenerationMethods").toString()
                
                // Exclude non-text/specialized models
                val isExcluded = shortName.contains("tts", ignoreCase = true) ||
                                shortName.contains("research", ignoreCase = true) ||
                                shortName.contains("lyria", ignoreCase = true) ||
                                shortName.contains("embedding", ignoreCase = true) ||
                                shortName.contains("aqa", ignoreCase = true)

                // Only include models that support content generation (text/multimodal) and are not excluded
                if (methods.contains("generateContent") && !isExcluded) {
                    textModels.add(name)
                }
            }
            textModels
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getModelPriorityList(availableModels: List<String>): List<String> {
        val modelPriorityList = mutableListOf<String>()
        val defaultModelId = "gemini-3.1-flash-lite-preview"
        val fullDefaultName = "models/$defaultModelId"

        if (availableModels.contains(fullDefaultName) || availableModels.contains(defaultModelId)) {
            modelPriorityList.add(defaultModelId)
        }

        availableModels.forEach { fullModelName ->
            val shortName = fullModelName.removePrefix("models/")
            if (shortName != defaultModelId && !modelPriorityList.contains(shortName)) {
                modelPriorityList.add(shortName)
            }
        }

        if (modelPriorityList.isEmpty()) {
            modelPriorityList.addAll(listOf(defaultModelId, "gemini-1.5-flash", "gemini-1.5-flash-8b", "gemini-1.5-pro"))
        }
        return modelPriorityList
    }

    fun getDietarySuggestion() {
        if (state.dietHistories.isEmpty() || state.isSuggestionLoading) return

        if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
            state = state.copy(dietarySuggestion = "Error: API Key is empty. Please check local.properties and Rebuild Project.")
            return
        }

        viewModelScope.launch {
            state = state.copy(isSuggestionLoading = true)
            try {
                // 1. Prepare Prompt
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

                // 2. Fetch models (persistent cache from UserDataStore)
                var currentCachedModels = userDataStore.cachedModelsFlow.first()
                if (currentCachedModels.isEmpty()) {
                    currentCachedModels = fetchAvailableModels()
                    if (currentCachedModels.isNotEmpty()) {
                        userDataStore.saveCachedModels(currentCachedModels)
                    }
                }

                var finalSuggestion: String? = null
                var lastException: Exception? = null

                // Helper to try models in order
                suspend fun tryToGenerateSuggestion(models: List<String>): String? {
                    val priorityList = getModelPriorityList(models)
                    for (modelName in priorityList) {
                        try {
                            val model = GenerativeModel(
                                modelName = modelName,
                                apiKey = BuildConfig.GEMINI_API_KEY
                            )
                            val response = model.generateContent(prompt)
                            if (!response.text.isNullOrBlank()) return response.text
                        } catch (e: Exception) {
                            lastException = e
                        }
                    }
                    return null
                }

                // 3. Try models from cache
                finalSuggestion = tryToGenerateSuggestion(currentCachedModels)

                // 4. If all cached models failed, refresh cache and try again once
                if (finalSuggestion == null) {
                    val refreshedModels = fetchAvailableModels()
                    if (refreshedModels.isNotEmpty()) {
                        userDataStore.saveCachedModels(refreshedModels)
                        finalSuggestion = tryToGenerateSuggestion(refreshedModels)
                    }
                }

                if (finalSuggestion != null) {
                    val today = getSdf().format(Date())
                    userDataStore.saveDietarySuggestion(finalSuggestion, today)
                    
                    state = state.copy(
                        dietarySuggestion = finalSuggestion,
                        lastSuggestionDate = today,
                        isSuggestionLoading = false
                    )
                } else {
                    throw lastException ?: Exception("All available models failed to generate content")
                }
            } catch (e: Exception) {
                val errorMsg = if (state.languageCode == "in") {
                    "Galat: ${e.message}\nPastikan model Gemini tersedia atau kuota limit belum tercapai."
                } else {
                    "Error: ${e.message}\nEnsure Gemini models are available or quota limit not reached."
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
