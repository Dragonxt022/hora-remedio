package com.example.aprobile.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aprobile.data.AppDatabase
import com.example.aprobile.data.AppointmentEntity
import com.example.aprobile.data.AppointmentRepository
import com.example.aprobile.data.BillingRepository
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.HealthEventEntity
import com.example.aprobile.data.HealthProfileEntity
import com.example.aprobile.data.HealthRepository
import com.example.aprobile.data.DoseWithMedication
import com.example.aprobile.data.MedicationEntity
import com.example.aprobile.data.MedicationRepository
import com.example.aprobile.data.ProfileRepository
import com.example.aprobile.data.SettingsRepository
import com.example.aprobile.domain.AdherenceStreak
import com.example.aprobile.domain.DoseAdherence
import com.example.aprobile.domain.PremiumLimits
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase
    private val repository: MedicationRepository
    private val appointmentRepository: AppointmentRepository
    private val healthRepository: HealthRepository
    private val profileRepository: ProfileRepository
    private val settingsRepository: SettingsRepository
    private val billingRepository: BillingRepository

    private val _uiState = MutableStateFlow(MedicationUiState())
    val uiState: StateFlow<MedicationUiState> = _uiState.asStateFlow()

    private val _filters = MutableStateFlow(FilterParams())
    private val _healthProfileName = MutableStateFlow("Eu")

    init {
        database = AppDatabase.getDatabase(application)
        repository = MedicationRepository(database.medicationDao(), application)
        appointmentRepository = AppointmentRepository(database.appointmentDao())
        healthRepository = HealthRepository(database.healthDao())
        profileRepository = ProfileRepository(database.profileDao(), database.medicationDao(), database.healthDao())
        settingsRepository = SettingsRepository(application)
        billingRepository = BillingRepository(application)

        viewModelScope.launch {
            repository.ensureDoseRecords()
            repository.scheduleAllActiveAlarms()
        }

        viewModelScope.launch {
            profileRepository.seedIfEmpty(repository.existingProfileNames())
        }

        observeSettings()
        observeData()
        observeHealth()
        observeProfiles()
    }

    private fun observeProfiles() {
        viewModelScope.launch {
            profileRepository.allProfiles.collect { profiles ->
                val names = profiles.map { it.name }
                val currentHealth = _healthProfileName.value
                val newHealth = if (names.isNotEmpty() && currentHealth !in names) names.first() else currentHealth
                if (newHealth != currentHealth) _healthProfileName.value = newHealth

                val currentSelected = _filters.value.selectedProfile
                val newSelected = if (currentSelected != "Todos" && currentSelected !in names) "Todos" else currentSelected
                if (newSelected != currentSelected) _filters.update { it.copy(selectedProfile = newSelected) }

                _uiState.update {
                    it.copy(
                        managedProfiles = names,
                        mainProfileName = names.firstOrNull() ?: "Eu",
                        mainProfilePhoto = profiles.firstOrNull()?.photoUri,
                        availableProfiles = listOf("Todos") + names,
                        healthProfileName = newHealth,
                        selectedProfile = newSelected,
                        activeHealthProfileName = if (newHealth.isNotBlank()) newHealth else it.activeHealthProfileName
                    )
                }
            }
        }
    }

    fun openProfileManager() {
        _uiState.update { it.copy(isProfileManagerOpen = true) }
    }

    fun closeProfileManager() {
        _uiState.update { it.copy(isProfileManagerOpen = false) }
    }

    fun addProfile(name: String) {
        viewModelScope.launch { profileRepository.addProfile(name) }
    }

    fun renameProfile(oldName: String, newName: String) {
        viewModelScope.launch { profileRepository.renameProfile(oldName, newName) }
    }

    fun deleteProfile(name: String) {
        viewModelScope.launch { profileRepository.deleteProfile(name) }
    }

    fun renameMainProfile(newName: String) {
        val old = _uiState.value.mainProfileName
        val trimmed = newName.trim()
        if (trimmed.isBlank() || trimmed == old) return
        viewModelScope.launch { profileRepository.renameProfile(old, trimmed) }
    }

    fun setMainProfilePhoto(path: String?) {
        val name = _uiState.value.mainProfileName
        viewModelScope.launch { profileRepository.setProfilePhoto(name, path) }
    }

    private fun resolveHealthProfile(profile: String): String {
        if (profile != "Todos") return profile
        return _uiState.value.availableProfiles.firstOrNull { it != "Todos" } ?: "Eu"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeHealth() {
        viewModelScope.launch {
            _healthProfileName
                .flatMapLatest { name -> healthRepository.getProfile(name) }
                .collect { profile ->
                    _uiState.update { it.copy(healthProfile = profile) }
                }
        }

        // Prontuário do perfil ativo global — usado pela dica na aba Doses e pelo alerta de alergia
        viewModelScope.launch {
            _filters
                .map { resolveHealthProfile(it.selectedProfile) }
                .distinctUntilChanged()
                .flatMapLatest { name -> healthRepository.getProfile(name) }
                .collect { profile ->
                    _uiState.update {
                        it.copy(
                            activeHealthProfile = profile,
                            activeHealthProfileName = resolveHealthProfile(_filters.value.selectedProfile)
                        )
                    }
                }
        }

        viewModelScope.launch {
            combine(
                healthRepository.allEvents,
                repository.allDoseRecords,
                repository.allMedications,
                _healthProfileName
            ) { events, doses, meds, profileName ->
                val medMap = meds.associateBy { it.id }
                val medIds = meds.filter { it.profile.equals(profileName, true) }.map { it.id }.toSet()
                val doseItems = doses
                    .filter { it.medicationId in medIds && it.status == DoseRecordEntity.STATUS_TAKEN }
                    .mapNotNull { dose -> medMap[dose.medicationId]?.let { HealthTimelineItem.Dose(it, dose) } }
                val eventItems = events
                    .filter { it.profile.equals(profileName, true) }
                    .map { HealthTimelineItem.Event(it) }
                (doseItems + eventItems).sortedByDescending { it.timestamp }
            }.collect { timeline ->
                _uiState.update {
                    it.copy(
                        healthTimeline = timeline,
                        healthEvents = timeline.filterIsInstance<HealthTimelineItem.Event>().map { item -> item.event }
                    )
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.darkMode.collect { dark ->
                _uiState.update { it.copy(isDarkMode = dark) }
            }
        }
        viewModelScope.launch {
            settingsRepository.snoozeMinutes.collect { minutes ->
                _uiState.update { it.copy(snoozeMinutes = minutes) }
            }
        }
        viewModelScope.launch {
            settingsRepository.onboardingDone.collect { done ->
                _uiState.update { it.copy(onboardingDone = done) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isPremium.collect { premium ->
                _uiState.update { it.copy(isPremium = premium) }
            }
        }
        viewModelScope.launch {
            settingsRepository.appLockEnabled.collect { enabled ->
                _uiState.update {
                    it.copy(
                        appLockEnabled = enabled,
                        isAppUnlocked = if (!enabled) true else it.isAppUnlocked
                    )
                }
            }
        }
    }

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAppLockEnabled(enabled) }
        _uiState.update { it.copy(isAppUnlocked = true) }
    }

    fun unlockApp() {
        _uiState.update { it.copy(isAppUnlocked = true) }
    }

    fun openPrivacy() {
        _uiState.update { it.copy(isPrivacyOpen = true) }
    }

    fun closePrivacy() {
        _uiState.update { it.copy(isPrivacyOpen = false) }
    }

    suspend fun exportBackup(): String = repository.exportBackup()

    suspend fun importBackup(json: String) {
        repository.importBackup(json)
    }

    suspend fun deleteAllData() {
        val context = getApplication<Application>()
        val medicationDao = database.medicationDao()

        medicationDao.getAllMedicationsSync().forEach {
            com.example.aprobile.notifications.AlarmScheduler.cancelAlarmsForMedication(context, it.id, it.times)
        }

        medicationDao.deleteAllDoseRecords()
        medicationDao.deleteAllMedications()
        database.appointmentDao().deleteAllAppointments()
        database.healthDao().deleteAllEvents()
        database.healthDao().deleteAllProfiles()
        database.profileDao().deleteAllProfiles()
        database.profileDao().insertProfile(com.example.aprobile.data.ProfileEntity(name = "Eu", sortOrder = 0))

        java.io.File(context.filesDir, "photos").listFiles()?.forEach { it.delete() }

        _healthProfileName.value = "Eu"
        _uiState.update { it.copy(healthProfileName = "Eu", healthProfile = null) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingDone(true)
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedOneYearData(database.healthDao(), _healthProfileName.value)
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.allMedications,
                repository.getDoseRecordsForDay(_uiState.value.selectedDate),
                repository.allDoseRecords,
                _filters,
                appointmentRepository.allAppointments
            ) { medications, doseRecords, allDoses, filters, appointments ->
                val medMap = medications.associateBy { it.id }

                val currentProfile = filters.selectedProfile
                val query = filters.searchQuery.trim().lowercase()

                val filteredMedications = medications.filter { med ->
                    val profileMatch = currentProfile == "Todos" || med.profile.equals(currentProfile, ignoreCase = true)
                    val queryMatch = query.isEmpty() || med.name.lowercase().contains(query) || med.dosage.lowercase().contains(query)
                    profileMatch && queryMatch
                }

                val filteredMedIds = filteredMedications.map { it.id }.toSet()

                val dosesWithMed = doseRecords.mapNotNull { record ->
                    val med = medMap[record.medicationId]
                    if (med != null && med.id in filteredMedIds) {
                        DoseWithMedication(doseRecord = record, medication = med)
                    } else null
                }.sortedBy { it.doseRecord.scheduledDateTime }

                val takenCount = dosesWithMed.count { it.doseRecord.status == DoseRecordEntity.STATUS_TAKEN }
                val totalCount = dosesWithMed.size

                // Grupos de perfil que possuem ao menos um medicamento cadastrado.
                val profilesWithData = medications
                    .map { it.profile.ifBlank { "Eu" } }
                    .distinct()
                    .sorted()
                val now = System.currentTimeMillis()
                val zone = ZoneId.systemDefault()
                val elapsedDoses = allDoses.filter { DoseAdherence.isElapsed(it.scheduledDateTime, now) }

                val dosesByDay = elapsedDoses
                    .groupBy { Instant.ofEpochMilli(it.scheduledDateTime).atZone(zone).toLocalDate() }
                    .mapValues { (_, list) -> list.count { it.status == DoseRecordEntity.STATUS_TAKEN } to list.size }
                val currentStreak = AdherenceStreak.currentStreak(dosesByDay, LocalDate.now())

                val periodStart = if (filters.statsPeriod.days > 0) {
                    LocalDate.now().minusDays((filters.statsPeriod.days - 1).toLong())
                        .atStartOfDay(zone).toInstant().toEpochMilli()
                } else {
                    Long.MIN_VALUE
                }
                val periodDoses = elapsedDoses.filter { it.scheduledDateTime >= periodStart }

                val overallTaken = periodDoses.count { it.status == DoseRecordEntity.STATUS_TAKEN }
                val overallTotal = periodDoses.size
                val adherence = DoseAdherence.computeRate(overallTaken, overallTotal)

                val profileStats = profilesWithData.map { profile ->
                    val ids = medications
                        .filter { it.profile.ifBlank { "Eu" } == profile }
                        .map { it.id }
                        .toSet()
                    val profileDoses = periodDoses.filter { it.medicationId in ids }
                    val taken = profileDoses.count { it.status == DoseRecordEntity.STATUS_TAKEN }
                    ProfileStat(
                        profile = profile,
                        taken = taken,
                        total = profileDoses.size,
                        adherence = DoseAdherence.computeRate(taken, profileDoses.size)
                    )
                }

                val medicationStats = medications.mapNotNull { med ->
                    val medDoses = periodDoses.filter { it.medicationId == med.id }
                    if (medDoses.isEmpty()) return@mapNotNull null
                    val taken = medDoses.count { it.status == DoseRecordEntity.STATUS_TAKEN }
                    MedicationStat(
                        medicationId = med.id,
                        name = med.name,
                        profile = med.profile,
                        colorTag = med.colorTag,
                        taken = taken,
                        total = medDoses.size,
                        adherence = DoseAdherence.computeRate(taken, medDoses.size)
                    )
                }.sortedByDescending { it.total }

                _uiState.value.copy(
                    medications = filteredMedications,
                    allMedications = medications,
                    allDoseRecords = allDoses,
                    todayDoses = dosesWithMed,
                    takenDosesCount = takenCount,
                    totalDosesCount = totalCount,
                    adherenceRate = adherence,
                    overallTaken = overallTaken,
                    overallTotal = overallTotal,
                    currentStreak = currentStreak,
                    statsPeriod = filters.statsPeriod,
                    profileStats = profileStats,
                    medicationStats = medicationStats,
                    appointments = appointments,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun selectProfile(profile: String) {
        _filters.update { it.copy(selectedProfile = profile) }
        _uiState.update { it.copy(selectedProfile = profile) }
    }

    fun setSearchQuery(query: String) {
        _filters.update { it.copy(searchQuery = query) }
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setStatsPeriod(period: StatsPeriod) {
        _filters.update { it.copy(statsPeriod = period) }
        _uiState.update { it.copy(statsPeriod = period) }
    }

    fun selectHealthProfile(name: String) {
        _healthProfileName.value = name
        _uiState.update { it.copy(healthProfileName = name) }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            settingsRepository.setDarkMode(!_uiState.value.isDarkMode)
        }
    }

    fun setSnoozeMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setSnoozeMinutes(minutes)
        }
    }

    fun updateUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }

    fun loginWithGoogle() {
        _uiState.update {
            it.copy(
                isLoggedIn = true,
                userName = "João Silva",
                userEmail = "joao.silva@gmail.com"
            )
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                userName = "Usuário Convidado",
                userEmail = "Modo offline (sem sincronização)"
            )
        }
    }

    fun toggleDoseStatus(doseWithMed: DoseWithMedication) {
        viewModelScope.launch {
            if (doseWithMed.doseRecord.status == DoseRecordEntity.STATUS_TAKEN) {
                repository.markDoseAsPending(doseWithMed.doseRecord.id)
            } else {
                repository.markDoseAsTaken(doseWithMed.doseRecord.id)
            }
        }
    }

    fun markNextDoseAsTaken(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.markNextPendingDoseAsTaken(medication.id)
        }
    }

    fun snoozeNextDose(medication: MedicationEntity) {
        viewModelScope.launch {
            val dose = repository.getNextPendingDose(medication.id) ?: return@launch
            repository.snoozeDose(medication, dose)
        }
    }

    fun openAddMedicationDialog() {
        val state = _uiState.value
        if (!PremiumLimits.canAddMedication(state.isPremium, state.allMedications.size)) {
            _uiState.update { it.copy(isPaywallOpen = true) }
        } else {
            _uiState.update { it.copy(isAddEditOpen = true, medicationToEdit = null) }
        }
    }

    fun openPaywall() {
        _uiState.update { it.copy(isPaywallOpen = true) }
    }

    fun closePaywall() {
        _uiState.update { it.copy(isPaywallOpen = false) }
    }

    private fun grantPremium() {
        viewModelScope.launch { settingsRepository.setPremium(true) }
        _uiState.update { it.copy(isPaywallOpen = false) }
    }

    fun activatePremium() = grantPremium()

    fun purchasePremium(activity: Activity?) {
        billingRepository.onEntitlement = { grantPremium() }
        if (activity == null) {
            grantPremium()
        } else {
            billingRepository.connectAndPurchase(activity) { grantPremium() }
        }
    }

    fun restorePurchase() = grantPremium()

    fun restorePremium(activity: Activity?) {
        billingRepository.onEntitlement = { grantPremium() }
        if (activity == null) {
            grantPremium()
        } else {
            billingRepository.restorePurchase { grantPremium() }
        }
    }

    fun openEditMedicationDialog(medication: MedicationEntity) {
        _uiState.update { it.copy(isAddEditOpen = true, medicationToEdit = medication) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditOpen = false, medicationToEdit = null) }
    }

    fun saveMedication(
        name: String,
        dosage: String,
        instructions: String?,
        times: List<String>,
        currentQuantity: Int?,
        lowStockAlert: Int?,
        profile: String,
        colorTag: String,
        soundEnabled: Boolean,
        vibrateEnabled: Boolean
    ) {
        viewModelScope.launch {
            val medToEdit = _uiState.value.medicationToEdit
            if (medToEdit == null) {
                val newMed = MedicationEntity(
                    name = name,
                    dosage = dosage,
                    instructions = instructions?.takeIf { it.isNotBlank() },
                    times = times.sorted(),
                    currentQuantity = currentQuantity,
                    lowStockAlert = lowStockAlert,
                    profile = profile.ifBlank { "Eu" },
                    colorTag = colorTag,
                    soundEnabled = soundEnabled,
                    vibrateEnabled = vibrateEnabled
                )
                repository.insertMedication(newMed)
            } else {
                val updated = medToEdit.copy(
                    name = name,
                    dosage = dosage,
                    instructions = instructions?.takeIf { it.isNotBlank() },
                    times = times.sorted(),
                    currentQuantity = currentQuantity,
                    lowStockAlert = lowStockAlert,
                    profile = profile.ifBlank { "Eu" },
                    colorTag = colorTag,
                    soundEnabled = soundEnabled,
                    vibrateEnabled = vibrateEnabled
                )
                repository.updateMedication(updated, medToEdit.times)
            }
            closeAddEditDialog()
        }
    }

    fun toggleMedicationEnabled(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.toggleMedicationEnabled(medication)
        }
    }

    fun requestDeleteMedication(medication: MedicationEntity) {
        _uiState.update { it.copy(medicationToDelete = medication) }
    }

    fun confirmDeleteMedication() {
        val med = _uiState.value.medicationToDelete ?: return
        viewModelScope.launch {
            repository.deleteMedication(med)
            _uiState.update { it.copy(medicationToDelete = null) }
        }
    }

    fun cancelDeleteMedication() {
        _uiState.update { it.copy(medicationToDelete = null) }
    }

    fun openAddAppointmentDialog() {
        _uiState.update { it.copy(isAppointmentDialogOpen = true, appointmentToEdit = null) }
    }

    fun openEditAppointmentDialog(appointment: AppointmentEntity) {
        _uiState.update { it.copy(isAppointmentDialogOpen = true, appointmentToEdit = appointment) }
    }

    fun closeAppointmentDialog() {
        _uiState.update { it.copy(isAppointmentDialogOpen = false, appointmentToEdit = null) }
    }

    fun saveAppointment(
        title: String,
        doctor: String?,
        location: String?,
        dateTime: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            val toEdit = _uiState.value.appointmentToEdit
            val cleanedDoctor = doctor?.takeIf { it.isNotBlank() }
            val cleanedLocation = location?.takeIf { it.isNotBlank() }
            val cleanedNotes = notes?.takeIf { it.isNotBlank() }
            if (toEdit == null) {
                appointmentRepository.insert(
                    AppointmentEntity(
                        title = title,
                        doctor = cleanedDoctor,
                        location = cleanedLocation,
                        dateTime = dateTime,
                        notes = cleanedNotes
                    )
                )
            } else {
                appointmentRepository.update(
                    toEdit.copy(
                        title = title,
                        doctor = cleanedDoctor,
                        location = cleanedLocation,
                        dateTime = dateTime,
                        notes = cleanedNotes
                    )
                )
            }
            closeAppointmentDialog()
        }
    }

    fun requestDeleteAppointment(appointment: AppointmentEntity) {
        _uiState.update { it.copy(appointmentToDelete = appointment) }
    }

    fun confirmDeleteAppointment() {
        val appointment = _uiState.value.appointmentToDelete ?: return
        viewModelScope.launch {
            appointmentRepository.delete(appointment)
            _uiState.update { it.copy(appointmentToDelete = null) }
        }
    }

    fun cancelDeleteAppointment() {
        _uiState.update { it.copy(appointmentToDelete = null) }
    }

    fun openEditHealthProfileDialog() {
        _uiState.update { it.copy(isHealthProfileDialogOpen = true) }
    }

    fun closeHealthProfileDialog() {
        _uiState.update { it.copy(isHealthProfileDialogOpen = false) }
    }

    fun saveHealthProfile(
        bloodType: String?,
        allergies: String?,
        conditions: String?,
        weight: String?,
        height: String?,
        emergencyContact: String?,
        notes: String?
    ) {
        val profileName = _healthProfileName.value
        viewModelScope.launch {
            healthRepository.saveProfile(
                HealthProfileEntity(
                    profile = profileName,
                    bloodType = bloodType?.takeIf { it.isNotBlank() },
                    allergies = allergies?.takeIf { it.isNotBlank() },
                    conditions = conditions?.takeIf { it.isNotBlank() },
                    weight = weight?.takeIf { it.isNotBlank() },
                    height = height?.takeIf { it.isNotBlank() },
                    emergencyContact = emergencyContact?.takeIf { it.isNotBlank() },
                    notes = notes?.takeIf { it.isNotBlank() }
                )
            )
        }
        closeHealthProfileDialog()
    }

    fun openAddHealthEventDialog() {
        _uiState.update { it.copy(isHealthEventDialogOpen = true) }
    }

    fun closeHealthEventDialog() {
        _uiState.update { it.copy(isHealthEventDialogOpen = false) }
    }

    fun saveHealthEvent(
        type: String,
        title: String,
        value: String?,
        notes: String?,
        timestamp: Long,
        photoUri: String?
    ) {
        val profileName = _healthProfileName.value
        viewModelScope.launch {
            healthRepository.insertEvent(
                HealthEventEntity(
                    profile = profileName,
                    timestamp = timestamp,
                    type = type,
                    title = title,
                    value = value?.takeIf { it.isNotBlank() },
                    notes = notes?.takeIf { it.isNotBlank() },
                    photoUri = photoUri
                )
            )
        }
        closeHealthEventDialog()
    }

    fun requestDeleteHealthEvent(event: HealthEventEntity) {
        _uiState.update { it.copy(healthEventToDelete = event) }
    }

    fun confirmDeleteHealthEvent() {
        val event = _uiState.value.healthEventToDelete ?: return
        viewModelScope.launch {
            healthRepository.deleteEvent(event)
            _uiState.update { it.copy(healthEventToDelete = null) }
        }
    }

    fun cancelDeleteHealthEvent() {
        _uiState.update { it.copy(healthEventToDelete = null) }
    }

    fun formatScheduledTime(scheduledDateTimeMillis: Long): String {
        return try {
            val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledDateTimeMillis), ZoneId.systemDefault())
            ldt.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            "--:--"
        }
    }
}
