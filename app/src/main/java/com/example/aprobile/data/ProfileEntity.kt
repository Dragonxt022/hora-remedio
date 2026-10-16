package com.example.aprobile.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val name: String,
    val sortOrder: Int = 0
)
