package com.example.aprobile.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.aprobile.data.AppointmentEntity

object AppointmentAlarmScheduler {

    private const val ONE_HOUR_MILLIS = 60L * 60L * 1000L

    fun schedule(context: Context, appointment: AppointmentEntity) {
        val triggerAt = appointment.dateTime - ONE_HOUR_MILLIS
        if (triggerAt <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildPendingIntent(context, appointment)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (se: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(context: Context, appointmentId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AppointmentAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(appointmentId),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun buildPendingIntent(context: Context, appointment: AppointmentEntity): PendingIntent {
        val intent = Intent(context, AppointmentAlarmReceiver::class.java).apply {
            putExtra(AppointmentAlarmReceiver.EXTRA_ID, appointment.id)
            putExtra(AppointmentAlarmReceiver.EXTRA_TITLE, appointment.title)
            putExtra(AppointmentAlarmReceiver.EXTRA_DATETIME, appointment.dateTime)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(appointment.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCode(appointmentId: Long): Int = "appt:$appointmentId".hashCode()
}
