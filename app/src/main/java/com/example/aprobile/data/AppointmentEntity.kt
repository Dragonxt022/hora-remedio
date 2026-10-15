package com.example.aprobile.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    indices = [Index(value = ["dateTime"])]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val doctor: String? = null,
    val location: String? = null,
    val dateTime: Long,
    val notes: String? = null
)
