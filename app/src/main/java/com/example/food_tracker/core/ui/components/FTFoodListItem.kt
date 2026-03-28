package com.example.food_tracker.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.R
import java.util.Locale

@Composable
fun FTFoodListItem(
    name: String,
    portion: Double,
    calories: Double,
    protein: Double,
    carbs: Double,
    fat: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    onAddClick: (() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val proteinLabel = stringResource(R.string.protein_short, String.format(Locale.getDefault(), "%.1f", protein))
                val carbsLabel = stringResource(R.string.carbs_short, String.format(Locale.getDefault(), "%.1f", carbs))
                val fatLabel = stringResource(R.string.fat_short, String.format(Locale.getDefault(), "%.1f", fat))
                val portionInfo = stringResource(R.string.portion_format, portion.toInt(), calories.toInt())

                Text(
                    text = "$portionInfo\n$proteinLabel | $carbsLabel | $fatLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 18.sp
                )
            }
            
            if (onAddClick != null) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.background(accentColor.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.capture))
                }
            } else if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
