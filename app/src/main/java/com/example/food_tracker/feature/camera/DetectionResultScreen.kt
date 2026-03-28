package com.example.food_tracker.feature.camera

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.R
import com.example.food_tracker.core.ui.components.FTFoodListItem
import com.example.food_tracker.core.ui.components.FTIconButton
import com.example.food_tracker.core.ui.components.FTTopBar
import com.example.food_tracker.data.ml.SnappedResult
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.feature.home.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionResultScreen(
    snappedResult: SnappedResult,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit,
    onFoodClick: (Food) -> Unit
) {
    val context = LocalContext.current
    val themeColors = MaterialTheme.colorScheme
    val detectedFoods = remember { mutableStateListOf<Food>() }
    
    val foodAddedDirectMsg = stringResource(R.string.food_added_direct_success)
    val lunchLabel = stringResource(R.string.lunch)

    LaunchedEffect(snappedResult) {
        val labels = snappedResult.result.detections.map { it.label }.distinct()
        val foods = labels.mapNotNull { label ->
            homeViewModel.getFoodByName(label)
        }
        detectedFoods.clear()
        detectedFoods.addAll(foods)
    }

    Scaffold(
        containerColor = themeColors.background,
        topBar = {
            FTTopBar(
                title = { 
                    Text(
                        stringResource(R.string.detection_results), 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "image_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box {
                        Image(
                            bitmap = snappedResult.bitmap.asImageBitmap(),
                            contentDescription = "Snapped Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        DetectionOverlay(detections = snappedResult.result.detections)
                    }
                }
            }

            item(key = "title") {
                Text(
                    text = stringResource(R.string.detected_foods),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.onBackground
                )
            }

            if (detectedFoods.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_matching_foods),
                            style = MaterialTheme.typography.bodyMedium,
                            color = themeColors.outline
                        )
                    }
                }
            }

            items(
                items = detectedFoods,
                key = { it.name },
                contentType = { "detected_food" }
            ) { food ->
                FTFoodListItem(
                    name = food.name,
                    portion = food.portion,
                    calories = food.calories,
                    protein = food.protein,
                    carbs = food.carbs,
                    fat = food.fat,
                    onClick = { onFoodClick(food) },
                    trailingContent = {
                        IconButton(
                            onClick = { 
                                homeViewModel.addFoodDirect(food, food.portion, "Lunch")
                                Toast.makeText(context, foodAddedDirectMsg.format(food.name), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.background(themeColors.secondary.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add")
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.retake_photo),
                        style = MaterialTheme.typography.labelLarge,
                        color = themeColors.outline
                    )
                }
            }
        }
    }
}
