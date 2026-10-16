package com.example.aprobile.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications")
    suspend fun getAllMedicationsSync(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<MedicationEntity>)

    @Query("DELETE FROM medications")
    suspend fun deleteAllMedications()

    @Query("UPDATE medications SET profile = :newName WHERE profile = :oldName")
    suspend fun renameProfile(oldName: String, newName: String)

    @Query("SELECT COUNT(*) FROM medications WHERE profile = :name")
    suspend fun countMedicationsForProfile(name: String): Int

    @Query("SELECT * FROM medications WHERE enabled = 1")
    suspend fun getActiveMedicationsSync(): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity): Int

    @Delete
    suspend fun deleteMedication(medication: MedicationEntity): Int

    // Dose Records
    @Query("SELECT * FROM dose_records WHERE scheduledDateTime >= :startOfDay AND scheduledDateTime <= :endOfDay ORDER BY scheduledDateTime ASC")
    fun getDoseRecordsForDay(startOfDay: Long, endOfDay: Long): Flow<List<DoseRecordEntity>>

    @Query("SELECT * FROM dose_records")
    fun getAllDoseRecords(): Flow<List<DoseRecordEntity>>

    @Query("SELECT * FROM dose_records")
    suspend fun getAllDoseRecordsSync(): List<DoseRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseRecords(doseRecords: List<DoseRecordEntity>)

    @Query("DELETE FROM dose_records")
    suspend fun deleteAllDoseRecords()

    @Query("SELECT * FROM dose_records WHERE medicationId = :medicationId AND scheduledDateTime = :scheduledDateTime LIMIT 1")
    suspend fun getDoseRecord(medicationId: Long, scheduledDateTime: Long): DoseRecordEntity?

    @Query("SELECT * FROM dose_records WHERE id = :doseId LIMIT 1")
    suspend fun getDoseRecordById(doseId: Long): DoseRecordEntity?

    @Query("SELECT * FROM dose_records WHERE medicationId = :medicationId AND status = 'PENDING' ORDER BY scheduledDateTime ASC LIMIT 1")
    suspend fun getFirstPendingDoseForMedication(medicationId: Long): DoseRecordEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDoseRecord(doseRecord: DoseRecordEntity): Long

    @Update
    suspend fun updateDoseRecord(doseRecord: DoseRecordEntity): Int

    @Query("UPDATE dose_records SET status = :status, takenAt = :takenAt WHERE id = :doseId")
    suspend fun updateDoseStatus(doseId: Long, status: String, takenAt: Long?): Int

    @Query("DELETE FROM dose_records WHERE medicationId = :medicationId")
    suspend fun deleteDoseRecordsForMedication(medicationId: Long): Int
}
