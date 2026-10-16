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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AppointmentAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(EXTRA_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Consulta"
        val dateTime = intent.getLongExtra(EXTRA_DATETIME, 0L)

        createChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val whenText = try {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm"))
        } catch (e: Exception) {
            ""
        }

        val bigIcon = largeIcon(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_medication)
            .apply { if (bigIcon != null) setLargeIcon(bigIcon) }
            .setColor(context.getColor(R.color.notification_accent))
            .setContentTitle(context.getString(R.string.appt_reminder_title))
            .setContentText(context.getString(R.string.appt_reminder_text, title, whenText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId(id), builder.build())
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.appt_reminder_channel),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
        }
    }

    private fun largeIcon(context: Context, sizeDp: Int = 48): Bitmap? {
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_notification_large) ?: return null
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(canvas)
        return bitmap
    }

    private fun notificationId(id: Long): Int = ("apptnotif:$id").hashCode()

    companion object {
        const val CHANNEL_ID = "appointment_reminders_channel"
        const val EXTRA_ID = "extra_appt_id"
        const val EXTRA_TITLE = "extra_appt_title"
        const val EXTRA_DATETIME = "extra_appt_datetime"
    }
}
