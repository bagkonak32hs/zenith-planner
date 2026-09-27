package com.selcuk.zenithplanner.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.selcuk.zenithplanner.MainActivity
import com.selcuk.zenithplanner.R
import com.selcuk.zenithplanner.ZenithPlannerApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object PlannerWidgetUpdater {
    const val ACTION_REFRESH = "com.selcuk.zenithplanner.widget.REFRESH"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, DailyPlannerWidgetProvider::class.java))
        update(context, ids)
    }

    fun update(context: Context, ids: IntArray) {
        if (ids.isEmpty()) return
        val appContext = context.applicationContext
        scope.launch {
            val database = (appContext as ZenithPlannerApplication).database
            val today = LocalDate.now()
            val tasks = database.taskDao().getTasksForDate(today.toString()).take(3)
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.daily_planner_widget)
                views.setTextViewText(R.id.widget_title, context.getString(R.string.daily_widget_title))
                views.setTextViewText(
                    R.id.widget_date,
                    today.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
                )
                views.setTextViewText(R.id.widget_task_1, tasks.getOrNull(0)?.title?.ifBlank { "1. ${context.getString(R.string.title)}" } ?: "1. ${context.getString(R.string.title)}")
                views.setTextViewText(R.id.widget_task_2, tasks.getOrNull(1)?.title?.ifBlank { "2. ${context.getString(R.string.title)}" } ?: "2. ${context.getString(R.string.title)}")
                views.setTextViewText(R.id.widget_task_3, tasks.getOrNull(2)?.title?.ifBlank { "3. ${context.getString(R.string.title)}" } ?: "3. ${context.getString(R.string.title)}")
                views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context))
                AppWidgetManager.getInstance(context).updateAppWidget(id, views)
            }
        }
    }

    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}


