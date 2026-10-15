package com.example.aprobile.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {

    @Test
    fun exportThenParse_roundTripsMedicationsAndDoses() {
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

        val json = BackupManager.export(listOf(medication), listOf(dose))
        val parsed = BackupManager.parse(json)

        assertEquals(1, parsed.medications.size)
        assertEquals(1, parsed.doses.size)

        val m = parsed.medications.first()
        assertEquals("Dipirona", m.name)
        assertEquals("500mg", m.dosage)
        assertEquals(listOf("08:00", "20:00"), m.times)
        assertEquals(10, m.currentQuantity)
        assertFalse(m.soundEnabled)
        assertTrue(m.vibrateEnabled)

        val d = parsed.doses.first()
        assertEquals(5L, d.id)
        assertEquals(1L, d.medicationId)
        assertEquals(DoseRecordEntity.STATUS_TAKEN, d.status)
        assertEquals(999L, d.takenAt)
    }

    @Test
    fun exportThenParse_handlesNulls() {
        val medication = MedicationEntity(
            id = 2,
            name = "Vitamina",
            dosage = "",
            times = listOf("09:00")
        )
        val json = BackupManager.export(listOf(medication), emptyList())
        val parsed = BackupManager.parse(json)

        val m = parsed.medications.first()
        assertNull(m.instructions)
        assertNull(m.currentQuantity)
        assertTrue(parsed.doses.isEmpty())
    }
}
