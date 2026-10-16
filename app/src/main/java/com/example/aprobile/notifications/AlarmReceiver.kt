package com.example.aprobile.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.aprobile.MainActivity
import com.example.aprobile.R
import com.example.aprobile.data.AppDatabase
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medicamento"
        val medicationDosage = intent.getStringExtra(EXTRA_MEDICATION_DOSAGE) ?: ""
        val medicationProfile = intent.getStringExtra(EXTRA_MEDICATION_PROFILE) ?: ""
        val medicationTime = intent.getStringExtra(EXTRA_MEDICATION_TIME) ?: ""
        val scheduledMillis = intent.getLongExtra(EXTRA_SCHEDULED_MILLIS, resolveScheduledMillis(medicationTime))
        val soundEnabled = intent.getBooleanExtra(EXTRA_SOUND_ENABLED, true)
        val vibrateEnabled = intent.getBooleanExtra(EXTRA_VIBRATE_ENABLED, true)

        val notificationId = notificationId(medicationId, medicationTime)

        when (intent.action) {
            ACTION_TAKEN -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = AppDatabase.getDatabase(context).medicationDao()
                    val doseRecord = dao.getDoseRecord(medicationId, scheduledMillis)
                    if (doseRecord != null) {
                        dao.updateDoseStatus(doseRecord.id, DoseRecordEntity.STATUS_TAKEN, System.currentTimeMillis())
                        val med = dao.getMedicationById(medicationId)
                        if (med != null && med.currentQuantity != null && med.currentQuantity > 0) {
                            dao.updateMedication(med.copy(currentQuantity = med.currentQuantity - 1))
                        }
                    }
                }
                cancelNotification(context, notificationId)
            }

            ACTION_SNOOZE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val minutes = runBlocking { SettingsRepository(context).getSnoozeMinutes() }
                    AlarmScheduler.snoozeAlarm(
                        context,
                        medicationId,
                        medicationName,
                        medicationDosage,
                        medicationTime,
                        minutes,
                        soundEnabled,
                        vibrateEnabled,
                        medicationProfile
                    )
                }
                cancelNotification(context, notificationId)
            }

            else -> {
                createNotificationChannels(context)
                showNotification(
                    context = context,
                    medicationId = medicationId,
                    name = medicationName,
                    dosage = medicationDosage,
                    profile = medicationProfile,
                    time = medicationTime,
                    scheduledMillis = scheduledMillis,
                    notificationId = notificationId,
                    soundEnabled = soundEnabled,
                    vibrateEnabled = vibrateEnabled
                )
            }
        }
    }

    private fun resolveScheduledMillis(time: String): Long {
        return try {
            val parsed = LocalTime.parse(time)
            LocalDateTime.of(LocalDate.now(), parsed)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.notification_channel_desc)
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }

            if (manager.getNotificationChannel(CHANNEL_ID_SILENT) == null) {
                val silent = NotificationChannel(
                    CHANNEL_ID_SILENT,
                    context.getString(R.string.notification_channel_silent_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.notification_channel_silent_desc)
                    setSound(null, null)
                    enableVibration(false)
                }
                manager.createNotificationChannel(silent)
            }
        }
    }

    private fun showNotification(
        context: Context,
        medicationId: Long,
        name: String,
        dosage: String,
        profile: String,
        time: String,
        scheduledMillis: Long,
        notificationId: Int,
        soundEnabled: Boolean,
        vibrateEnabled: Boolean
    ) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_SCHEDULED_MILLIS, scheduledMillis)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val takenIntent = actionIntent(context, ACTION_TAKEN, medicationId, name, dosage, profile, time, scheduledMillis, soundEnabled, vibrateEnabled, notificationId)
        val snoozeIntent = actionIntent(context, ACTION_SNOOZE, medicationId, name, dosage, profile, time, scheduledMillis, soundEnabled, vibrateEnabled, notificationId)

        val bodyText = if (dosage.isNotBlank()) {
            "Hora de tomar $name ($dosage) - $time"
        } else {
            "Hora de tomar $name - $time"
        }

        val largeIcon = largeIconBitmap(context)
        val channelId = if (soundEnabled && vibrateEnabled) CHANNEL_ID else CHANNEL_ID_SILENT

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_medication)
            .setLargeIcon(largeIcon)
            .setColor(context.getColor(R.color.notification_accent))
            .setContentTitle(context.getString(R.string.notification_title, name))
            .setContentText(bodyText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, context.getString(R.string.notification_action_taken), takenIntent)
            .addAction(0, context.getString(R.string.notification_action_snooze), snoozeIntent)

        if (profile.isNotBlank()) builder.setSubText(profile)
        builder.setSilent(!soundEnabled)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())
    }

    private fun actionIntent(
        context: Context,
        action: String,
        medicationId: Long,
        name: String,
        dosage: String,
        profile: String,
        time: String,
        scheduledMillis: Long,
        soundEnabled: Boolean,
        vibrateEnabled: Boolean,
        notificationId: Int
    ): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_MEDICATION_NAME, name)
            putExtra(EXTRA_MEDICATION_DOSAGE, dosage)
            putExtra(EXTRA_MEDICATION_PROFILE, profile)
            putExtra(EXTRA_MEDICATION_TIME, time)
            putExtra(EXTRA_SCHEDULED_MILLIS, scheduledMillis)
            putExtra(EXTRA_SOUND_ENABLED, soundEnabled)
            putExtra(EXTRA_VIBRATE_ENABLED, vibrateEnabled)
        }
        return PendingIntent.getBroadcast(
            context,
            notificationId + if (action == ACTION_TAKEN) 1 else 2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun largeIconBitmap(context: Context, sizeDp: Int = 48): Bitmap {
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_notification_large)
        if (drawable != null) {
            drawable.setBounds(0, 0, sizePx, sizePx)
            drawable.draw(canvas)
        }
        return bitmap
    }

    private fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }

    private fun notificationId(medicationId: Long, time: String): Int = "$medicationId:$time".hashCode()

    companion object {
        const val CHANNEL_ID = "medication_reminders_channel"
        const val CHANNEL_ID_SILENT = "medication_reminders_channel_silent"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "extra_medication_dosage"
        const val EXTRA_MEDICATION_PROFILE = "extra_medication_profile"
        const val EXTRA_MEDICATION_TIME = "extra_medication_time"
        const val EXTRA_SCHEDULED_MILLIS = "extra_scheduled_millis"
        const val EXTRA_SOUND_ENABLED = "extra_sound_enabled"
        const val EXTRA_VIBRATE_ENABLED = "extra_vibrate_enabled"
        const val ACTION_TAKEN = "com.example.aprobile.ACTION_TAKEN"
        const val ACTION_SNOOZE = "com.example.aprobile.ACTION_SNOOZE"
    }
}
