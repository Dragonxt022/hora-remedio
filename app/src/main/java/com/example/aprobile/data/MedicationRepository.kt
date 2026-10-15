package com.example.aprobile.data

import android.content.Context
import com.example.aprobile.notifications.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MedicationRepository(
    private val medicationDao: MedicationDao,
    private val context: Context
) {
    val allMedications: Flow<List<MedicationEntity>> = medicationDao.getAllMedications()
    val allDoseRecords: Flow<List<DoseRecordEntity>> = medicationDao.getAllDoseRecords()

    suspend fun scheduleAllActiveAlarms() {
        val activeMedications = medicationDao.getActiveMedicationsSync()
        for (med in activeMedications) {
            AlarmScheduler.scheduleAlarmsForMedication(context, med)
        }
    }

    suspend fun insertMedication(medication: MedicationEntity): Long {
        val id = medicationDao.insertMedication(medication)
        val inserted = medication.copy(id = id)
        if (inserted.enabled) {
            AlarmScheduler.scheduleAlarmsForMedication(context, inserted)
        }
        ensureDoseRecords()
        return id
    }

    suspend fun updateMedication(medication: MedicationEntity, previousTimes: List<String> = emptyList()) {
        medicationDao.updateMedication(medication)
        AlarmScheduler.cancelAlarmsForMedication(context, medication.id, previousTimes + medication.times)
        if (medication.enabled) {
            AlarmScheduler.scheduleAlarmsForMedication(context, medication)
        }
        ensureDoseRecords()
    }

    suspend fun deleteMedication(medication: MedicationEntity) {
        AlarmScheduler.cancelAlarmsForMedication(context, medication.id, medication.times)
        medicationDao.deleteMedication(medication)
    }

    suspend fun toggleMedicationEnabled(medication: MedicationEntity) {
        val updated = medication.copy(enabled = !medication.enabled)
        medicationDao.updateMedication(updated)
        if (updated.enabled) {
            AlarmScheduler.scheduleAlarmsForMedication(context, updated)
        } else {
            AlarmScheduler.cancelAlarmsForMedication(context, updated.id, updated.times)
        }
    }

    fun getDoseRecordsForDay(date: LocalDate): Flow<List<DoseRecordEntity>> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return medicationDao.getDoseRecordsForDay(startOfDay, endOfDay)
    }

    suspend fun generateDoseRecordsForToday(date: LocalDate = LocalDate.now()) {
        ensureDoseRecords(date, date)
    }

    suspend fun ensureDoseRecords(
        from: LocalDate = LocalDate.now().minusDays(7),
        to: LocalDate = LocalDate.now().plusDays(3)
    ) {
        val activeMedications = medicationDao.getActiveMedicationsSync()
        val zone = ZoneId.systemDefault()
        var date = from
        while (!date.isAfter(to)) {
            for (medication in activeMedications) {
                val start = Instant.ofEpochMilli(medication.startDate).atZone(zone).toLocalDate()
                if (date.isBefore(start)) continue
                val end = medication.endDate?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                if (end != null && date.isAfter(end)) continue

                for (timeStr in medication.times) {
                    try {
                        val time = LocalTime.parse(timeStr)
                        val scheduledDateTime = LocalDateTime.of(date, time)
                            .atZone(zone)
                            .toInstant()
                            .toEpochMilli()

                        val existing = medicationDao.getDoseRecord(medication.id, scheduledDateTime)
                        if (existing == null) {
                            medicationDao.insertDoseRecord(
                                DoseRecordEntity(
                                    medicationId = medication.id,
                                    scheduledDateTime = scheduledDateTime,
                                    status = DoseRecordEntity.STATUS_PENDING
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            date = date.plusDays(1)
        }
    }

    suspend fun markDoseAsTaken(doseId: Long) {
        val doseRecord = medicationDao.getDoseRecordById(doseId)
        if (doseRecord != null && doseRecord.status != DoseRecordEntity.STATUS_TAKEN) {
            medicationDao.updateDoseStatus(doseId, DoseRecordEntity.STATUS_TAKEN, System.currentTimeMillis())
            val medication = medicationDao.getMedicationById(doseRecord.medicationId)
            if (medication != null && medication.currentQuantity != null && medication.currentQuantity > 0) {
                val updatedMed = medication.copy(currentQuantity = medication.currentQuantity - 1)
                medicationDao.updateMedication(updatedMed)
            }
        }
    }

    suspend fun getNextPendingDose(medicationId: Long): DoseRecordEntity? {
        return medicationDao.getFirstPendingDoseForMedication(medicationId)
    }

    suspend fun markNextPendingDoseAsTaken(medicationId: Long) {
        val dose = medicationDao.getFirstPendingDoseForMedication(medicationId) ?: return
        markDoseAsTaken(dose.id)
    }

    fun snoozeDose(medication: MedicationEntity, dose: DoseRecordEntity, minutes: Int = 15) {
        val timeStr = try {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(dose.scheduledDateTime), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            medication.times.firstOrNull() ?: "08:00"
        }
        AlarmScheduler.snoozeAlarm(
            context,
            medication.id,
            medication.name,
            medication.dosage,
            timeStr,
            minutes,
            medication.soundEnabled,
            medication.vibrateEnabled
        )
    }

    suspend fun markDoseAsPending(doseId: Long) {
        val doseRecord = medicationDao.getDoseRecordById(doseId)
        if (doseRecord != null && doseRecord.status == DoseRecordEntity.STATUS_TAKEN) {
            medicationDao.updateDoseStatus(doseId, DoseRecordEntity.STATUS_PENDING, null)
            val medication = medicationDao.getMedicationById(doseRecord.medicationId)
            if (medication != null && medication.currentQuantity != null) {
                val updatedMed = medication.copy(currentQuantity = medication.currentQuantity + 1)
                medicationDao.updateMedication(updatedMed)
            }
        }
    }

    suspend fun exportBackup(): String {
        val medications = medicationDao.getAllMedicationsSync()
        val doses = medicationDao.getAllDoseRecordsSync()
        return BackupManager.export(medications, doses)
    }

    suspend fun importBackup(json: String) {
        val data = BackupManager.parse(json)
        val existing = medicationDao.getAllMedicationsSync()
        existing.forEach { AlarmScheduler.cancelAlarmsForMedication(context, it.id, it.times) }

        medicationDao.deleteAllDoseRecords()
        medicationDao.deleteAllMedications()
        medicationDao.insertMedications(data.medications)
        medicationDao.insertDoseRecords(data.doses)

        data.medications.filter { it.enabled }.forEach {
            AlarmScheduler.scheduleAlarmsForMedication(context, it)
        }
    }

    suspend fun deleteAllData() {
        val existing = medicationDao.getAllMedicationsSync()
        existing.forEach { AlarmScheduler.cancelAlarmsForMedication(context, it.id, it.times) }
        medicationDao.deleteAllDoseRecords()
        medicationDao.deleteAllMedications()
    }
}
