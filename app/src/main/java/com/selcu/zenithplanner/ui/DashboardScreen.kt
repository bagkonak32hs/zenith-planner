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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcu.zenithplanner.R
import com.selcu.zenithplanner.data.QuoteEntity
import com.selcu.zenithplanner.ui.theme.PlannerBlack
import com.selcu.zenithplanner.ui.theme.PlannerGold
import com.selcu.zenithplanner.ui.theme.PlannerInk
import com.selcu.zenithplanner.ui.theme.PlannerMuted
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
internal fun DashboardScreen(viewModel: PlannerViewModel, onNavigate: (PlannerDestination) -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val habitLogs by viewModel.habitLogs.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val quotes by viewModel.quotes.collectAsState()
    val quote = quotes.firstOrNull()
    val taskProgress = tasks.completion()
    val habitProgress = habits.completion(habitLogs)

    ScreenColumn(title = stringResource(R.string.dashboard), subtitle = selectedDate.longLabel()) {
        HeroPanel(quote = quote, selectedDate = selectedDate)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(stringResource(R.string.top_three_tasks), taskProgress, "${(taskProgress * 100).roundToInt()}%")
            MetricCard(stringResource(R.string.habits_today), habitProgress, "${(habitProgress * 100).roundToInt()}%")
            MetricCard(stringResource(R.string.goals), goals.map { it.progress }.averageOrZero(), "${goals.size}")
        }
        SectionHeader(stringResource(R.string.premium_focus))
        PlannerCard {
            tasks.take(3).forEach { task ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        if (task.isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (task.isDone) PlannerGold else PlannerMuted
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(task.title.ifBlank { stringResource(R.string.title) }, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DashboardButton(R.string.calendar, PlannerDestination.Calendar, onNavigate)
            DashboardButton(R.string.daily_planner, PlannerDestination.Daily, onNavigate)
            DashboardButton(R.string.goals, PlannerDestination.Goals, onNavigate)
            DashboardButton(R.string.habits, PlannerDestination.Habits, onNavigate)
            DashboardButton(R.string.motivation, PlannerDestination.Motivation, onNavigate)
            DashboardButton(R.string.notes, PlannerDestination.Notes, onNavigate)
            DashboardButton(R.string.finance, PlannerDestination.Finance, onNavigate)
            DashboardButton(R.string.settings, PlannerDestination.Settings, onNavigate)
        }
    }
}

@Composable
private fun HeroPanel(quote: QuoteEntity?, selectedDate: LocalDate) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PlannerBlack, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(), color = PlannerGold)
            Text(
                text = quote?.text ?: stringResource(R.string.reflection_question),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(quote?.author ?: stringResource(R.string.app_name), color = Color(0xFFE8DBC0))
        }
    }
}

@Composable
private fun DashboardButton(@StringRes label: Int, destination: PlannerDestination, onNavigate: (PlannerDestination) -> Unit) {
    Button(
        onClick = { onNavigate(destination) },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PlannerInk, contentColor = Color.White)
    ) {
        Icon(destination.icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(label))
    }
}
