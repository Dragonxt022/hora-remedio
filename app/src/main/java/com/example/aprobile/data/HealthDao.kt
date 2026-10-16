package com.example.aprobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    @Query("SELECT * FROM health_profiles WHERE profile = :profile LIMIT 1")
    fun getProfile(profile: String): Flow<HealthProfileEntity?>

    @Query("SELECT * FROM health_profiles WHERE profile = :profile LIMIT 1")
    suspend fun getProfileSync(profile: String): HealthProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: HealthProfileEntity)

    @Query("SELECT * FROM health_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<HealthEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: HealthEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<HealthEventEntity>)

    @Delete
    suspend fun deleteEvent(event: HealthEventEntity): Int

    @Query("UPDATE health_events SET profile = :newName WHERE profile = :oldName")
    suspend fun renameProfile(oldName: String, newName: String)

    @Query("DELETE FROM health_profiles WHERE profile = :name")
    suspend fun deleteProfile(name: String)

    @Query("SELECT COUNT(*) FROM health_events WHERE profile = :name")
    suspend fun countEventsForProfile(name: String): Int

    @Query("DELETE FROM health_events")
    suspend fun deleteAllEvents()

    @Query("DELETE FROM health_profiles")
    suspend fun deleteAllProfiles()
}
