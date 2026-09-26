@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcu.zenithplanner.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcu.zenithplanner.R
import com.selcu.zenithplanner.data.GoalEntity
import com.selcu.zenithplanner.subscription.FREE_GOALS_LIMIT
import com.selcu.zenithplanner.ui.theme.PlannerGold
import com.selcu.zenithplanner.ui.theme.PlannerInk
import com.selcu.zenithplanner.ui.theme.PlannerMuted
import kotlin.math.roundToInt

@Composable
internal fun GoalsScreen(viewModel: PlannerViewModel, onUpgrade: () -> Unit) {
    val goals by viewModel.goals.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val atLimit = !isPro && goals.size >= FREE_GOALS_LIMIT
    var showAddForm by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("") }
    var newHorizon by remember { mutableStateOf("MONTHLY") }

    val horizonLabels = mapOf("MONTHLY" to R.string.monthly_goals, "90_DAY" to R.string.ninety_day_plan, "LIFE" to R.string.life_goals)

    ScreenColumn(title = stringResource(R.string.goals), subtitle = stringResource(R.string.progress)) {
        if (atLimit) {
            ProLimitBanner(
                message = stringResource(R.string.pro_limit_goals, FREE_GOALS_LIMIT),
                onUpgrade = onUpgrade
            )
        } else if (showAddForm) {
            PlannerCard {
                SectionHeader(stringResource(R.string.add_goal))
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.title)) }
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
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    horizonLabels.forEach { (key, res) ->
                        FilterChip(
                            selected = newHorizon == key,
                            onClick = { newHorizon = key },
                            label = { Text(stringResource(res)) }
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        viewModel.addGoal(newTitle, newCategory.ifBlank { "Personal" }, newHorizon)
                        newTitle = ""; newCategory = ""; showAddForm = false
                    }) { Text(stringResource(R.string.add)) }
                    Button(
                        onClick = { showAddForm = false; newTitle = ""; newCategory = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = PlannerInk)
                    ) { Text(stringResource(R.string.back)) }
                }
            }
        } else {
            Button(
                onClick = { showAddForm = true },
                colors = ButtonDefaults.buttonColors(containerColor = PlannerInk, contentColor = androidx.compose.ui.graphics.Color.White)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_goal))
            }
        }
        if (goals.isEmpty()) {
            Text(stringResource(R.string.no_goals), color = PlannerMuted, modifier = Modifier.padding(vertical = 16.dp))
        } else {
            GoalGroup(R.string.monthly_goals, goals.filter { it.horizon == "MONTHLY" }, viewModel)
            GoalGroup(R.string.ninety_day_plan, goals.filter { it.horizon == "90_DAY" }, viewModel)
            GoalGroup(R.string.life_goals, goals.filter { it.horizon == "LIFE" }, viewModel)
        }
    }
}

@Composable
private fun GoalGroup(@StringRes title: Int, goals: List<GoalEntity>, viewModel: PlannerViewModel) {
    if (goals.isEmpty()) return
    SectionHeader(stringResource(title))
    goals.forEach { goal ->
        var showNotes by remember(goal.id) { mutableStateOf(goal.notes.isNotBlank()) }
        var editNotes by remember(goal.id) { mutableStateOf(goal.notes) }
        PlannerCard {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(goal.title, fontWeight = FontWeight.SemiBold)
                    Text(goal.category, color = PlannerMuted, style = MaterialTheme.typography.bodySmall)
                }
                Text("${(goal.progress * 100).roundToInt()}%", color = PlannerGold, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showNotes = !showNotes }) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = PlannerMuted, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = PlannerMuted, modifier = Modifier.size(18.dp))
                }
            }
            Slider(value = goal.progress, onValueChange = { viewModel.updateGoalProgress(goal.id, it) })
            if (showNotes) {
                OutlinedTextField(
                    value = editNotes,
                    onValueChange = {
                        editNotes = it
                        viewModel.updateGoalNotes(goal.id, it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text(stringResource(R.string.goal_notes_hint)) }
                )
            }
        }
    }
}
