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
    val vibrateEnabled: Boolean = true
)
