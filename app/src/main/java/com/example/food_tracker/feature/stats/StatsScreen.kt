package com.example.food_tracker.feature.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.food_tracker.R
import com.example.food_tracker.core.ui.components.FTCard
import com.example.food_tracker.core.ui.components.FTCenterTopBar
import com.example.food_tracker.domain.model.DietHistory
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state = viewModel.state
    val themeColors = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = themeColors.background,
        topBar = {
            FTCenterTopBar(
                title = stringResource(R.string.nutrition_stats),
                onTopBarClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    DietarySuggestionSection(
                        suggestion = state.dietarySuggestion,
                        isLoading = state.isSuggestionLoading,
                        onRefresh = { viewModel.getDietarySuggestion() }
                    )
                }

                item {
                    SectionTitle(stringResource(R.string.weekly_accomplishments))
                    AccomplishmentBarChart(state.weeklyStatsList)
                }

                item {
                    SectionTitle(stringResource(R.string.monthly_accomplishments))
                    AccomplishmentBarChart(state.monthlyStatsList)
                }

                item {
                    SectionTitle(stringResource(R.string.calorie_trend))
                    CalorieTrendChart(state.graphData)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun DietarySuggestionSection(
    suggestion: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    FTCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ai_dietary_suggestion),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.refresh_suggestion),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (suggestion != null) {
                MarkdownText(
                    markdown = suggestion,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            } else if (!isLoading) {
                Text(
                    text = stringResource(R.string.no_suggestion_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun AccomplishmentBarChart(statsList: List<GoalStats>) {
    if (statsList.isEmpty()) return

    val labels = statsList.map { it.label }
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    val calorieLabel = stringResource(R.string.legend_calories)
    val proteinLabel = stringResource(R.string.legend_protein)
    val carbsLabel = stringResource(R.string.legend_carbs)
    val fatLabel = stringResource(R.string.legend_fat)

    FTCard {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(calorieLabel, Color(0xFFE57373))
                LegendItem(proteinLabel, Color(0xFF81C784))
                LegendItem(carbsLabel, Color(0xFF64B5F6))
                LegendItem(fatLabel, Color(0xFFFFB74D))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        setDrawGridBackground(false)
                        setDrawBarShadow(false)
                        setDrawValueAboveBar(true)
                        setPinchZoom(false)
                        setScaleEnabled(false)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            textColor = onSurfaceColor
                            valueFormatter = IndexAxisValueFormatter(labels)
                        }

                        axisLeft.apply {
                            setDrawGridLines(true)
                            axisMinimum = 0f
                            textColor = onSurfaceColor
                        }

                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val calorieEntries = statsList.mapIndexed { index, stat -> BarEntry(index.toFloat(), stat.caloriesReachedCount.toFloat()) }
                    val proteinEntries = statsList.mapIndexed { index, stat -> BarEntry(index.toFloat(), stat.proteinReachedCount.toFloat()) }
                    val carbsEntries = statsList.mapIndexed { index, stat -> BarEntry(index.toFloat(), stat.carbsReachedCount.toFloat()) }
                    val fatEntries = statsList.mapIndexed { index, stat -> BarEntry(index.toFloat(), stat.fatReachedCount.toFloat()) }

                    val calorieSet = BarDataSet(calorieEntries, calorieLabel).apply { color = Color(0xFFE57373).toArgb(); setDrawValues(false) }
                    val proteinSet = BarDataSet(proteinEntries, proteinLabel).apply { color = Color(0xFF81C784).toArgb(); setDrawValues(false) }
                    val carbsSet = BarDataSet(carbsEntries, carbsLabel).apply { color = Color(0xFF64B5F6).toArgb(); setDrawValues(false) }
                    val fatSet = BarDataSet(fatEntries, fatLabel).apply { color = Color(0xFFFFB74D).toArgb(); setDrawValues(false) }

                    val data = BarData(calorieSet, proteinSet, carbsSet, fatSet)
                    
                    val groupSpace = 0.08f
                    val barSpace = 0.03f
                    val barWidth = 0.2f

                    data.barWidth = barWidth
                    chart.data = data
                    chart.groupBars(0f, groupSpace, barSpace)
                    chart.xAxis.axisMinimum = 0f
                    chart.xAxis.axisMaximum = 0f + chart.barData.getGroupWidth(groupSpace, barSpace) * statsList.size
                    chart.xAxis.setCenterAxisLabels(true)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun CalorieTrendChart(data: List<DietHistory>) {
    if (data.isEmpty()) return

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val dateTimeFormatter = SimpleDateFormat("d/M", Locale.getDefault())
    val goalLabel = stringResource(R.string.legend_goal)
    val consumedLabel = stringResource(R.string.legend_consumed)

    FTCard {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(goalLabel, Color.Red)
                LegendItem(consumedLabel, Color.Blue)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        setDrawGridBackground(false)
                        setPinchZoom(true)
                        setScaleEnabled(true)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            textColor = onSurfaceColor
                            granularity = 1f
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return data.getOrNull(value.toInt())?.let {
                                        try {
                                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                                            if (date != null) dateTimeFormatter.format(date) else ""
                                        } catch (e: Exception) { "" }
                                    } ?: ""
                                }
                            }
                        }

                        axisLeft.apply {
                            setDrawGridLines(true)
                            textColor = onSurfaceColor
                        }

                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val goalEntries = data.mapIndexed { index, history -> Entry(index.toFloat(), history.calorieGoal.toFloat()) }
                    val consumedEntries = data.mapIndexed { index, history -> Entry(index.toFloat(), history.totalCalories.toFloat()) }

                    val goalSet = LineDataSet(goalEntries, goalLabel).apply {
                        color = Color.Red.toArgb()
                        setCircleColor(Color.Red.toArgb())
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        setDrawValues(true)
                        valueTextColor = onSurfaceColor
                        valueTextSize = 10f
                    }

                    val consumedSet = LineDataSet(consumedEntries, consumedLabel).apply {
                        color = Color.Blue.toArgb()
                        setCircleColor(Color.Blue.toArgb())
                        lineWidth = 2f
                        circleRadius = 3f
                        setDrawCircleHole(false)
                        setDrawValues(true)
                        valueTextColor = onSurfaceColor
                        valueTextSize = 10f
                    }

                    chart.data = LineData(goalSet, consumedSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = color,
            shape = MaterialTheme.shapes.extraSmall
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
