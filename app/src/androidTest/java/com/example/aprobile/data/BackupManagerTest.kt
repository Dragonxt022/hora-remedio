package com.example.aprobile.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupManagerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun exportThenImport_roundTripsMedicationsAndDoses() {
        val medication = MedicationEntity(
            id = 1,
            name = "Dipirona",
            dosage = "500mg",
            instructions = "Após refeição",
            times = listOf("08:00", "20:00"),
            profile = "Eu",
            colorTag = "Azul",
            currentQuantity = 10,
            lowStockAlert = 3,
            soundEnabled = false,
            vibrateEnabled = true
        )
        val dose = DoseRecordEntity(
            id = 5,
            medicationId = 1,
            scheduledDateTime = 123456L,
            status = DoseRecordEntity.STATUS_TAKEN,
            takenAt = 999L
        )

        val json = BackupManager.export(context, BackupData(medications = listOf(medication), doses = listOf(dose)))
        val parsed = BackupManager.import(context, json)

        assertEquals(1, parsed.medications.size)
        assertEquals(1, parsed.doses.size)
        assertEquals("Dipirona", parsed.medications.first().name)
        assertEquals(listOf("08:00", "20:00"), parsed.medications.first().times)
        assertTrue(!parsed.medications.first().soundEnabled)
        assertEquals(DoseRecordEntity.STATUS_TAKEN, parsed.doses.first().status)
    }

    @Test
    fun exportThenImport_roundTripsHealthAndAppointments() {
        val appointment = AppointmentEntity(id = 1, title = "Dentista", dateTime = 5000L)
        val event = HealthEventEntity(
            id = 1,
            profile = "Eu",
            timestamp = 1000L,
            type = HealthEventEntity.TYPE_FEVER,
            title = "Febre",
            value = "38"
        )
        val profile = ProfileEntity(name = "Eu", sortOrder = 0)

        val json = BackupManager.export(
            context,
            BackupData(
                appointments = listOf(appointment),
                healthEvents = listOf(event),
                healthProfiles = listOf(HealthProfileEntity(profile = "Eu", bloodType = "O+")),
                profiles = listOf(profile)
            )
        )
        val parsed = BackupManager.import(context, json)

        assertEquals(1, parsed.appointments.size)
        assertEquals("Dentista", parsed.appointments.first().title)
        assertEquals(1, parsed.healthEvents.size)
        assertEquals("38", parsed.healthEvents.first().value)
        assertEquals("O+", parsed.healthProfiles.first().bloodType)
        assertEquals(1, parsed.profiles.size)
    }
}
