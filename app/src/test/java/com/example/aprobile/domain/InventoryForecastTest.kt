package com.example.aprobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class InventoryForecastTest {

    private val today = LocalDate.of(2026, 1, 10)

    @Test
    fun forecast_nullQuantity_returnsNull() {
        assertNull(InventoryForecast.forecast(currentQuantity = null, dosesPerDay = 2, lowStockAlert = 5, today = today))
    }

    @Test
    fun forecast_computesDaysRemainingAndRunsOutDate() {
        val result = InventoryForecast.forecast(currentQuantity = 10, dosesPerDay = 2, lowStockAlert = 5, today = today)!!
        assertEquals(5, result.daysRemaining)
        assertEquals(today.plusDays(5), result.runsOutDate)
        assertFalse(result.isLow)
    }

    @Test
    fun forecast_lowWhenBelowThreshold() {
        val result = InventoryForecast.forecast(currentQuantity = 4, dosesPerDay = 2, lowStockAlert = 5, today = today)!!
        assertTrue(result.isLow)
    }

    @Test
    fun forecast_lowWhenFewDaysRemaining() {
        val result = InventoryForecast.forecast(currentQuantity = 6, dosesPerDay = 2, lowStockAlert = 1, today = today)!!
        assertEquals(3, result.daysRemaining)
        assertTrue(result.isLow)
    }

    @Test
    fun forecast_zeroDosesPerDay_doesNotDivideByZero() {
        val result = InventoryForecast.forecast(currentQuantity = 10, dosesPerDay = 0, lowStockAlert = 5, today = today)!!
        assertEquals(Int.MAX_VALUE, result.daysRemaining)
        assertNull(result.runsOutDate)
    }
}
