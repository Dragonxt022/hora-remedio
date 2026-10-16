package com.example.aprobile.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AllergyMatcherTest {

    @Test
    fun matchesWhenMedicationContainsAllergy() {
        assertEquals("Dipirona", AllergyMatcher.firstMatch("Dipirona 500mg", "Dipirona, Penicilina"))
    }

    @Test
    fun matchesIgnoringCaseAndAccents() {
        assertEquals("amoxicilina", AllergyMatcher.firstMatch("AMOXICILINA", "amoxicilina"))
    }

    @Test
    fun returnsNullWhenNoMatch() {
        assertNull(AllergyMatcher.firstMatch("Paracetamol", "Dipirona, Penicilina"))
    }

    @Test
    fun returnsNullForShortOrBlank() {
        assertNull(AllergyMatcher.firstMatch("Ab", "Dipirona"))
        assertNull(AllergyMatcher.firstMatch("Dipirona", ""))
        assertNull(AllergyMatcher.firstMatch("Dipirona", null))
    }
}
