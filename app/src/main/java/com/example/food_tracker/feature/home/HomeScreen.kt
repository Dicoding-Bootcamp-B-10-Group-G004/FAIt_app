package com.example.food_tracker.feature.home

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.food_tracker.R
import com.example.food_tracker.core.ui.components.*
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.domain.model.TrackedFood
import com.example.food_tracker.feature.addfood.AddFoodEvent
import com.example.food_tracker.feature.addfood.AddFoodViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DateDisplayModel(
    val dateStr: String,
    val dayName: String,
    val dayOfMonth: String,
    val isFuture: Boolean,
    val isSelected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    addFoodViewModel: AddFoodViewModel,
    navController: NavController,
    onFoodClick: (Any, String, String) -> Unit,
    onNavigateToAddFood: (String) -> Unit
) {
    val context = LocalContext.current
    val foodAddedSuccessMsg = stringResource(R.string.food_added_success)
    val breakfastLabel = stringResource(R.string.breakfast)
    val lunchLabel = stringResource(R.string.lunch)
    val dinnerLabel = stringResource(R.string.dinner)
    val foodAddedDirectMsg = stringResource(R.string.food_added_direct_success)
    val returnToTodayDesc = stringResource(R.string.return_to_today)
    val deleteDesc = stringResource(R.string.delete)
    
    LaunchedEffect(Unit) {
        homeViewModel.resetToToday()
    }

    val foodAdded = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("food_added", false)
        ?.collectAsState()

    LaunchedEffect(foodAdded?.value) {
        if (foodAdded?.value == true) {
            Toast.makeText(context, foodAddedSuccessMsg, Toast.LENGTH_SHORT).show()
            navController.currentBackStackEntry?.savedStateHandle?.set("food_added", false)
        }
    }

    val themeColors = MaterialTheme.colorScheme
    val homeState = homeViewModel.state
    val addFoodState = addFoodViewModel.state

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var selectedCategory by remember { mutableStateOf("") }

    Scaffold(
        containerColor = themeColors.background,
        topBar = {
            FTTopBar(
                title = {
                    HomeTopBarTitle(
                        isToday = homeState.isToday,
                        displayDate = homeState.displayDate
                    )
                },
                onTopBarClick = {
                    scope.launch { listState.animateScrollToItem(0) }
                },
                actions = {
                    AnimatedVisibility(
                        visible = !homeState.isToday,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        FTIconButton(
                            icon = Icons.Rounded.Today,
                            onClick = { homeViewModel.resetToToday() },
                            contentDescription = returnToTodayDesc,
                            containerColor = themeColors.onPrimaryContainer.copy(alpha = 0.1f),
                            contentColor = themeColors.onPrimaryContainer
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "date_selector", contentType = "widget") {
                DateSelector(
                    selectedDate = homeState.selectedDate,
                    onDateSelected = { homeViewModel.onDateSelected(it) },
                    darkGreen = themeColors.onPrimaryContainer
                )
            }

            item(key = "calorie_card", contentType = "card") {
                CalorieSection(
                    suppliedCalories = homeState.suppliedCalories,
                    calorieGoal = homeState.calorieGoal,
                    accentColor = themeColors.secondary
                )
            }

            item(key = "search_section", contentType = "search") {
                SearchSection(
                    searchQuery = addFoodState.searchQuery,
                    selectedCategory = selectedCategory,
                    displayDate = homeState.displayDate,
                    onSearchQueryChange = { addFoodViewModel.onEvent(AddFoodEvent.OnSearchQueryChange(it)) },
                    onClearCategory = { selectedCategory = "" }
                )
            }

            if (addFoodState.searchQuery.isNotEmpty()) {
                items(
                    items = addFoodState.searchResults,
                    key = { it.name },
                    contentType = { "search_result" }
                ) { food ->
                    FTFoodListItem(
                        name = food.name,
                        portion = food.portion,
                        calories = food.calories,
                        protein = food.protein,
                        carbs = food.carbs,
                        fat = food.fat,
                        onClick = { 
                            onFoodClick(food, selectedCategory.ifEmpty { "Lunch" }, homeState.selectedDate) 
                        },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    homeViewModel.addFoodDirect(food, food.portion, selectedCategory.ifEmpty { "Lunch" })
                                    addFoodViewModel.onEvent(AddFoodEvent.OnSearchQueryChange(""))
                                    selectedCategory = ""
                                    Toast.makeText(context, foodAddedDirectMsg.format(food.name), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.background(themeColors.secondary.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Add")
                            }
                        }
                    )
                }
            }

            item(key = "macros", contentType = "widget") {
                MacroRow(
                    carbsCount = homeState.carbsCount,
                    carbsGoal = homeState.carbsGoal,
                    fatCount = homeState.fatCount,
                    fatGoal = homeState.fatGoal,
                    proteinCount = homeState.proteinCount,
                    proteinGoal = homeState.proteinGoal,
                    accentColor = themeColors.secondary
                )
            }

            homeState.categories.forEach { category ->
                item(key = "header_${category.name}", contentType = "meal_header") {
                    val categoryDisplayName = when(category.name) {
                        "Breakfast" -> breakfastLabel
                        "Lunch" -> lunchLabel
                        "Dinner" -> dinnerLabel
                        else -> category.name
                    }
                    MealCategoryCard(
                        title = categoryDisplayName,
                        kcal = "${category.totalCalories} kcal",
                        icon = when(category.name) {
                            "Breakfast" -> Icons.Rounded.LightMode
                            "Lunch" -> Icons.Rounded.WbSunny
                            "Dinner" -> Icons.Rounded.NightsStay
                            else -> Icons.Rounded.Restaurant
                        }
                    ) {
                        selectedCategory = category.name
                        scope.launch {
                            listState.animateScrollToItem(index = 2)
                        }
                    }
                }
                items(
                    items = category.foods,
                    key = { it.id },
                    contentType = { "tracked_food" }
                ) { food ->
                    FTFoodListItem(
                        name = food.name,
                        portion = food.portion,
                        calories = food.calories,
                        protein = food.protein,
                        carbs = food.carbs,
                        fat = food.fat,
                        onClick = { onFoodClick(food, food.mealType, food.date) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${food.calories.toInt()} kcal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold, 
                                    color = themeColors.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { homeViewModel.deleteFood(food) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = deleteDesc,
                                        tint = themeColors.error,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTopBarTitle(
    isToday: Boolean,
    displayDate: String
) {
    Column {
        Text(
            text = if (isToday) stringResource(R.string.today) else stringResource(R.string.history),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f)
        )
        Text(
            text = displayDate,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@Composable
private fun CalorieSection(
    suppliedCalories: Double,
    calorieGoal: Double,
    accentColor: Color
) {
    val suppliedStr = remember(suppliedCalories) { 
        String.format(Locale.getDefault(), "%.0f", suppliedCalories) 
    }
    val leftStr = remember(suppliedCalories, calorieGoal) { 
        String.format(Locale.getDefault(), "%.0f", (calorieGoal - suppliedCalories).coerceAtLeast(0.0)) 
    }
    val progress = remember(suppliedCalories, calorieGoal) { 
        (suppliedCalories / calorieGoal.coerceAtLeast(1.0)).toFloat() 
    }
    
    MainCalorieCard(
        accentColor = accentColor,
        supplied = suppliedStr,
        left = leftStr,
        progress = progress
    )
}

@Composable
private fun MacroRow(
    carbsCount: Double,
    carbsGoal: Double,
    fatCount: Double,
    fatGoal: Double,
    proteinCount: Double,
    proteinGoal: Double,
    accentColor: Color
) {
    val carbsLabel = stringResource(R.string.legend_carbs)
    val fatLabel = stringResource(R.string.legend_fat)
    val proteinLabel = stringResource(R.string.legend_protein)
    
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        val carbsProgress = (carbsCount.toFloat() / carbsGoal.toFloat().coerceAtLeast(1f))
        val fatProgress = (fatCount.toFloat() / fatGoal.toFloat().coerceAtLeast(1f))
        val proteinProgress = (proteinCount.toFloat() / proteinGoal.toFloat().coerceAtLeast(1f))
        
        MacroItem(carbsLabel, "${carbsCount.toInt()}/${carbsGoal.toInt()}g", carbsProgress, Modifier.weight(1f), accentColor)
        MacroItem(fatLabel, "${fatCount.toInt()}/${fatGoal.toInt()}g", fatProgress, Modifier.weight(1f), accentColor)
        MacroItem(proteinLabel, "${proteinCount.toInt()}/${proteinGoal.toInt()}g", proteinProgress, Modifier.weight(1f), accentColor)
    }
}

@Composable
fun SearchSection(
    searchQuery: String,
    selectedCategory: String,
    displayDate: String,
    onSearchQueryChange: (String) -> Unit,
    onClearCategory: () -> Unit
) {
    val breakfastLabel = stringResource(R.string.breakfast)
    val lunchLabel = stringResource(R.string.lunch)
    val dinnerLabel = stringResource(R.string.dinner)
    val searchPlaceholder = stringResource(R.string.search_food)
    val addingToMsg = stringResource(R.string.adding_to)

    Column {
        if (selectedCategory.isNotEmpty()) {
            val categoryDisplayName = when(selectedCategory) {
                "Breakfast" -> breakfastLabel
                "Lunch" -> lunchLabel
                "Dinner" -> dinnerLabel
                else -> selectedCategory
            }
            AssistChip(
                onClick = onClearCategory,
                label = { Text(addingToMsg.format(categoryDisplayName, displayDate)) },
                trailingIcon = {
                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            )
        }

        FTOutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = searchPlaceholder,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(selectedDate: String, onDateSelected: (String) -> Unit, darkGreen: Color) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val horizontalListState = rememberLazyListState()
    var showCalendar by remember { mutableStateOf(false) }

    val weekDates = remember(selectedDate) {
        val list = mutableListOf<DateDisplayModel>()
        val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dayOfMonthFormat = SimpleDateFormat("d", Locale.getDefault())
        val cal = Calendar.getInstance()
        
        val selected = try { dateFormat.parse(selectedDate) ?: Date() } catch(e: Exception) { Date() }
        cal.time = selected
        
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        for (i in 0..6) {
            val date = cal.time
            val dStr = dateFormat.format(date)
            list.add(
                DateDisplayModel(
                    dateStr = dStr,
                    dayName = dayNameFormat.format(date),
                    dayOfMonth = dayOfMonthFormat.format(date),
                    isFuture = date.after(today),
                    isSelected = dStr == selectedDate
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    LaunchedEffect(selectedDate, weekDates) {
        val index = weekDates.indexOfFirst { it.dateStr == selectedDate }
        if (index != -1) {
            horizontalListState.animateScrollToItem(index)
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                state = horizontalListState,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                items(
                    items = weekDates,
                    key = { model -> model.dateStr }
                ) { model ->
                    DateItem(model, darkGreen, onDateSelected)
                }
            }

            VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 4.dp))

            IconButton(onClick = { showCalendar = true }) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "Open Calendar",
                    tint = darkGreen
                )
            }
        }
    }

    if (showCalendar) {
        CalendarDialog(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            onDismiss = { showCalendar = false },
            darkGreen = darkGreen
        )
    }
}

@Composable
private fun DateItem(
    model: DateDisplayModel,
    darkGreen: Color,
    onDateSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(50.dp)
            .alpha(if (model.isFuture) 0.3f else 1.0f)
            .clickable(enabled = !model.isFuture) { onDateSelected(model.dateStr) },
        shape = RoundedCornerShape(16.dp),
        color = if (model.isSelected) darkGreen else Color.Transparent,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = model.dayName,
                style = MaterialTheme.typography.labelSmall,
                color = if (model.isSelected) Color.White else MaterialTheme.colorScheme.outline,
                fontWeight = if (model.isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = model.dayOfMonth,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (model.isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarDialog(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    darkGreen: Color
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
    }

    val okLabel = stringResource(R.string.ok)
    val cancelLabel = stringResource(R.string.cancel)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try { dateFormat.parse(selectedDate)?.time } catch(e: Exception) { null },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayMillis
            }
        }
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.timeInMillis = it
                    onDateSelected(dateFormat.format(calendar.time))
                }
                onDismiss()
            }) {
                Text(okLabel, color = darkGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelLabel, color = MaterialTheme.colorScheme.outline)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            showModeToggle = true,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = darkGreen,
                todayContentColor = darkGreen,
                todayDateBorderColor = darkGreen
            )
        )
    }
}

@Composable
fun MainCalorieCard(accentColor: Color, supplied: String, left: String, progress: Float) {
    val kcalLeftLabel = stringResource(R.string.kcal_left)
    val suppliedLabel = stringResource(R.string.supplied)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = accentColor,
                    strokeWidth = 10.dp,
                    trackColor = accentColor.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = left,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = kcalLeftLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = suppliedLabel.format(supplied),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: String, progress: Float, modifier: Modifier, accentColor: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun MealCategoryCard(title: String, kcal: String, icon: ImageVector, onAdd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(), 
        shape = RoundedCornerShape(20.dp), 
        color = MaterialTheme.colorScheme.surface, 
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = kcal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                IconButton(onClick = onAdd) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                }
            }
        }
    }
}
