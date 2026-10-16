package com.example.aprobile.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aprobile.data.AppDatabase
import com.example.aprobile.data.MedicationRepository

class DoseMaintenanceWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val repository = MedicationRepository(database.medicationDao(), applicationContext)
            repository.ensureDoseRecords()
            repository.scheduleAllActiveAlarms()
            database.appointmentDao().getAllAppointmentsSync().forEach {
                AppointmentAlarmScheduler.schedule(applicationContext, it)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
