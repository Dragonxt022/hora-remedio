package com.example.aprobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoseAdherenceTest {

    @Test
    fun computeRate_returns100_whenNoElapsedDoses() {
        assertEquals(100, DoseAdherence.computeRate(taken = 0, elapsed = 0))
    }

    @Test
    fun computeRate_computesPercentage() {
        assertEquals(75, DoseAdherence.computeRate(taken = 3, elapsed = 4))
        assertEquals(50, DoseAdherence.computeRate(taken = 1, elapsed = 2))
        assertEquals(0, DoseAdherence.computeRate(taken = 0, elapsed = 4))
    }

    @Test
    fun computeRate_fullAdherence() {
        assertEquals(100, DoseAdherence.computeRate(taken = 5, elapsed = 5))
    }

    @Test
    fun isElapsed_trueForPastOrNow() {
        val now = 1_000_000L
        assertTrue(DoseAdherence.isElapsed(scheduledDateTime = now - 1, now = now))
        assertTrue(DoseAdherence.isElapsed(scheduledDateTime = now, now = now))
    }

    @Test
    fun isElapsed_falseForFuture() {
        val now = 1_000_000L
        assertFalse(DoseAdherence.isElapsed(scheduledDateTime = now + 1, now = now))
    }
}
