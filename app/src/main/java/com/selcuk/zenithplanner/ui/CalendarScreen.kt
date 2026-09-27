@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcuk.zenithplanner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.selcuk.zenithplanner.R
import com.selcuk.zenithplanner.ui.theme.PlannerBlack
import com.selcuk.zenithplanner.ui.theme.PlannerGold
import com.selcuk.zenithplanner.ui.theme.PlannerMuted
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
internal fun CalendarScreen(viewModel: PlannerViewModel, onOpenDay: () -> Unit) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val dailyPlan by viewModel.dailyPlan.collectAsState()
    val activeDates by viewModel.activeDates.collectAsState()
    var month by remember(selectedDate.year, selectedDate.monthValue) { mutableStateOf(YearMonth.from(selectedDate)) }
    val days = remember(month) { month.calendarCells() }

    ScreenColumn(title = stringResource(R.string.calendar), subtitle = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year) {
        PlannerCard {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { month = month.minusMonths(1) }) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = null)
                }
                Text(
                    text = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { month = month.plusMonths(1) }) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = null)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                    Text(it, color = PlannerMuted, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.height(330.dp)
            ) {
                items(days) { date ->
                    CalendarDayCell(
                        date = date,
                        selectedDate = selectedDate,
                        currentMonth = month,
                        hasActiveTasks = date.toString() in activeDates,
                        onSelect = {
                            viewModel.selectDate(it)
                            onOpenDay()
                        }
                    )
                }
            }
        }
        PlannerCard {
            SectionHeader(selectedDate.longLabel())
            tasks.take(3).forEach { task ->
                Text("• " + task.title.ifBlank { stringResource(R.string.top_three_tasks) })
            }
            Spacer(Modifier.height(8.dp))
            Text(dailyPlan?.notes?.ifBlank { stringResource(R.string.daily_notes) } ?: stringResource(R.string.daily_notes), color = PlannerMuted)
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    hasActiveTasks: Boolean,
    onSelect: (LocalDate) -> Unit
) {
    val isSelected = date == selectedDate
    val isCurrentMonth = date.month == currentMonth.month
    Surface(
        onClick = { onSelect(date) },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) PlannerBlack else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        border = if (date == LocalDate.now()) BorderStroke(1.dp, PlannerGold) else null,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = if (isCurrentMonth || isSelected) Color.Unspecified else PlannerMuted
                )
                if (hasActiveTasks) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) PlannerGold else PlannerGold.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

