package com.example.food_tracker.feature.addfood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.R
import com.example.food_tracker.core.ui.components.FTOutlinedTextField
import com.example.food_tracker.core.ui.components.FTPrimaryButton
import java.util.Locale

@Composable
fun AddFoodScreen(
    viewModel: AddFoodViewModel,
    prefillName: String? = null,
    prefillMeal: String? = null,
    prefillDate: String? = null,
    trackedFoodId: String? = null,
    onSaveSuccess: () -> Unit = {}
) {
    val state = viewModel.state

    LaunchedEffect(trackedFoodId, prefillName, prefillMeal, prefillDate) {
        if (trackedFoodId != null) {
            viewModel.onEvent(AddFoodEvent.OnLoadTrackedFood(trackedFoodId))
        } else {
            viewModel.onEvent(AddFoodEvent.OnReset)
            
            if (!prefillName.isNullOrBlank()) {
                viewModel.onEvent(AddFoodEvent.OnSearchQueryChange(prefillName))
            }
            if (!prefillMeal.isNullOrBlank()) {
                viewModel.onEvent(AddFoodEvent.OnMealChange(prefillMeal))
            }
            if (!prefillDate.isNullOrBlank()) {
                viewModel.onEvent(AddFoodEvent.OnDateChange(prefillDate))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (trackedFoodId != null) stringResource(R.string.edit_nutrition) else stringResource(R.string.confirm_nutrition), 
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        FTOutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onEvent(AddFoodEvent.OnSearchQueryChange(it)) },
            label = stringResource(R.string.food_name),
            enabled = trackedFoodId == null 
        )

        Spacer(modifier = Modifier.height(16.dp))

        val food = state.selectedFood ?: state.searchResults.firstOrNull()

        if (food != null) {
            // Only select automatically if it's a new entry and nothing is selected yet
            if (trackedFoodId == null && state.selectedFood == null) {
                LaunchedEffect(food) {
                    viewModel.onEvent(AddFoodEvent.OnFoodSelected(food))
                }
            }

            FoodDetailsCard(
                foodName = food.name,
                portion = state.portion,
                mealType = state.mealType,
                baseFood = food,
                isEditing = trackedFoodId != null,
                onPortionChange = { viewModel.onEvent(AddFoodEvent.OnPortionChange(it)) },
                onMealChange = { viewModel.onEvent(AddFoodEvent.OnMealChange(it)) },
                onSave = {
                    viewModel.onEvent(AddFoodEvent.OnSave)
                    onSaveSuccess()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailsCard(
    foodName: String,
    portion: Double,
    mealType: String,
    baseFood: com.example.food_tracker.domain.model.Food,
    isEditing: Boolean,
    onPortionChange: (Double) -> Unit,
    onMealChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val categories = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = foodName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.portion) + ": ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = if (portion == 0.0) "" else portion.toString(),
                    onValueChange = {
                        val p = it.toDoubleOrNull() ?: 0.0
                        onPortionChange(p)
                    },
                    modifier = Modifier.width(100.dp).padding(horizontal = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    stringResource(R.string.gram),
                    style = MaterialTheme.typography.bodyLarge
                ) 
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                stringResource(R.string.meal_category) + ":",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { meal ->
                    val mealLabel = when(meal) {
                        "Breakfast" -> stringResource(R.string.breakfast)
                        "Lunch" -> stringResource(R.string.lunch)
                        "Dinner" -> stringResource(R.string.dinner)
                        "Snack" -> stringResource(R.string.snack)
                        else -> meal
                    }
                    FilterChip(
                        selected = mealType == meal,
                        onClick = { onMealChange(meal) },
                        label = { Text(mealLabel, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            val multiplier = portion / baseFood.portion
            val calories = baseFood.calories * multiplier 
            val protein = baseFood.protein * multiplier
            val carbs = baseFood.carbs * multiplier
            val fat = baseFood.fat * multiplier

            Text(
                stringResource(R.string.estimated_total_nutrition) + ":",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.calories_format, String.format(Locale.getDefault(), "%.1f", calories)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    stringResource(R.string.protein_format, String.format(Locale.getDefault(), "%.1f", protein)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    stringResource(R.string.carbs_format, String.format(Locale.getDefault(), "%.1f", carbs)),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    stringResource(R.string.fat_format, String.format(Locale.getDefault(), "%.1f", fat)),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            FTPrimaryButton(
                text = if (isEditing) stringResource(R.string.update_diary) else stringResource(R.string.save_to_diary),
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
