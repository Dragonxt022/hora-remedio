package com.example.aprobile.data

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class BackupData(
    val medications: List<MedicationEntity> = emptyList(),
    val doses: List<DoseRecordEntity> = emptyList(),
    val appointments: List<AppointmentEntity> = emptyList(),
    val healthProfiles: List<HealthProfileEntity> = emptyList(),
    val healthEvents: List<HealthEventEntity> = emptyList(),
    val profiles: List<ProfileEntity> = emptyList()
)

object BackupManager {

    const val CURRENT_VERSION = 2

    fun export(context: Context, data: BackupData): String {
        val root = JSONObject()
        root.put("app", "HoraDoRemedio")
        root.put("version", CURRENT_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        val medArray = JSONArray()
        data.medications.forEach { medArray.put(medicationToJson(it)) }
        root.put("medications", medArray)

        val doseArray = JSONArray()
        data.doses.forEach { doseArray.put(doseToJson(it)) }
        root.put("doses", doseArray)

        val apptArray = JSONArray()
        data.appointments.forEach { apptArray.put(appointmentToJson(it)) }
        root.put("appointments", apptArray)

        val hpArray = JSONArray()
        data.healthProfiles.forEach { hpArray.put(healthProfileToJson(it)) }
        root.put("healthProfiles", hpArray)

        val heArray = JSONArray()
        data.healthEvents.forEach { heArray.put(healthEventToJson(context, it)) }
        root.put("healthEvents", heArray)

        val profArray = JSONArray()
        data.profiles.forEach { profArray.put(profileToJson(context, it)) }
        root.put("profiles", profArray)

        return root.toString(2)
    }

    /** Lê o JSON, grava as fotos no armazenamento interno e devolve as entidades prontas para inserção. */
    fun import(context: Context, json: String): BackupData {
        val root = JSONObject(json)

        val medications = mutableListOf<MedicationEntity>()
        val doses = mutableListOf<DoseRecordEntity>()
        val appointments = mutableListOf<AppointmentEntity>()
        val healthProfiles = mutableListOf<HealthProfileEntity>()
        val healthEvents = mutableListOf<HealthEventEntity>()
        val profiles = mutableListOf<ProfileEntity>()

        root.optJSONArray("medications")?.let { array ->
            for (i in 0 until array.length()) medications.add(medicationFromJson(array.getJSONObject(i)))
        }
        root.optJSONArray("doses")?.let { array ->
            for (i in 0 until array.length()) doses.add(doseFromJson(array.getJSONObject(i)))
        }
        root.optJSONArray("appointments")?.let { array ->
            for (i in 0 until array.length()) appointments.add(appointmentFromJson(array.getJSONObject(i)))
        }
        root.optJSONArray("healthProfiles")?.let { array ->
            for (i in 0 until array.length()) healthProfiles.add(healthProfileFromJson(array.getJSONObject(i)))
        }
        root.optJSONArray("healthEvents")?.let { array ->
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val photoBase64 = if (o.isNull("photoBase64")) null else o.optString("photoBase64")
                val path = writePhoto(context, photoBase64, "evt")
                healthEvents.add(healthEventFromJson(o, path))
            }
        }
        root.optJSONArray("profiles")?.let { array ->
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                val photoBase64 = if (o.isNull("photoBase64")) null else o.optString("photoBase64")
                val path = writePhoto(context, photoBase64, "profile")
                profiles.add(profileFromJson(o, path))
            }
        }

