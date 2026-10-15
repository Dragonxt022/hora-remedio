package com.example.aprobile.data

import org.json.JSONArray
import org.json.JSONObject

data class BackupData(
    val medications: List<MedicationEntity>,
    val doses: List<DoseRecordEntity>
)

object BackupManager {

    const val CURRENT_VERSION = 1

    fun export(medications: List<MedicationEntity>, doses: List<DoseRecordEntity>): String {
        val root = JSONObject()
        root.put("app", "HoraDoRemedio")
        root.put("version", CURRENT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val medArray = JSONArray()
        medications.forEach { medArray.put(medicationToJson(it)) }
        root.put("medications", medArray)

        val doseArray = JSONArray()
        doses.forEach { doseArray.put(doseToJson(it)) }
        root.put("doses", doseArray)

        return root.toString(2)
    }

    fun parse(json: String): BackupData {
        val root = JSONObject(json)
        val medications = mutableListOf<MedicationEntity>()
        val doses = mutableListOf<DoseRecordEntity>()

        root.optJSONArray("medications")?.let { array ->
            for (i in 0 until array.length()) {
                medications.add(medicationFromJson(array.getJSONObject(i)))
            }
        }
        root.optJSONArray("doses")?.let { array ->
            for (i in 0 until array.length()) {
                doses.add(doseFromJson(array.getJSONObject(i)))
            }
        }
        return BackupData(medications, doses)
    }

    private fun medicationToJson(m: MedicationEntity) = JSONObject().apply {
        put("id", m.id)
        put("name", m.name)
        put("dosage", m.dosage)
        put("instructions", m.instructions ?: JSONObject.NULL)
        put("times", JSONArray(m.times))
        put("startDate", m.startDate)
        put("endDate", m.endDate ?: JSONObject.NULL)
        put("enabled", m.enabled)
        put("currentQuantity", m.currentQuantity ?: JSONObject.NULL)
        put("lowStockAlert", m.lowStockAlert ?: JSONObject.NULL)
        put("profile", m.profile)
        put("colorTag", m.colorTag)
        put("soundEnabled", m.soundEnabled)
        put("vibrateEnabled", m.vibrateEnabled)
    }

    private fun medicationFromJson(o: JSONObject): MedicationEntity {
        val times = mutableListOf<String>()
        o.optJSONArray("times")?.let { array ->
            for (i in 0 until array.length()) times.add(array.getString(i))
        }
        return MedicationEntity(
            id = o.optLong("id", 0),
            name = o.optString("name", ""),
            dosage = o.optString("dosage", ""),
            instructions = if (o.isNull("instructions")) null else o.optString("instructions"),
            times = times,
            startDate = o.optLong("startDate", System.currentTimeMillis()),
            endDate = if (o.isNull("endDate")) null else o.optLong("endDate"),
            enabled = o.optBoolean("enabled", true),
            currentQuantity = if (o.isNull("currentQuantity")) null else o.optInt("currentQuantity"),
            lowStockAlert = if (o.isNull("lowStockAlert")) null else o.optInt("lowStockAlert"),
            profile = o.optString("profile", "Eu"),
            colorTag = o.optString("colorTag", "Azul"),
            soundEnabled = o.optBoolean("soundEnabled", true),
            vibrateEnabled = o.optBoolean("vibrateEnabled", true)
        )
    }

    private fun doseToJson(d: DoseRecordEntity) = JSONObject().apply {
        put("id", d.id)
        put("medicationId", d.medicationId)
        put("scheduledDateTime", d.scheduledDateTime)
        put("status", d.status)
        put("takenAt", d.takenAt ?: JSONObject.NULL)
    }

    private fun doseFromJson(o: JSONObject): DoseRecordEntity {
        return DoseRecordEntity(
            id = o.optLong("id", 0),
            medicationId = o.optLong("medicationId", 0),
            scheduledDateTime = o.optLong("scheduledDateTime", 0),
            status = o.optString("status", DoseRecordEntity.STATUS_PENDING),
            takenAt = if (o.isNull("takenAt")) null else o.optLong("takenAt")
        )
    }
}
