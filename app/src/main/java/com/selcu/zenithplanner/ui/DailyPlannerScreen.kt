@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcu.zenithplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcu.zenithplanner.R
import com.selcu.zenithplanner.data.ScheduleEntryEntity
import com.selcu.zenithplanner.ui.theme.PlannerGold

@Composable
internal fun DailyPlannerScreen(viewModel: PlannerViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyPlan by viewModel.dailyPlan.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val habitLogs by viewModel.habitLogs.collectAsState()
    val doneHabitIds = habitLogs.filter { it.isDone }.map { it.habitId }.toSet()

    ScreenColumn(title = stringResource(R.string.daily_planner), subtitle = selectedDate.longLabel()) {
        PlannerCard {
            SectionHeader(stringResource(R.string.top_three_tasks))
            tasks.take(3).forEach { task ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Checkbox(checked = task.isDone, onCheckedChange = { viewModel.updateTaskDone(task.id, it) })
                    OutlinedTextField(
                        value = task.title,
                        onValueChange = { viewModel.updateTaskTitle(task.id, it) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.title)) }
                    )
                }
            }
        }
        PlannerCard {
            SectionHeader(stringResource(R.string.habits_today))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                habits.forEach { habit ->
                    FilterChip(
                        selected = habit.id in doneHabitIds,
                        onClick = { viewModel.setHabitDone(habit.id, habit.id !in doneHabitIds) },
                        label = { Text(habit.name) },
                        leadingIcon = { Icon(habitIcon(habit), contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }
        }
        PlannerCard {
            SectionHeader(stringResource(R.string.hourly_schedule))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(420.dp)
            ) {
                items(schedule) { entry ->
                    ScheduleRow(entry = entry, onChange = { viewModel.updateScheduleTitle(entry.id, it) })
                }
            }
        }
        PlannerCard {
            SectionHeader(stringResource(R.string.daily_notes))
            OutlinedTextField(
                value = dailyPlan?.notes.orEmpty(),
                onValueChange = viewModel::updateNotes,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.note_body)) }
            )
            Spacer(Modifier.height(12.dp))
            SectionHeader(stringResource(R.string.reflection_question))
            OutlinedTextField(
                value = dailyPlan?.reflection.orEmpty(),
                onValueChange = viewModel::updateReflection,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScheduleRow(entry: ScheduleEntryEntity, onChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "%02d:00".format(entry.hour),
            color = PlannerGold,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(64.dp)
        )
        OutlinedTextField(
            value = entry.title,
            onValueChange = onChange,
            singleLine = true,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.title)) }
        )
    }
}
