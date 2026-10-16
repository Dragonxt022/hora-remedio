package com.example.aprobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_profiles")
data class HealthProfileEntity(
    @PrimaryKey
    val profile: String,
    val bloodType: String? = null,
    val allergies: String? = null,
    val conditions: String? = null,
    val weight: String? = null,
    val height: String? = null,
    val emergencyContact: String? = null,
    val notes: String? = null
)
