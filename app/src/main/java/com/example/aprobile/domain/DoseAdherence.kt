package com.example.aprobile.domain

object DoseAdherence {

    /** A dose já "venceu" (horário no passado), portanto conta para a adesão. */
    fun isElapsed(scheduledDateTime: Long, now: Long): Boolean = scheduledDateTime <= now

    /**
     * Taxa de adesão (0-100) com base apenas nas doses já vencidas.
     * Sem doses vencidas, considera 100%.
     */
    fun computeRate(taken: Int, elapsed: Int): Int {
        if (elapsed <= 0) return 100
        return (taken.toFloat() / elapsed.toFloat() * 100).toInt()
    }
}