        return BackupData(
            medications = medications,
            doses = doses,
            appointments = appointments,
            healthProfiles = healthProfiles,
            healthEvents = healthEvents,
            profiles = profiles
        )
    }

    private fun writePhoto(context: Context, base64: String?, prefix: String): String? {
        if (base64.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val dir = File(context.filesDir, "photos").apply { mkdirs() }
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}_${(0..9999).random()}.jpg")
            file.writeBytes(bytes)
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun readPhotoBase64(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return try {
            val file = File(path)
            if (!file.exists()) null
            else Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    // ---- Medications ----

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
        o.optJSONArray("times")?.let { array -> for (i in 0 until array.length()) times.add(array.getString(i)) }
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

    // ---- Doses ----

    private fun doseToJson(d: DoseRecordEntity) = JSONObject().apply {
        put("id", d.id)
        put("medicationId", d.medicationId)
        put("scheduledDateTime", d.scheduledDateTime)
        put("status", d.status)
        put("takenAt", d.takenAt ?: JSONObject.NULL)
    }

    private fun doseFromJson(o: JSONObject): DoseRecordEntity = DoseRecordEntity(
        id = o.optLong("id", 0),
        medicationId = o.optLong("medicationId", 0),
        scheduledDateTime = o.optLong("scheduledDateTime", 0),
        status = o.optString("status", DoseRecordEntity.STATUS_PENDING),
        takenAt = if (o.isNull("takenAt")) null else o.optLong("takenAt")
    )

    // ---- Appointments ----

    private fun appointmentToJson(a: AppointmentEntity) = JSONObject().apply {
        put("id", a.id)
        put("title", a.title)
        put("doctor", a.doctor ?: JSONObject.NULL)
        put("location", a.location ?: JSONObject.NULL)
        put("dateTime", a.dateTime)
        put("notes", a.notes ?: JSONObject.NULL)
    }

    private fun appointmentFromJson(o: JSONObject): AppointmentEntity = AppointmentEntity(
        id = o.optLong("id", 0),
        title = o.optString("title", ""),
        doctor = if (o.isNull("doctor")) null else o.optString("doctor"),
        location = if (o.isNull("location")) null else o.optString("location"),
        dateTime = o.optLong("dateTime", 0),
        notes = if (o.isNull("notes")) null else o.optString("notes")
    )

    // ---- Health profiles ----

    private fun healthProfileToJson(h: HealthProfileEntity) = JSONObject().apply {
        put("profile", h.profile)
        put("bloodType", h.bloodType ?: JSONObject.NULL)
        put("allergies", h.allergies ?: JSONObject.NULL)
        put("conditions", h.conditions ?: JSONObject.NULL)
        put("weight", h.weight ?: JSONObject.NULL)
        put("height", h.height ?: JSONObject.NULL)
        put("emergencyContact", h.emergencyContact ?: JSONObject.NULL)
        put("notes", h.notes ?: JSONObject.NULL)
    }

    private fun healthProfileFromJson(o: JSONObject): HealthProfileEntity = HealthProfileEntity(
        profile = o.optString("profile", "Eu"),
        bloodType = if (o.isNull("bloodType")) null else o.optString("bloodType"),
        allergies = if (o.isNull("allergies")) null else o.optString("allergies"),
        conditions = if (o.isNull("conditions")) null else o.optString("conditions"),
        weight = if (o.isNull("weight")) null else o.optString("weight"),
        height = if (o.isNull("height")) null else o.optString("height"),
        emergencyContact = if (o.isNull("emergencyContact")) null else o.optString("emergencyContact"),
        notes = if (o.isNull("notes")) null else o.optString("notes")
    )

    // ---- Health events (com foto) ----

    private fun healthEventToJson(context: Context, e: HealthEventEntity) = JSONObject().apply {
        put("id", e.id)
        put("profile", e.profile)
        put("timestamp", e.timestamp)
        put("type", e.type)
        put("title", e.title)
        put("value", e.value ?: JSONObject.NULL)
        put("notes", e.notes ?: JSONObject.NULL)
        put("photoBase64", readPhotoBase64(e.photoUri) ?: JSONObject.NULL)
    }

    private fun healthEventFromJson(o: JSONObject, photoPath: String?): HealthEventEntity = HealthEventEntity(
        id = o.optLong("id", 0),
        profile = o.optString("profile", "Eu"),
        timestamp = o.optLong("timestamp", 0),
        type = o.optString("type", HealthEventEntity.TYPE_NOTE),
        title = o.optString("title", ""),
        value = if (o.isNull("value")) null else o.optString("value"),
        notes = if (o.isNull("notes")) null else o.optString("notes"),
        photoUri = photoPath
    )

    // ---- Profiles (com foto) ----

    private fun profileToJson(context: Context, p: ProfileEntity) = JSONObject().apply {
        put("name", p.name)
        put("sortOrder", p.sortOrder)
        put("photoBase64", readPhotoBase64(p.photoUri) ?: JSONObject.NULL)
    }

    private fun profileFromJson(o: JSONObject, photoPath: String?): ProfileEntity = ProfileEntity(
        name = o.optString("name", "Eu"),
        sortOrder = o.optInt("sortOrder", 0),
        photoUri = photoPath
    )
}
