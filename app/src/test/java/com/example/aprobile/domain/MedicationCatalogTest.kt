package com.example.aprobile.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationCatalogTest {

    @Test
    fun suggest_returnsMatchesForPartialQuery() {
        val result = MedicationCatalog.suggest("par")
        assertTrue(result.any { it.equals("Paracetamol", ignoreCase = true) })
    }

    @Test
    fun suggest_emptyForShortQuery() {
        assertTrue(MedicationCatalog.suggest("a").isEmpty())
    }

    @Test
    fun suggest_respectsLimit() {
        assertTrue(MedicationCatalog.suggest("i", limit = 5).size <= 5)
        assertTrue(MedicationCatalog.suggest("o", limit = 2).size <= 2)
    }
}
