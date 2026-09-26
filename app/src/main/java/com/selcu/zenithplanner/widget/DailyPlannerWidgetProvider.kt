package com.selcu.zenithplanner.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class DailyPlannerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        PlannerWidgetUpdater.update(context, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == PlannerWidgetUpdater.ACTION_REFRESH) {
            PlannerWidgetUpdater.updateAll(context)
        }
    }
}

