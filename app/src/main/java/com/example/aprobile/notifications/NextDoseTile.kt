package com.example.aprobile.notifications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.aprobile.MainActivity
import com.example.aprobile.R
import com.example.aprobile.data.AppDatabase
import com.example.aprobile.data.DoseRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NextDoseTile : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.label = getString(R.string.app_name)
        tile.state = Tile.STATE_INACTIVE
        tile.icon = Icon.createWithResource(this, R.drawable.ic_stat_medication)
        tile.subtitle = getString(R.string.tile_loading)
        tile.updateTile()

        CoroutineScope(Dispatchers.IO).launch {
            val text = loadNextDoseText()
            tile.subtitle = text
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private suspend fun loadNextDoseText(): String {
        return try {
            val dao = AppDatabase.getDatabase(applicationContext).medicationDao()
            val medMap = dao.getAllMedicationsSync().associateBy { it.id }
            val now = System.currentTimeMillis()
            val next = dao.getAllDoseRecordsSync()
                .filter { it.status == DoseRecordEntity.STATUS_PENDING && it.scheduledDateTime >= now }
                .minByOrNull { it.scheduledDateTime }
            if (next == null) {
                getString(R.string.tile_no_doses)
            } else {
                val name = medMap[next.medicationId]?.name ?: "—"
                val time = LocalDateTime.ofInstant(Instant.ofEpochMilli(next.scheduledDateTime), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
                getString(R.string.tile_next, name, time)
            }
        } catch (e: Exception) {
            getString(R.string.tile_no_doses)
        }
    }
}
