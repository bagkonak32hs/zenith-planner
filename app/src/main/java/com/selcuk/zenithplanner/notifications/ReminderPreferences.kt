package com.selcuk.zenithplanner.notifications

import android.content.Context

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

object ReminderPreferences {
    private const val NAME = "planner_reminder"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_HOUR = "hour"
    private const val KEY_MINUTE = "minute"

    fun read(context: Context): ReminderSettings {
        val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return ReminderSettings(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            hour = prefs.getInt(KEY_HOUR, 8),
            minute = prefs.getInt(KEY_MINUTE, 0)
        )
    }

    fun save(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putInt(KEY_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }
}


