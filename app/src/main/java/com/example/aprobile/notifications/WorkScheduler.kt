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
}
