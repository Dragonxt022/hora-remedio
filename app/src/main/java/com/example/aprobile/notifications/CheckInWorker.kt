package com.example.aprobile.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aprobile.data.SettingsRepository
import kotlinx.coroutines.flow.first

class CheckInWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val enabled = SettingsRepository(applicationContext).dailyCheckInEnabled.first()
            if (enabled) {
                Notifier.notifyCheckIn(applicationContext)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
