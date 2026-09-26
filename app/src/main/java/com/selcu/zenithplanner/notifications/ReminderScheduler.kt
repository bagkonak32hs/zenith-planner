package com.selcu.zenithplanner.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.selcu.zenithplanner.R
import java.util.Calendar

object ReminderScheduler {
    const val CHANNEL_ID = "daily_planner_reminders"
    private const val REQUEST_CODE = 20260423

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.daily_reminder),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.reminder_notification_body)
        }
        manager.createNotificationChannel(channel)
    }

    fun schedule(context: Context, hour: Int, minute: Int) {
        ensureChannel(context)
        ReminderPreferences.save(context, enabled = true, hour = hour, minute = minute)
        context.getSystemService(AlarmManager::class.java).setRepeating(
            AlarmManager.RTC_WAKEUP,
            nextTrigger(hour, minute),
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context)
        )
    }

    fun cancel(context: Context) {
        val current = ReminderPreferences.read(context)
        ReminderPreferences.save(context, enabled = false, hour = current.hour, minute = current.minute)
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context))
    }

    fun restoreIfEnabled(context: Context) {
        val settings = ReminderPreferences.read(context)
        if (settings.enabled) schedule(context, settings.hour, settings.minute)
    }

    private fun nextTrigger(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

