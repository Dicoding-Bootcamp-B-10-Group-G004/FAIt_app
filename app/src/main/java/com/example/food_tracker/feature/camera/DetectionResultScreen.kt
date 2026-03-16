package com.example.food_tracker.feature.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.data.ml.SnappedResult
import com.example.food_tracker.feature.home.HomeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionResultScreen(
    snappedResult: SnappedResult,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    val softGreen = Color(0xFFB9F6CA)
    
    // Get distinct labels and map to Food objects from repository
    val distinctLabels = remember(snappedResult.result.detections) {
        snappedResult.result.detections.map { it.label }.distinct()
    }
    
    val detectedFoods = remember(distinctLabels, homeViewModel) {
        distinctLabels.mapNotNull { label ->
            homeViewModel.getFoodByName(label)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detection Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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

            item {
                Text(
                    text = "Found ${detectedFoods.size} items",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(detectedFoods) { food ->
                var portion by remember { mutableStateOf("1") }
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(food.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    "${food.calories} kcal | P: ${food.protein}g | C: ${food.carbs}g",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            OutlinedTextField(
                                value = portion,
                                onValueChange = { portion = it },
                                modifier = Modifier.width(70.dp),
                                label = { Text("Portion", fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(
                                onClick = {
                                    val p = portion.toIntOrNull() ?: 1
                                    homeViewModel.addFoodDirect(food, p)
                                },
                                modifier = Modifier.background(
                                    softGreen.copy(alpha = 0.2f),
                                    CircleShape
                                )
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Take Another Picture")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
