@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcu.zenithplanner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.selcu.zenithplanner.R
import com.selcu.zenithplanner.data.HabitEntity
import com.selcu.zenithplanner.ui.theme.PlannerBlack
import com.selcu.zenithplanner.ui.theme.PlannerGold
import com.selcu.zenithplanner.ui.theme.PlannerInk
import com.selcu.zenithplanner.ui.theme.PlannerMuted
import com.selcu.zenithplanner.ui.theme.PlannerPaper
import kotlin.math.roundToInt

internal val habitIconOptions = listOf("Water", "FitnessCenter", "Spa", "MenuBook", "Work", "CheckCircle")

@Composable
internal fun HabitsScreen(viewModel: PlannerViewModel) {
    val habits by viewModel.habits.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()
    val streaks by viewModel.habitStreaks.collectAsState()
    val doneIds = logs.filter { it.isDone }.map { it.habitId }.toSet()
    val completion = habits.completion(logs)
    var showAddForm by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newIcon by remember { mutableStateOf("CheckCircle") }

    ScreenColumn(title = stringResource(R.string.habits), subtitle = "${(completion * 100).roundToInt()}% ${stringResource(R.string.completion)}") {
        PlannerCard {
            LinearProgressIndicator(
                progress = { completion },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = PlannerGold
            )
        }
        if (showAddForm) {
            PlannerCard {
                SectionHeader(stringResource(R.string.add_habit))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.habit_name)) }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.category)) }
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.icon), color = PlannerMuted, style = MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    habitIconOptions.forEach { icon ->
                        FilterChip(
                            selected = newIcon == icon,
                            onClick = { newIcon = icon },
                            label = { Icon(habitIcon(HabitEntity(name = "", category = "", icon = icon)), contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        viewModel.addHabit(newName, newCategory.ifBlank { "General" }, newIcon)
                        newName = ""; newCategory = ""; newIcon = "CheckCircle"; showAddForm = false
                    }) {
                        Text(stringResource(R.string.add))
                    }
                    Button(
                        onClick = { showAddForm = false; newName = ""; newCategory = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = PlannerInk)
                    ) { Text(stringResource(R.string.back)) }
                }
            }
        } else {
            Button(
                onClick = { showAddForm = true },
                colors = ButtonDefaults.buttonColors(containerColor = PlannerInk, contentColor = Color.White)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_habit))
            }
        }
        if (habits.isEmpty()) {
            Text(stringResource(R.string.no_habits), color = PlannerMuted, modifier = Modifier.padding(vertical = 16.dp))
        } else {
            habits.forEach { habit ->
                HabitCard(
                    habit = habit,
                    checked = habit.id in doneIds,
                    streak = streaks[habit.id] ?: 0,
                    onToggle = { viewModel.setHabitDone(habit.id, habit.id !in doneIds) },
                    onDelete = { viewModel.deleteHabit(habit.id) }
                )
            }
        }
    }
}

@Composable
private fun HabitCard(habit: HabitEntity, checked: Boolean, streak: Int, onToggle: () -> Unit, onDelete: () -> Unit) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(8.dp),
        color = if (checked) PlannerBlack else PlannerPaper,
        contentColor = if (checked) Color.White else PlannerInk,
        border = BorderStroke(1.dp, if (checked) PlannerGold else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Spacer(Modifier.height(8.dp))
                Icon(habitIcon(habit), contentDescription = null, tint = if (checked) PlannerGold else PlannerMuted)
                Text(habit.name, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Text(habit.category, color = if (checked) Color(0xFFE8DBC0) else PlannerMuted, style = MaterialTheme.typography.bodySmall)
                if (streak > 0) {
                    Text(
                        stringResource(R.string.streak_days, streak),
                        color = PlannerGold,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (checked) Color(0xFFE8DBC0) else PlannerMuted
                )
            }
        }
    }
}
