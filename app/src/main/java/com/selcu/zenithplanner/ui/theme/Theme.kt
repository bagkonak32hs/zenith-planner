package com.selcu.zenithplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PlannerBlack = Color(0xFF11100E)
val PlannerInk = Color(0xFF25221D)
val PlannerCream = Color(0xFFF7F2E8)
val PlannerPaper = Color(0xFFFFFBF4)
val PlannerGold = Color(0xFFC9A24A)
val PlannerMuted = Color(0xFF766F62)
val PlannerRose = Color(0xFF8E4B4B)
val PlannerSage = Color(0xFF617462)

private val PlannerColorScheme: ColorScheme = lightColorScheme(
    primary = PlannerGold,
    onPrimary = PlannerBlack,
    secondary = PlannerSage,
    onSecondary = Color.White,
    tertiary = PlannerRose,
    onTertiary = Color.White,
    background = PlannerCream,
    onBackground = PlannerBlack,
    surface = PlannerPaper,
    onSurface = PlannerInk,
    surfaceVariant = Color(0xFFEDE5D7),
    onSurfaceVariant = PlannerMuted,
    outline = Color(0xFFD6C8B0),
    outlineVariant = Color(0xFFE7DCCB)
)

@Composable
fun ZenithPlannerTheme(content: @Composable () -> Unit) {
    val colorScheme = PlannerColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

