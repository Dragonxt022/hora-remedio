package com.example.aprobile

import android.app.Application
import com.example.aprobile.notifications.WorkScheduler

class AprobileApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WorkScheduler.schedulePeriodic(this)
        WorkScheduler.runNow(this)
        WorkScheduler.scheduleCheckIn(this)
    }
}
