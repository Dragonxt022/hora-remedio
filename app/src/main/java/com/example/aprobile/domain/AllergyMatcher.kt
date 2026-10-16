package com.example.aprobile.domain

object AllergyMatcher {

    /**
     * Retorna o termo de alergia que bate com o nome do medicamento, ou null.
     * Compara de forma tolerante (substring, sem diferenciar maiúsculas).
     */
    fun firstMatch(medicationName: String, allergies: String?): String? {
        val name = medicationName.trim().lowercase()
        if (name.length < 3 || allergies.isNullOrBlank()) return null

        val terms = allergies
            .split(',', ';', '\n')
            .map { it.trim() }
            .filter { it.length >= 3 }

        return terms.firstOrNull { term ->
            val t = term.lowercase()
            name.contains(t) || t.contains(name)
        }
    }
}
