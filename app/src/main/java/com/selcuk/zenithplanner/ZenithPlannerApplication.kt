package com.selcuk.zenithplanner

import android.app.Application
import androidx.room.Room
import com.selcuk.zenithplanner.data.PlannerDatabase
import com.selcuk.zenithplanner.data.PlannerRepository
import com.selcuk.zenithplanner.subscription.SubscriptionManager
import com.selcuk.zenithplanner.sync.FirebaseSyncManager

class ZenithPlannerApplication : Application() {
    val database: PlannerDatabase by lazy {
        Room.databaseBuilder(this, PlannerDatabase::class.java, "zenith_planner.db")
            .fallbackToDestructiveMigration(true)
            .build()
    }

    val repository: PlannerRepository by lazy {
        PlannerRepository(
            appContext = this,
            dailyPlanDao = database.dailyPlanDao(),
            taskDao = database.taskDao(),
            scheduleDao = database.scheduleDao(),
            habitDao = database.habitDao(),
            goalDao = database.goalDao(),
            quoteDao = database.quoteDao(),
            noteDao = database.noteDao(),
            financeDao = database.financeDao()
        )
    }

    val firebaseSyncManager: FirebaseSyncManager by lazy {
        FirebaseSyncManager(this, database)
    }

    val subscriptionManager: SubscriptionManager by lazy {
        SubscriptionManager(this)
    }
}


