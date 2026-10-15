package com.example.aprobile.domain

import java.time.LocalDate

object AdherenceStreak {

    /**
     * Dias consecutivos com adesão completa (todas as doses vencidas tomadas),
     * terminando hoje ou ontem.
     *
     * @param byDay data -> Pair(tomadas, total de doses vencidas no dia)
     */
    fun currentStreak(byDay: Map<LocalDate, Pair<Int, Int>>, today: LocalDate): Int {
        var streak = 0

        val todayStats = byDay[today]
        if (todayStats != null && todayStats.second > 0 && todayStats.first >= todayStats.second) {
            streak++
        }

        var cursor = today.minusDays(1)
        while (true) {
            val stats = byDay[cursor] ?: break
            if (stats.second <= 0 || stats.first < stats.second) break
            streak++
            cursor = cursor.minusDays(1)
        }

        return streak
    }
}
