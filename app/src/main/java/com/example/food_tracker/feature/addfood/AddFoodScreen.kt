package com.example.food_tracker.feature.addfood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: AddFoodViewModel,
    onNavigateBack: () -> Unit
) {
    val state = viewModel.state // Pastikan state diambil dari viewModel

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onEvent(AddFoodEvent.OnSearchQueryChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search food...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.searchResults) { foodItem ->
                FoodItemCard(
                    food = foodItem,
                    onAddClick = {
                        viewModel.onEvent(AddFoodEvent.OnFoodSelected(foodItem))
                    }
                )
            }
        }
    }
}

@Composable
fun FoodItemCard(
    food: com.example.food_tracker.domain.model.Food,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FIX: Modifier.weight harus pakai Float (1f), bukan Dp (1.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = food.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${food.calories} kcal | P: ${food.protein}g | C: ${food.carbs}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onAddClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFFF1F8E9)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}