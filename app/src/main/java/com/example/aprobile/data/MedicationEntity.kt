package com.example.aprobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val instructions: String? = null,
    val times: List<String>,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long? = null,
    val enabled: Boolean = true,
    val currentQuantity: Int? = null,
    val lowStockAlert: Int? = null,
    val profile: String = "Eu", // e.g. "Eu", "Mãe", "Pai", "Pet"
    val colorTag: String = "Azul", // e.g. "Azul", "Verde", "Vermelho", "Roxo", "Laranja"
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val scheduleType: String = SCHEDULE_DAILY,
    val daysOfWeek: String = "", // "1,3,5" (seg=1 ... dom=7), usado quando scheduleType = WEEKLY_DAYS
    val barcode: String? = null // código de barras (EAN/UPC) lido pelo scanner
) {
    companion object {
        const val SCHEDULE_DAILY = "DAILY"
        const val SCHEDULE_WEEKLY_DAYS = "WEEKLY_DAYS"
        const val SCHEDULE_AS_NEED = "AS_NEED"
    }
}
