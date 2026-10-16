package com.example.aprobile.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
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

    fun notifyOutOfStock(context: Context, medicationId: Long, medicationName: String) {
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
            .setContentTitle(context.getString(R.string.refill_out_title))
            .setContentText(context.getString(R.string.refill_out_text, medicationName))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        manager.notify(("outofstock:$medicationId").hashCode(), builder.build())
    }

    fun notifyCheckIn(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(context, manager, CHANNEL_CHECKIN, context.getString(R.string.checkin_channel))

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val replyIntent = Intent(context, CheckInReplyReceiver::class.java)
        val replyPending = PendingIntent.getBroadcast(
            context,
            1,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val remoteInput = RemoteInput.Builder(KEY_CHECKIN_REPLY)
            .setLabel(context.getString(R.string.checkin_reply_label))
            .build()
        val replyAction = NotificationCompat.Action.Builder(
            R.drawable.ic_stat_medication,
            context.getString(R.string.checkin_reply_action),
            replyPending
        ).addRemoteInput(remoteInput).build()

        val builder = NotificationCompat.Builder(context, CHANNEL_CHECKIN)
            .setSmallIcon(R.drawable.ic_stat_medication)
            .setColor(context.getColor(R.color.notification_accent))
            .setContentTitle(context.getString(R.string.checkin_title))
            .setContentText(context.getString(R.string.checkin_text))
            .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(R.string.checkin_text)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPending)
            .addAction(replyAction)

        manager.notify(CHECKIN_NOTIF_ID, builder.build())
    }

    fun notifyCheckInSaved(context: Context, text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannel(context, manager, CHANNEL_CHECKIN, context.getString(R.string.checkin_channel))
        manager.cancel(CHECKIN_NOTIF_ID)

        val builder = NotificationCompat.Builder(context, CHANNEL_CHECKIN)
            .setSmallIcon(R.drawable.ic_stat_medication)
            .setColor(context.getColor(R.color.notification_accent))
            .setContentTitle(context.getString(R.string.checkin_saved_title))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        manager.notify(CHECKIN_SAVED_NOTIF_ID, builder.build())
    }

    private fun createChannel(context: Context, manager: NotificationManager, id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(id) == null) {
            manager.createNotificationChannel(
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    private const val CHANNEL_REFILL = "refill_channel"
    private const val CHANNEL_CHECKIN = "checkin_channel"
    private const val CHECKIN_NOTIF_ID = 4001
    private const val CHECKIN_SAVED_NOTIF_ID = 4002

    const val KEY_CHECKIN_REPLY = "checkin_reply"
}
