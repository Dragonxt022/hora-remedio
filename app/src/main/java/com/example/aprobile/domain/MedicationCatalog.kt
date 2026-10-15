package com.example.aprobile.domain

object MedicationCatalog {

    val names: List<String> = listOf(
        "Paracetamol", "Dipirona", "Ibuprofeno", "Amoxicilina", "Omeprazol",
        "Losartana", "Metformina", "Sinvastatina", "Atenolol", "Levotiroxina",
        "Loratadina", "Cetirizina", "Dexametasona", "Prednisona", "Salbutamol",
        "Insulina", "Vitamina D", "Vitamina C", "Ácido Fólico", "Sertralina",
        "Fluoxetina", "Clonazepam", "Ranitidina", "Nimesulida", "Diclofenaco"
    )

    fun suggest(query: String, limit: Int = 5): List<String> {
        val normalized = query.trim().lowercase()
        if (normalized.length < 2) return emptyList()
        return names
            .filter { it.lowercase().contains(normalized) }
            .take(limit)
    }
}
