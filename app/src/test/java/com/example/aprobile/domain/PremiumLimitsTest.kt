package com.example.aprobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumLimitsTest {

    @Test
    fun freeUser_canAddBelowLimit() {
        assertTrue(PremiumLimits.canAddMedication(isPremium = false, currentCount = 0))
        assertTrue(PremiumLimits.canAddMedication(isPremium = false, currentCount = 2))
    }

    @Test
    fun freeUser_cannotAddAtLimit() {
        assertFalse(PremiumLimits.canAddMedication(isPremium = false, currentCount = PremiumLimits.FREE_MAX_MEDICATIONS))
        assertFalse(PremiumLimits.canAddMedication(isPremium = false, currentCount = 10))
    }

    @Test
    fun premiumUser_canAlwaysAdd() {
        assertTrue(PremiumLimits.canAddMedication(isPremium = true, currentCount = 100))
    }

    @Test
    fun remainingFreeSlots_isComputedAndClamped() {
        assertEquals(3, PremiumLimits.remainingFreeSlots(isPremium = false, currentCount = 0))
        assertEquals(1, PremiumLimits.remainingFreeSlots(isPremium = false, currentCount = 2))
        assertEquals(0, PremiumLimits.remainingFreeSlots(isPremium = false, currentCount = 5))
        assertEquals(Int.MAX_VALUE, PremiumLimits.remainingFreeSlots(isPremium = true, currentCount = 5))
    }
}
