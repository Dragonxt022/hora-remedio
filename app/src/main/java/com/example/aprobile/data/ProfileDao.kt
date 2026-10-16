package com.example.aprobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY sortOrder ASC, name ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles ORDER BY sortOrder ASC, name ASC")
    suspend fun getAllProfilesSync(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE name = :name")
    suspend fun deleteProfile(name: String)

    @Query("UPDATE profiles SET name = :newName WHERE name = :oldName")
    suspend fun renameProfile(oldName: String, newName: String)

    @Query("DELETE FROM profiles")
    suspend fun deleteAllProfiles()
}
