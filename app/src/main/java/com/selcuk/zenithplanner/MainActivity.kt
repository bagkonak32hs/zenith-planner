package com.selcuk.zenithplanner

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.selcuk.zenithplanner.notifications.ReminderScheduler
import com.selcuk.zenithplanner.ui.ZenithPlannerApp
import com.selcuk.zenithplanner.ui.theme.ZenithPlannerTheme

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


