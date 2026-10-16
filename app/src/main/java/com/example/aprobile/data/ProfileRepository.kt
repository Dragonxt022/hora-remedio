package com.example.aprobile.data

import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val profileDao: ProfileDao,
    private val medicationDao: MedicationDao,
    private val healthDao: HealthDao
) {

    val allProfiles: Flow<List<ProfileEntity>> = profileDao.getAllProfiles()

    suspend fun seedIfEmpty(defaults: List<String>) {
        if (profileDao.getAllProfilesSync().isNotEmpty()) return
        val names = (listOf("Eu") + defaults).map { it.trim() }.filter { it.isNotBlank() }.distinct()
        names.forEachIndexed { index, name ->
            profileDao.insertProfile(ProfileEntity(name = name, sortOrder = index))
        }
    }

    suspend fun addProfile(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        if (profileDao.getAllProfilesSync().any { it.name.equals(trimmed, ignoreCase = true) }) return
        profileDao.insertProfile(ProfileEntity(name = trimmed, sortOrder = profileDao.getAllProfilesSync().size))
    }

    suspend fun renameProfile(oldName: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank() || trimmed == oldName) return

        medicationDao.renameProfile(oldName, trimmed)
        healthDao.renameProfile(oldName, trimmed)
        healthDao.getProfileSync(oldName)?.let { hp ->
            healthDao.deleteProfile(oldName)
            healthDao.upsertProfile(hp.copy(profile = trimmed))
        }
        profileDao.renameProfile(oldName, trimmed)
    }

    suspend fun deleteProfile(name: String) {
        if (isProfileInUse(name)) return
        profileDao.deleteProfile(name)
    }

    suspend fun isProfileInUse(name: String): Boolean {
        if (medicationDao.countMedicationsForProfile(name) > 0) return true
        if (healthDao.countEventsForProfile(name) > 0) return true
        return healthDao.getProfileSync(name) != null
    }
}
