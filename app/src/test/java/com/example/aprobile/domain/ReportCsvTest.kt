package com.example.aprobile.domain

import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.MedicationEntity
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ReportCsvTest {

    @Test
    fun buildCsv_containsHeaderAndRow() {
        val zone = ZoneId.systemDefault()
        val millis = LocalDateTime.of(2026, 1, 10, 8, 0).atZone(zone).toInstant().toEpochMilli()

        val medication = MedicationEntity(
            id = 1,
            name = "Dipirona",
            dosage = "500mg",
            times = listOf("08:00"),
            profile = "Eu"
        )
        val dose = DoseRecordEntity(
            id = 1,
            medicationId = 1,
            scheduledDateTime = millis,
            status = DoseRecordEntity.STATUS_TAKEN
        )

        val csv = ReportCsv.buildCsv(listOf(medication), listOf(dose), zone)
        val lines = csv.trim().split("\n")

        assertTrue(lines.first().startsWith("Data;Hora;Medicamento;Perfil;Dosagem;Status"))
        assertTrue(csv.contains("10/01/2026;08:00;Dipirona;Eu;500mg;Tomada"))
    }

    @Test
    fun buildCsv_escapesSemicolons() {
        val zone = ZoneId.systemDefault()
        val millis = LocalDateTime.of(2026, 1, 10, 8, 0).atZone(zone).toInstant().toEpochMilli()
        val medication = MedicationEntity(
            id = 1,
            name = "Vitamina;D",
            dosage = "1 gota",
            times = listOf("08:00")
        )
        val dose = DoseRecordEntity(id = 1, medicationId = 1, scheduledDateTime = millis)

        val csv = ReportCsv.buildCsv(listOf(medication), listOf(dose), zone)
        assertTrue(csv.contains("Vitamina,D"))
    }
}
