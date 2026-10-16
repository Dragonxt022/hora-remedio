package com.example.aprobile.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val UNIQUE_PERIODIC = "dose_maintenance_periodic"
    private const val UNIQUE_ONESHOT = "dose_maintenance_oneshot"
    private const val UNIQUE_CHECKIN = "daily_checkin_periodic"
    private const val CHECKIN_HOUR = 20

    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<DoseMaintenanceWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<DoseMaintenanceWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_ONESHOT,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleCheckIn(context: Context) {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, CHECKIN_HOUR)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (!after(now)) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        val delay = target.timeInMillis - now.timeInMillis
        val request = PeriodicWorkRequestBuilder<CheckInWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_CHECKIN,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
