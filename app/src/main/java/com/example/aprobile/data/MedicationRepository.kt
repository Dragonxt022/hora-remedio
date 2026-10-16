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
                if (medication.scheduleType == MedicationEntity.SCHEDULE_AS_NEED) continue

                val start = Instant.ofEpochMilli(medication.startDate).atZone(zone).toLocalDate()
                if (date.isBefore(start)) continue
                val end = medication.endDate?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
                if (end != null && date.isAfter(end)) continue

                if (medication.scheduleType == MedicationEntity.SCHEDULE_WEEKLY_DAYS) {
                    val dayOfWeek = date.dayOfWeek.value // 1 = segunda ... 7 = domingo
                    val allowed = medication.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                    if (dayOfWeek !in allowed) continue
                }

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
                val newQty = updatedMed.currentQuantity ?: 0
                if (newQty <= (updatedMed.lowStockAlert ?: 5)) {
                    com.example.aprobile.notifications.Notifier.notifyRefill(context, updatedMed.id, updatedMed.name, newQty)
                }
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
            medication.vibrateEnabled,
            medication.profile
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

    suspend fun existingProfileNames(): List<String> =
        medicationDao.getAllMedicationsSync().map { it.profile.ifBlank { "Eu" } }.distinct()

    /**
     * Gera ~1 ano de histórico de doses (e alguns episódios de febre) para simulação/testes.
     * Não faz nada se já houver muitos registros.
     */
    suspend fun seedOneYearData(
        healthDao: HealthDao,
        feverProfile: String?,
        random: kotlin.random.Random = kotlin.random.Random(42)
    ) {
        if (medicationDao.getAllDoseRecordsSync().size > 400) return
        val medications = medicationDao.getAllMedicationsSync()
        if (medications.isEmpty()) return

        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val today = LocalDate.now()
        val records = mutableListOf<DoseRecordEntity>()

        for (med in medications) {
            var date = today.minusDays(364)
            while (!date.isAfter(today)) {
                for (timeStr in med.times) {
                    try {
                        val time = LocalTime.parse(timeStr)
                        val millis = LocalDateTime.of(date, time)
                            .atZone(zone).toInstant().toEpochMilli()
                        if (millis > now) continue
                        val taken = random.nextInt(100) < 82
                        records.add(
                            DoseRecordEntity(
                                medicationId = med.id,
                                scheduledDateTime = millis,
                                status = if (taken) DoseRecordEntity.STATUS_TAKEN else DoseRecordEntity.STATUS_PENDING,
                                takenAt = if (taken) millis + 60_000 else null
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                date = date.plusDays(1)
            }
        }
        if (records.isNotEmpty()) medicationDao.insertDoseRecords(records)

        if (feverProfile != null) {
            val events = mutableListOf<HealthEventEntity>()
            for (i in 0 until 12) {
                val day = today.minusDays(random.nextInt(360).toLong())
                val hour = 8 + random.nextInt(12)
                val millis = LocalDateTime.of(day, LocalTime.of(hour, random.nextInt(60)))
                    .atZone(zone).toInstant().toEpochMilli()
                val temp = 37.5f + random.nextInt(20) / 10f
                events.add(
                    HealthEventEntity(
                        profile = feverProfile,
                        timestamp = millis,
                        type = HealthEventEntity.TYPE_FEVER,
                        title = "Febre",
                        value = String.format(java.util.Locale.US, "%.1f", temp)
                    )
                )
            }
            healthDao.insertEvents(events)
        }
    }

    /** Registra um uso de medicamento "quando necessário" (SOS): cria uma dose tomada agora e baixa o estoque. */
    suspend fun logAsNeededDose(medication: MedicationEntity) {
        val now = System.currentTimeMillis()
        medicationDao.insertDoseRecord(
            DoseRecordEntity(
                medicationId = medication.id,
                scheduledDateTime = now,
                status = DoseRecordEntity.STATUS_TAKEN,
                takenAt = now
            )
        )
        if (medication.currentQuantity != null && medication.currentQuantity > 0) {
            val updated = medication.copy(currentQuantity = medication.currentQuantity - 1)
            medicationDao.updateMedication(updated)
            val qty = updated.currentQuantity ?: 0
            if (qty <= (updated.lowStockAlert ?: 5)) {
                com.example.aprobile.notifications.Notifier.notifyRefill(context, updated.id, updated.name, qty)
            }
        }
    }
}
