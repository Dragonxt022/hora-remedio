package com.example.aprobile.domain

import com.example.aprobile.data.HealthEventEntity

object HealthMetrics {

    /**
     * Extrai pontos (timestamp, temperatura) dos eventos de febre com valor numérico.
     */
    fun temperaturePoints(events: List<HealthEventEntity>): List<Pair<Long, Float>> =
        events
            .filter { it.type == HealthEventEntity.TYPE_FEVER }
            .mapNotNull { event ->
                val value = event.value?.trim()?.replace(",", ".")?.toFloatOrNull()
                if (value == null) null else event.timestamp to value
            }
            .sortedBy { it.first }

    fun glucosePoints(events: List<HealthEventEntity>): List<Pair<Long, Float>> =
        events
            .filter { it.type == HealthEventEntity.TYPE_GLUCOSE }
            .mapNotNull { event ->
                val value = event.value?.trim()?.replace(",", ".")?.toFloatOrNull()
                if (value == null) null else event.timestamp to value
            }
            .sortedBy { it.first }

    /** Pontos de pressão: (timestamp, sistólica, diastólica). */
    fun pressurePoints(events: List<HealthEventEntity>): List<Triple<Long, Int, Int>> =
        events
            .filter { it.type == HealthEventEntity.TYPE_PRESSURE }
            .mapNotNull { event ->
                val parts = event.value?.trim()?.split("/")
                if (parts == null || parts.size != 2) {
                    null
                } else {
                    val systolic = parts[0].trim().toIntOrNull()
                    val diastolic = parts[1].trim().toIntOrNull()
                    if (systolic == null || diastolic == null) null
                    else Triple(event.timestamp, systolic, diastolic)
                }
            }
            .sortedBy { it.first }
}
