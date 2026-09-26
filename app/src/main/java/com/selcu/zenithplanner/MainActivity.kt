package com.selcu.zenithplanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.selcu.zenithplanner.notifications.ReminderScheduler
import com.selcu.zenithplanner.ui.ZenithPlannerApp
import com.selcu.zenithplanner.ui.theme.ZenithPlannerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderScheduler.ensureChannel(this)
        enableEdgeToEdge()
        setContent {
            ZenithPlannerTheme {
                ZenithPlannerApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (application as ZenithPlannerApplication).subscriptionManager.refreshPurchases()
    }
}

