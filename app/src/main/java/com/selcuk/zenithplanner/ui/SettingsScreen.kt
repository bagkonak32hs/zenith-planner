@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcuk.zenithplanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.selcuk.zenithplanner.R
import com.selcuk.zenithplanner.notifications.ReminderPreferences
import com.selcuk.zenithplanner.notifications.ReminderScheduler
import com.selcuk.zenithplanner.ui.theme.PlannerBlack
import com.selcuk.zenithplanner.ui.theme.PlannerGold
import com.selcuk.zenithplanner.ui.theme.PlannerInk
import com.selcuk.zenithplanner.ui.theme.PlannerMuted

@Composable
internal fun SettingsScreen(viewModel: PlannerViewModel, onUpgrade: () -> Unit) {
    val context = LocalContext.current
    val isPro by viewModel.isPro.collectAsState()
    var currentTag by remember { mutableStateOf(AppCompatDelegate.getApplicationLocales().toLanguageTags()) }
    val syncState by viewModel.syncState.collectAsState()
    var reminder by remember { mutableStateOf(ReminderPreferences.read(context)) }
    var hourText by remember { mutableStateOf(reminder.hour.toString().padStart(2, '0')) }
    var minuteText by remember { mutableStateOf(reminder.minute.toString().padStart(2, '0')) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 8
            val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
            ReminderScheduler.schedule(context, hour, minute)
            reminder = ReminderPreferences.read(context)
        }
    }

    fun enableReminder() {
        val hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: 8
        val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: 0
        ReminderScheduler.schedule(context, hour, minute)
        reminder = ReminderPreferences.read(context)
        hourText = reminder.hour.toString().padStart(2, '0')
        minuteText = reminder.minute.toString().padStart(2, '0')
    }

    ScreenColumn(title = stringResource(R.string.settings), subtitle = stringResource(R.string.language)) {
        PlannerCard {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    if (isPro) Icons.Outlined.Star else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = if (isPro) PlannerGold else PlannerMuted,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    SectionHeader(stringResource(R.string.subscription))
                    Text(
                        text = stringResource(if (isPro) R.string.pro_active else R.string.free_plan),
                        color = if (isPro) PlannerGold else PlannerMuted
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (!isPro) {
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = PlannerGold, contentColor = PlannerBlack)
                ) {
                    Icon(Icons.Outlined.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.upgrade_to_pro))
                }
            }
        }
        PlannerCard {
            SectionHeader(stringResource(R.string.language))
            LanguageChip(label = stringResource(R.string.language_system), tag = "", currentTag = currentTag) { currentTag = it }
            LanguageChip(label = stringResource(R.string.language_english), tag = "en", currentTag = currentTag) { currentTag = it }
            LanguageChip(label = stringResource(R.string.language_turkish), tag = "tr", currentTag = currentTag) { currentTag = it }
            LanguageChip(label = stringResource(R.string.language_spanish), tag = "es", currentTag = currentTag) { currentTag = it }
            LanguageChip(label = stringResource(R.string.language_french), tag = "fr", currentTag = currentTag) { currentTag = it }
        }
        PlannerCard {
            SectionHeader(stringResource(R.string.daily_reminder))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = hourText,
                    onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                    label = { Text(stringResource(R.string.reminder_hour)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = minuteText,
                    onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                    label = { Text(stringResource(R.string.reminder_minute)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    if (reminder.enabled) {
                        ReminderScheduler.cancel(context)
                        reminder = ReminderPreferences.read(context)
                    } else if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        enableReminder()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (reminder.enabled) MaterialTheme.colorScheme.tertiary else PlannerInk,
                    contentColor = Color.White
                )
            ) {
                Text(stringResource(if (reminder.enabled) R.string.disable_reminder else R.string.enable_reminder))
            }
        }
        PlannerCard {
            SectionHeader(stringResource(R.string.sync))
            if (!isPro) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = PlannerMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.sync_pro_only), color = PlannerMuted)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = PlannerGold, contentColor = PlannerBlack)
                ) {
                    Text(stringResource(R.string.upgrade_to_pro))
                }
            } else {
                Text(stringResource(syncState.statusRes), color = PlannerMuted)
                syncState.userEmail?.let {
                    Text(it, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        enabled = !syncState.isBusy && syncState.isGoogleSignInConfigured && !syncState.isSignedIn,
                        onClick = { context.findActivity()?.let(viewModel::signInWithGoogle) }
                    ) {
                        Text(stringResource(R.string.sign_in_google))
                    }
                    Button(
                        enabled = !syncState.isBusy && syncState.isSignedIn,
                        onClick = viewModel::syncNow,
                        colors = ButtonDefaults.buttonColors(containerColor = PlannerInk, contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.sync_now))
                    }
                    Button(
                        enabled = !syncState.isBusy && syncState.isSignedIn,
                        onClick = viewModel::restoreFromCloud
                    ) {
                        Text(stringResource(R.string.restore_from_cloud))
                    }
                    Button(
                        enabled = !syncState.isBusy && syncState.isSignedIn,
                        onClick = viewModel::signOut,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.sign_out))
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(label: String, tag: String, currentTag: String, onSelected: (String) -> Unit) {
    FilterChip(
        selected = currentTag == tag || (tag == "" && currentTag.isBlank()),
        onClick = {
            onSelected(tag)
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        },
        label = { Text(label) },
        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
    )
}

