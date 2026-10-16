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
}
