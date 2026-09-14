package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun GoalSettingDialog(
    currentDailyHours: Float,
    currentWeeklyHours: Float,
    onSave: (dailyHours: Float, weeklyHours: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var dailyGoal by remember { mutableFloatStateOf(currentDailyHours) }
    var weeklyGoal by remember { mutableFloatStateOf(currentWeeklyHours) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Set Study & Focus Goals", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(
                    text = "Daily Focus Goal: ${"%.1f".format(dailyGoal)} hours",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Recommended for students: 2.5 - 4 hours daily",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = dailyGoal,
                    onValueChange = { dailyGoal = (it * 2).roundToInt() / 2f },
                    valueRange = 0.5f..8.0f,
                    steps = 14
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Weekly Focus Goal: ${weeklyGoal.roundToInt()} hours",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Recommended for students: 15 - 25 hours weekly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = weeklyGoal,
                    onValueChange = { weeklyGoal = it.roundToInt().toFloat() },
                    valueRange = 5f..40f,
                    steps = 34
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(dailyGoal, weeklyGoal) },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Goals")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}
