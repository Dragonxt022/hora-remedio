package com.example.aprobile.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AdherenceStreakTest {

    private val today = LocalDate.of(2026, 1, 10)

    @Test
    fun currentStreak_emptyMap_isZero() {
        assertEquals(0, AdherenceStreak.currentStreak(emptyMap(), today))
    }

    @Test
    fun currentStreak_countsConsecutiveCompleteDays() {
        val byDay = mapOf(
            today to (2 to 2),
            today.minusDays(1) to (1 to 1),
            today.minusDays(2) to (3 to 3)
        )
        assertEquals(3, AdherenceStreak.currentStreak(byDay, today))
    }

    @Test
    fun currentStreak_incompleteToday_keepsPreviousStreak() {
        val byDay = mapOf(
            today to (1 to 2),
            today.minusDays(1) to (1 to 1),
            today.minusDays(2) to (2 to 2)
        )
        assertEquals(2, AdherenceStreak.currentStreak(byDay, today))
    }

    @Test
    fun currentStreak_gapBreaksStreak() {
        val byDay = mapOf(
            today to (2 to 2),
            today.minusDays(1) to (0 to 1),
            today.minusDays(2) to (2 to 2)
        )
        assertEquals(1, AdherenceStreak.currentStreak(byDay, today))
    }

    @Test
    fun currentStreak_noToday_noYesterday_isZero() {
        val byDay = mapOf(
            today.minusDays(2) to (2 to 2)
        )
        assertEquals(0, AdherenceStreak.currentStreak(byDay, today))
    }
}
