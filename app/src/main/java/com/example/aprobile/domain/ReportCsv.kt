package com.example.aprobile.domain

import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.HealthEventEntity
import com.example.aprobile.data.MedicationEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReportCsv {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun buildCsv(
        medications: List<MedicationEntity>,
        doses: List<DoseRecordEntity>,
        zone: ZoneId = ZoneId.systemDefault()
    ): String {
        val medMap = medications.associateBy { it.id }
        val sb = StringBuilder()
        sb.append("Data;Hora;Medicamento;Perfil;Dosagem;Status\n")

        doses.sortedBy { it.scheduledDateTime }.forEach { dose ->
            val med = medMap[dose.medicationId] ?: return@forEach
            val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(dose.scheduledDateTime), zone)
            sb.append(dateTime.format(dateFormatter)).append(';')
                .append(dateTime.format(timeFormatter)).append(';')
                .append(escape(med.name)).append(';')
                .append(escape(med.profile)).append(';')
                .append(escape(med.dosage)).append(';')
                .append(if (dose.status == DoseRecordEntity.STATUS_TAKEN) "Tomada" else "Não tomada")
                .append('\n')
        }
        return sb.toString()
    }

    fun buildHealthCsv(
        events: List<HealthEventEntity>,
        doses: List<DoseRecordEntity>,
        medications: List<MedicationEntity>,
        zone: ZoneId = ZoneId.systemDefault()
    ): String {
        val medMap = medications.associateBy { it.id }
        val rows = mutableListOf<Triple<Long, String, String>>()

        events.forEach { event ->
            val instant = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.timestamp), zone)
            val whenText = instant.format(dateFormatter) + ";" + instant.format(timeFormatter)
            val detail = listOfNotNull(event.title, event.value, event.notes).joinToString(" ")
            rows.add(Triple(event.timestamp, whenText, "${event.profile};$detail"))
        }
        doses.filter { it.status == DoseRecordEntity.STATUS_TAKEN }.forEach { dose ->
            val med = medMap[dose.medicationId] ?: return@forEach
            val instant = LocalDateTime.ofInstant(Instant.ofEpochMilli(dose.scheduledDateTime), zone)
            val whenText = instant.format(dateFormatter) + ";" + instant.format(timeFormatter)
            rows.add(Triple(dose.scheduledDateTime, whenText, "${med.profile};Remédio tomado: ${escape(med.name)}"))
        }

        val sb = StringBuilder()
        sb.append("Data;Hora;Perfil;Detalhe\n")
        rows.sortedBy { it.first }.forEach { (_, whenText, detail) ->
            sb.append(whenText).append(';').append(detail).append('\n')
        }
        return sb.toString()
    }

    private fun escape(value: String): String = value.replace(';', ',').replace('\n', ' ')
}
