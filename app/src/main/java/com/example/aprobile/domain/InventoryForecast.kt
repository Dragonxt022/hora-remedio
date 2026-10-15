package com.example.aprobile.domain

import java.time.LocalDate

object InventoryForecast {

    data class Forecast(
        val dosesPerDay: Int,
        val daysRemaining: Int,
        val runsOutDate: LocalDate?,
        val isLow: Boolean
    )

    /**
     * Previsão de término do estoque.
     * [currentQuantity] nulo = estoque não controlado (retorna null).
     * [dosesPerDay] = número de horários por dia.
     * [lowStockAlert] = limite configurado (ou 5 por padrão).
     */
    fun forecast(
        currentQuantity: Int?,
        dosesPerDay: Int,
        lowStockAlert: Int?,
        today: LocalDate
    ): Forecast? {
        if (currentQuantity == null) return null

        val threshold = lowStockAlert ?: DEFAULT_LOW_STOCK

        if (dosesPerDay <= 0) {
            return Forecast(
                dosesPerDay = 0,
                daysRemaining = Int.MAX_VALUE,
                runsOutDate = null,
                isLow = currentQuantity <= threshold
            )
        }

        val daysRemaining = currentQuantity / dosesPerDay
        val runsOutDate = today.plusDays(daysRemaining.toLong())
        val isLow = currentQuantity <= threshold || daysRemaining <= LOW_DAYS_THRESHOLD

        return Forecast(
            dosesPerDay = dosesPerDay,
            daysRemaining = daysRemaining,
            runsOutDate = runsOutDate,
            isLow = isLow
        )
    }

    const val DEFAULT_LOW_STOCK = 5
    const val LOW_DAYS_THRESHOLD = 3
}
