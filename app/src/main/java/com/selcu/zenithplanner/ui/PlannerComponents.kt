@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcu.zenithplanner.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcu.zenithplanner.R
import com.selcu.zenithplanner.data.HabitEntity
import com.selcu.zenithplanner.data.HabitLogEntity
import com.selcu.zenithplanner.data.TaskEntity
import com.selcu.zenithplanner.ui.theme.PlannerBlack
import com.selcu.zenithplanner.ui.theme.PlannerGold
import com.selcu.zenithplanner.ui.theme.PlannerInk
import com.selcu.zenithplanner.ui.theme.PlannerMuted
import com.selcu.zenithplanner.ui.theme.PlannerPaper
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ScreenColumn(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PlannerBlack)
            Text(subtitle, color = PlannerMuted)
        }
        content()
        Spacer(Modifier.height(64.dp))
    }
}

@Composable
internal fun PlannerCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PlannerPaper, contentColor = PlannerInk),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            content()
        }
    }
}

@Composable
internal fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = PlannerBlack
    )
}

@Composable
internal fun MetricCard(label: String, progress: Float, value: String) {
    PlannerCard {
        Text(label, color = PlannerMuted)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = PlannerGold,
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
internal fun ProLimitBanner(message: String, onUpgrade: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PlannerBlack, contentColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, PlannerGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = PlannerGold, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(message, color = Color(0xFFE8DBC0))
            }
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = PlannerGold, contentColor = PlannerBlack)
            ) {
                Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.upgrade_to_pro), fontWeight = FontWeight.Bold)
            }
        }
    }
}

internal fun YearMonth.calendarCells(): List<LocalDate> {
    val first = atDay(1)
    val start = first.minusDays((first.dayOfWeek.value - 1).toLong())
    return (0 until 42).map { start.plusDays(it.toLong()) }
}

internal fun LocalDate.longLabel(): String =
    format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))

internal fun List<TaskEntity>.completion(): Float =
    if (isEmpty()) 0f else count { it.isDone }.toFloat() / size

internal fun List<HabitEntity>.completion(logs: List<HabitLogEntity>): Float =
    if (isEmpty()) 0f else logs.count { it.isDone }.toFloat() / size

internal fun List<Float>.averageOrZero(): Float =
    if (isEmpty()) 0f else average().toFloat()

internal fun habitIcon(habit: HabitEntity): ImageVector = when (habit.icon) {
    "Water" -> Icons.Outlined.WaterDrop
    "FitnessCenter" -> Icons.Outlined.FitnessCenter
    "Spa" -> Icons.Outlined.Spa
    "MenuBook" -> Icons.Outlined.MenuBook
    "Work" -> Icons.Outlined.Work
    else -> Icons.Outlined.CheckCircle
}

internal fun money(amount: Double): String {
    val locale = Locale.getDefault()
    return try {
        java.text.NumberFormat.getCurrencyInstance(locale).format(amount)
    } catch (_: Exception) {
        "%.2f".format(amount)
    }
}

internal tailrec fun Context.findActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
