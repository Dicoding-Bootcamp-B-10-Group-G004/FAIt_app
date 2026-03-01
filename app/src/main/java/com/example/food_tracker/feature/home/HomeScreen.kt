package com.example.food_tracker.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.domain.model.Food

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val lemonWhite = Color(0xFFFFFDF0)
    val softGreen = Color(0xFFB9F6CA)

    // State untuk Search Bar
    var searchText by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults

    Scaffold(
        containerColor = lemonWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Food Tracker", fontWeight = FontWeight.Black) },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Rounded.Settings, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Main Calorie Card
            item {
                MainCalorieCard(softGreen)
            }

            // Section 2: Search Bar (DITAMBAHKAN DISINI)
            item {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.searchFood(it) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari ") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = softGreen,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            // Section 3: Search Results (DITAMBAHKAN DISINI)
            if (searchText.isNotEmpty()) {
                items(searchResults) { food ->
                    FoodResultItem(food, softGreen)
                }
            }

            // Section 4: Macro Nutrients
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroItem("Carbs", "0/385g", 0.1f, Modifier.weight(1f))
                    MacroItem("Fat", "0/71g", 0.1f, Modifier.weight(1f))
                    MacroItem("Protein", "0/96g", 0.1f, Modifier.weight(1f))
                }
            }

            // Section 5: Meal Sections
            item { MealSection("Activity", Icons.Rounded.Add) }
            item { MealSection("Breakfast", Icons.Rounded.Add) }
            item { MealSection("Lunch", Icons.Rounded.Add) }
        }
    }
}

@Composable
fun FoodResultItem(food: Food, accentColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    "${food.calories} kcal | P: ${food.protein} | C: ${food.carbs}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(
                onClick = { /* Logic Tambah Kalori */ },
                modifier = Modifier.background(accentColor.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.DarkGray)
            }
        }
    }
}

// --- Komponen Pendukung Tetap Sama ---

@Composable
fun MainCalorieCard(accentColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                CircularProgressIndicator(
                    progress = { 0.7f },
                    modifier = Modifier.fillMaxSize(),
                    color = accentColor,
                    strokeWidth = 12.dp,
                    trackColor = accentColor.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "2567",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "kcal left",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieInfoVertical("Supplied", "0")
                Divider(modifier = Modifier.height(40.dp).width(1.dp))
                CalorieInfoVertical("Burned", "0")
            }
        }
    }
}

@Composable
fun CalorieInfoVertical(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MacroItem(label: String, value: String, progress: Float, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(32.dp),
                color = Color(0xFFB9F6CA),
                strokeWidth = 4.dp,
                trackColor = Color(0xFFB9F6CA).copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun MealSection(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Surface(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            onClick = { /* Action */ }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.LightGray
                )
            }
        }
    }
}