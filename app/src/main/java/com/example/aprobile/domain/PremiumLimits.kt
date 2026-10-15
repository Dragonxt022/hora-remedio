package com.example.aprobile.domain

object PremiumLimits {

    const val FREE_MAX_MEDICATIONS = 3

    fun canAddMedication(isPremium: Boolean, currentCount: Int): Boolean {
        return isPremium || currentCount < FREE_MAX_MEDICATIONS
    }

    fun remainingFreeSlots(isPremium: Boolean, currentCount: Int): Int {
        if (isPremium) return Int.MAX_VALUE
        return (FREE_MAX_MEDICATIONS - currentCount).coerceAtLeast(0)
    }
}
