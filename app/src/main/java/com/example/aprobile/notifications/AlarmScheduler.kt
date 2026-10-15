package com.example.aprobile.notifications

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.aprobile.data.MedicationEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {

    fun scheduleAlarmsForMedication(context: Context, medication: MedicationEntity) {
        if (!medication.enabled || medication.times.isEmpty()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = LocalDateTime.now()

        val scheduled = mutableListOf<Pair<Long, PendingIntent>>()

        for (timeStr in medication.times) {
            try {
                val time = LocalTime.parse(timeStr)
                var scheduledDateTime = LocalDateTime.of(LocalDate.now(), time)
                if (scheduledDateTime.isBefore(now)) {
                    scheduledDateTime = scheduledDateTime.plusDays(1)
                }
                val triggerAtMillis = scheduledDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val pendingIntent = buildPendingIntent(context, medication, timeStr, triggerAtMillis)
                scheduled.add(triggerAtMillis to pendingIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val nextTrigger = scheduled.minOfOrNull { it.first }
        for ((triggerAtMillis, pendingIntent) in scheduled) {
            setAlarm(alarmManager, triggerAtMillis, pendingIntent, isNext = triggerAtMillis == nextTrigger)
        }
    }

    fun snoozeAlarm(
        context: Context,
        medicationId: Long,
        medicationName: String,
        medicationDosage: String,
        timeStr: String,
        minutes: Int = 15,
        soundEnabled: Boolean = true,
        vibrateEnabled: Boolean = true
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAtMillis = System.currentTimeMillis() + (minutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_DOSAGE, medicationDosage)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_TIME, timeStr)
            putExtra(AlarmReceiver.EXTRA_SCHEDULED_MILLIS, triggerAtMillis)
            putExtra(AlarmReceiver.EXTRA_SOUND_ENABLED, soundEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATE_ENABLED, vibrateEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(medicationId, SNOOZE_KEY),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(alarmManager, triggerAtMillis, pendingIntent, isNext = true)
    }

    fun cancelAlarmsForMedication(context: Context, medicationId: Long, times: List<String>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val keys = (times + SNOOZE_KEY).distinct()
        for (key in keys) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode(medicationId, key),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    private fun buildPendingIntent(
        context: Context,
        medication: MedicationEntity,
        timeStr: String,
        triggerAtMillis: Long
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEDICATION_ID, medication.id)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_NAME, medication.name)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_DOSAGE, medication.dosage)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_TIME, timeStr)
            putExtra(AlarmReceiver.EXTRA_SCHEDULED_MILLIS, triggerAtMillis)
            putExtra(AlarmReceiver.EXTRA_SOUND_ENABLED, medication.soundEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATE_ENABLED, medication.vibrateEnabled)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(medication.id, timeStr),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
        isNext: Boolean
    ) {
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        try {
            when {
                isNext && canExact -> {
                    alarmManager.setAlarmClock(AlarmClockInfo(triggerAtMillis, pendingIntent), pendingIntent)
                }
                canExact -> {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
                else -> {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            }
        } catch (se: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun requestCode(medicationId: Long, key: String): Int = "$medicationId:$key".hashCode()

    private const val SNOOZE_KEY = "snooze"
}
