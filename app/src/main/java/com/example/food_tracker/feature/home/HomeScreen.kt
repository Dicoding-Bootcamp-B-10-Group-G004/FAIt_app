package com.example.food_tracker.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.feature.addfood.AddFoodEvent
import com.example.food_tracker.feature.addfood.AddFoodViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    addFoodViewModel: AddFoodViewModel
) {

    val softGreen = Color(0xFFB9F6CA)
    val darkGreen = Color(0xFF2E7D32)

    val homeState = homeViewModel.state
    val addFoodState = addFoodViewModel.state

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }

    // state portion
    var portion by remember { mutableStateOf("1") }

    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date())

    Scaffold(
        containerColor = Color(0xFFFFFDF0),

        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today", fontSize = 14.sp, color = Color.Gray)
                        Text(formattedDate, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }

    ) { padding ->

        if (showSheet) {

            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {

                Column(
                    modifier = Modifier.padding(
                        bottom = 32.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                ) {

                    Text(
                        "Select Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { category ->

                        ListItem(
                            headlineContent = { Text(category) },

                            leadingContent = {

                                val iconVector = when (category) {
                                    "Breakfast" -> Icons.Rounded.LightMode
                                    "Lunch" -> Icons.Rounded.WbSunny
                                    else -> Icons.Rounded.NightsStay
                                }

                                Icon(iconVector, contentDescription = null)
                            },

                            modifier = Modifier.clickable {

                                selectedCategory = category

                                scope.launch { sheetState.hide() }
                                    .invokeOnCompletion { showSheet = false }
                            }
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding = PaddingValues(20.dp),

            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items((9..15).toList()) { day ->

                        CalendarItem(
                            day = day.toString(),
                            isSelected = day == today.get(Calendar.DAY_OF_MONTH),
                            darkGreen = darkGreen
                        )
                    }
                }
            }

            item {

                MainCalorieCard(
                    accentColor = softGreen,
                    supplied = homeState.suppliedCalories.toInt().toString(),
                    left = (2567 - homeState.suppliedCalories.toInt())
                        .coerceAtLeast(0)
                        .toString(),
                    progress = (homeState.suppliedCalories.toFloat() / 2567f)
                )
            }

            item {

                Column {

                    if (selectedCategory.isNotEmpty()) {

                        AssistChip(
                            onClick = { selectedCategory = "" },
                            label = { Text("Adding to: $selectedCategory") },

                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }

                    OutlinedTextField(
                        value = addFoodState.searchQuery,

                        onValueChange = {
                            addFoodViewModel.onEvent(
                                AddFoodEvent.OnSearchQueryChange(it)
                            )
                        },

                        modifier = Modifier.fillMaxWidth(),

                        placeholder = { Text("Search food...") },

                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null)
                        },

                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = portion,
                        onValueChange = { portion = it },
                        modifier = Modifier.width(120.dp),
                        label = { Text("Portion") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            if (addFoodState.searchQuery.isNotEmpty()) {

                items(addFoodState.searchResults) { food ->

                    FoodResultItem(food = food, accentColor = softGreen) {

                        val p = portion.toIntOrNull() ?: 1

                        addFoodViewModel.addFoodWithPortion(food, p)

                        // FIX supaya calories ikut update
                        homeViewModel.addFoodDirect(food, p)

                        addFoodViewModel.onEvent(
                            AddFoodEvent.OnSearchQueryChange("")
                        )

                        portion = "1"

                        selectedCategory = ""
                    }
                }
            }

            item {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    MacroItem(
                        "Carbs",
                        "${homeState.carbsCount.toInt()}/385g",
                        (homeState.carbsCount.toFloat() / 385f),
                        Modifier.weight(1f)
                    )

                    MacroItem(
                        "Fat",
                        "${homeState.fatCount.toInt()}/71g",
                        (homeState.fatCount.toFloat() / 71f),
                        Modifier.weight(1f)
                    )

                    MacroItem(
                        "Protein",
                        "${homeState.proteinCount.toInt()}/96g",
                        (homeState.proteinCount.toFloat() / 96f),
                        Modifier.weight(1f)
                    )
                }
            }

            item {
                MealCategoryCard("Breakfast", "0 kcal", Icons.Rounded.LightMode) {
                    showSheet = true
                }
            }

            item {
                MealCategoryCard("Lunch", "0 kcal", Icons.Rounded.WbSunny) {
                    showSheet = true
                }
            }
        }
    }
}

@Composable
fun CalendarItem(day: String, isSelected: Boolean, darkGreen: Color) {

    Column(
        modifier = Modifier
            .width(55.dp)
            .background(
                if (isSelected) darkGreen else Color.White,
                RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Mar", fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.Gray)

        Text(
            day,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

@Composable
fun MainCalorieCard(
    accentColor: Color,
    supplied: String,
    left: String,
    progress: Float
) {

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

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {

                CircularProgressIndicator(
                    progress = progress.coerceIn(0f,1f),
                    modifier = Modifier.fillMaxSize(),
                    color = accentColor,
                    strokeWidth = 10.dp,
                    trackColor = accentColor.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(left, fontWeight = FontWeight.Black, fontSize = 28.sp)

                    Text("kcal left", fontSize = 14.sp, color = Color.Gray)

                    Text("Supplied: $supplied",
                        fontSize = 10.sp,
                        color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun MacroItem(label:String,value:String,progress:Float,modifier:Modifier){

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ){

        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Text(label,fontSize = 12.sp,fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f,1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = Color(0xFFB9F6CA),
                trackColor = Color(0xFFB9F6CA).copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(value,fontSize = 10.sp,color = Color.Gray)
        }
    }
}

@Composable
fun FoodResultItem(food:Food,accentColor:Color,onAddClick:()->Unit){

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ){

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){

            Column(modifier = Modifier.weight(1f)){

                Text(food.name,fontWeight = FontWeight.Bold)

                Text(
                    "${food.calories} kcal | P: ${food.protein}g | C: ${food.carbs}g",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onAddClick,
                modifier = Modifier.background(
                    accentColor.copy(alpha = 0.2f),
                    CircleShape
                )
            ){

                Icon(Icons.Rounded.Add,contentDescription = null)
            }
        }
    }
}

@Composable
fun MealCategoryCard(title:String,kcal:String,icon:ImageVector,onAdd:()->Unit){

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ){

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){

            Row(verticalAlignment = Alignment.CenterVertically){

                Icon(icon,contentDescription = null,tint = Color.Gray)

                Spacer(modifier = Modifier.width(12.dp))

                Text(title,fontWeight = FontWeight.Bold)
            }

            Row(verticalAlignment = Alignment.CenterVertically){

                Text(kcal,color = Color.Gray)

                IconButton(onClick = onAdd){

                    Icon(Icons.Rounded.Add,contentDescription = null)
                }
            }
        }
    }
}