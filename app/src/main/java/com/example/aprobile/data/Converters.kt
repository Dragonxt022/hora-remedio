package com.example.aprobile.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimesList(times: List<String>?): String {
        return times?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toTimesList(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
