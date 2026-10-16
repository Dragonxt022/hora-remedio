package com.example.aprobile.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.aprobile.MainActivity
import com.example.aprobile.R

object Notifier {

    fun notifyRefill(context: Context, medicationId: Long, medicationName: String, quantity: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(context, manager, CHANNEL_REFILL, context.getString(R.string.refill_channel))

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_REFILL)
            .setSmallIcon(R.drawable.ic_stat_medication)
            .setColor(context.getColor(R.color.notification_accent))
            .setContentTitle(context.getString(R.string.refill_title))
            .setContentText(context.getString(R.string.refill_text, medicationName, quantity))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        manager.notify(("refill:$medicationId").hashCode(), builder.build())
    }

    private fun createChannel(context: Context, manager: NotificationManager, id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(id) == null) {
            manager.createNotificationChannel(
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    private const val CHANNEL_REFILL = "refill_channel"
}
