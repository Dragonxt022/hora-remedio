package com.example.aprobile.domain

import com.example.aprobile.data.HealthEventEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthMetricsTest {

    private fun event(type: String, value: String?, ts: Long) =
        HealthEventEntity(id = ts, profile = "Eu", timestamp = ts, type = type, title = "t", value = value)

    @Test
    fun temperaturePoints_onlyFeverWithNumericValue() {
        val events = listOf(
            event(HealthEventEntity.TYPE_FEVER, "38", 100),
            event(HealthEventEntity.TYPE_FEVER, "39,5", 200),
            event(HealthEventEntity.TYPE_SYMPTOM, "40", 150),
            event(HealthEventEntity.TYPE_FEVER, "muito", 300)
        )
        val points = HealthMetrics.temperaturePoints(events)
        assertEquals(2, points.size)
        assertEquals(100L to 38f, points[0])
        assertEquals(200L to 39.5f, points[1])
    }

    @Test
    fun temperaturePoints_emptyWhenNoFever() {
        val events = listOf(event(HealthEventEntity.TYPE_NOTE, "x", 1))
        assertEquals(0, HealthMetrics.temperaturePoints(events).size)
    }
}
