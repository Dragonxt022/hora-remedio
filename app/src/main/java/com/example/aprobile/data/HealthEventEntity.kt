package com.example.aprobile.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_events",
    indices = [Index(value = ["profile"]), Index(value = ["timestamp"])]
)
data class HealthEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profile: String,
    val timestamp: Long,
    val type: String,
    val title: String,
    val value: String? = null,
    val notes: String? = null
) {
    companion object {
        const val TYPE_SYMPTOM = "SYMPTOM"
        const val TYPE_FEVER = "FEVER"
        const val TYPE_PRESSURE = "PRESSURE"
        const val TYPE_NOTE = "NOTE"
    }
}
