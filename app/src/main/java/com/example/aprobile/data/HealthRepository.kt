package com.example.aprobile.data

import kotlinx.coroutines.flow.Flow

class HealthRepository(private val dao: HealthDao) {

    fun getProfile(profile: String): Flow<HealthProfileEntity?> = dao.getProfile(profile)

    val allEvents: Flow<List<HealthEventEntity>> = dao.getAllEvents()

    suspend fun saveProfile(profile: HealthProfileEntity) = dao.upsertProfile(profile)

    suspend fun insertEvent(event: HealthEventEntity): Long = dao.insertEvent(event)

    suspend fun deleteEvent(event: HealthEventEntity): Int = dao.deleteEvent(event)
}
