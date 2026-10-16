package com.example.aprobile.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.aprobile.data.AppDatabase
import com.example.aprobile.data.HealthEventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckInReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val results = RemoteInput.getResultsFromIntent(intent) ?: return
        val text = results.getCharSequence(Notifier.KEY_CHECKIN_REPLY)?.toString()?.trim().orEmpty()
        if (text.isEmpty()) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(appContext)
                val profile = database.profileDao().getAllProfilesSync().firstOrNull()?.name ?: "Eu"
                database.healthDao().insertEvent(
                    HealthEventEntity(
                        profile = profile,
                        timestamp = System.currentTimeMillis(),
                        type = HealthEventEntity.TYPE_SYMPTOM,
                        title = text
                    )
                )
                Notifier.notifyCheckInSaved(appContext, text)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
