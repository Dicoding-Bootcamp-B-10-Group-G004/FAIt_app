package com.example.food_tracker.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Tambahkan import ini

@Composable
fun ProfileScreen(
    // Tambahkan parameter ViewModel agar tersambung ke logic
    viewModel: ProfileViewModel = viewModel()
) {
    val state = viewModel.state
    val lemonWhite = Color(0xFFFFFDF0)

    Scaffold(
        containerColor = lemonWhite,
        topBar = {
            ProfileTopBar()
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Section 1: BMI Circle - Mengambil data dari state.result
            item {
                BMICard(bmiScore = state.result, status = "Normal Weight")
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Section 2: Nutrition Card - Mengambil data gizi otomatis dari state
            item {
                NutritionCard(
                    calories = state.result,
                    protein = state.protein,
                    carbs = state.carbs,
                    fat = state.fat
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Section 3: User Data List - Mengambil data input user dari state
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileInfoItem(Icons.Rounded.DirectionsRun, "Activity", "Active")
                    ProfileInfoItem(Icons.Rounded.Flag, "Goal", "Maintain Weight")
                    ProfileInfoItem(Icons.Rounded.MonitorWeight, "Weight", "${state.weight} kg")
                    ProfileInfoItem(Icons.Rounded.Height, "Height", "${state.height} cm")
                    ProfileInfoItem(Icons.Rounded.Cake, "Age", "${state.age} years")
                    ProfileInfoItem(
                        Icons.Rounded.Male,
                        "Gender",
                        if (state.isMale) "Male" else "Female"
                    )
                }
            }
        }
    }
}

@Composable
fun NutritionCard(calories: String, protein: String, carbs: String, fat: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF2E7D32),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Daily Calorie Goal",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "$calories kcal",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    Icons.Rounded.Whatshot,
                    contentDescription = null,
                    tint = Color(0xFFFFCC80),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f)) // Ganti Divider ke HorizontalDivider (M3)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroItem("Protein", protein)
                MacroItem("Carbs", carbs)
                MacroItem("Fat", fat)
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar() {
    CenterAlignedTopAppBar(
        title = { Text("Profile", fontWeight = FontWeight.ExtraBold) },
        navigationIcon = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Rounded.AccountCircle, contentDescription = null)
            }
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Rounded.Settings, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun BMICard(bmiScore: String, status: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .background(Color(0xFFB9F6CA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = bmiScore,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = "BMI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20).copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = status,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Rounded.HelpOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp).padding(start = 4.dp),
                tint = Color.Gray
            )
        }
        Text(
            text = "Risk of comorbidities: Average",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF2E7D32))
            }
        }

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}