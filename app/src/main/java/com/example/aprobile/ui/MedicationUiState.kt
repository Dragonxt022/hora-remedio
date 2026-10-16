package com.example.aprobile.ui

import com.example.aprobile.data.AppointmentEntity
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.DoseWithMedication
import com.example.aprobile.data.HealthEventEntity
import com.example.aprobile.data.HealthProfileEntity
import com.example.aprobile.data.MedicationEntity
import java.time.LocalDate

sealed interface HealthTimelineItem {
    val timestamp: Long

    data class Dose(
        val medication: MedicationEntity,
        val record: DoseRecordEntity
    ) : HealthTimelineItem {
        override val timestamp: Long get() = record.scheduledDateTime
    }

    data class Event(val event: HealthEventEntity) : HealthTimelineItem {
        override val timestamp: Long get() = event.timestamp
    }
}

data class ProfileStat(
    val profile: String,
    val taken: Int,
    val total: Int,
    val adherence: Int
)

data class MedicationStat(
    val medicationId: Long,
    val name: String,
    val profile: String,
    val colorTag: String,
    val taken: Int,
    val total: Int,
    val adherence: Int
)

enum class StatsPeriod(val days: Int, val label: String) {
    DAYS_7(7, "7 dias"),
    DAYS_30(30, "30 dias"),
    DAYS_90(90, "90 dias"),
    ALL(0, "Tudo")
}

data class FilterParams(
    val selectedProfile: String = "Todos",
    val searchQuery: String = "",
    val statsPeriod: StatsPeriod = StatsPeriod.DAYS_30
)

data class MedicationUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val medications: List<MedicationEntity> = emptyList(),
    val allMedications: List<MedicationEntity> = emptyList(),
    val allDoseRecords: List<DoseRecordEntity> = emptyList(),
    val todayDoses: List<DoseWithMedication> = emptyList(),
    val takenDosesCount: Int = 0,
    val totalDosesCount: Int = 0,
    val selectedProfile: String = "Todos",
    val availableProfiles: List<String> = listOf("Todos"),
    val managedProfiles: List<String> = emptyList(),
    val mainProfileName: String = "Eu",
    val mainProfilePhoto: String? = null,
    val profilePhotos: Map<String, String> = emptyMap(),
    val isProfileManagerOpen: Boolean = false,
    val searchQuery: String = "",
    val isAddEditOpen: Boolean = false,
    val medicationToEdit: MedicationEntity? = null,
    val medicationToDelete: MedicationEntity? = null,
    val isDarkMode: Boolean = false,
    val snoozeMinutes: Int = 15,
    val onboardingDone: Boolean = false,
    val isPremium: Boolean = false,
    val isPaywallOpen: Boolean = false,
    val showThanksDialog: Boolean = false,
    val appLockEnabled: Boolean = false,
    val isAppUnlocked: Boolean = false,
    val isPrivacyOpen: Boolean = false,
    val appointments: List<AppointmentEntity> = emptyList(),
    val isAppointmentDialogOpen: Boolean = false,
    val appointmentToEdit: AppointmentEntity? = null,
    val appointmentToDelete: AppointmentEntity? = null,
    val healthProfile: HealthProfileEntity? = null,
    val healthProfileName: String = "Eu",
    val activeHealthProfile: HealthProfileEntity? = null,
    val activeHealthProfileName: String = "Eu",
    val healthEvents: List<HealthEventEntity> = emptyList(),
    val healthTimeline: List<HealthTimelineItem> = emptyList(),
    val isHealthProfileDialogOpen: Boolean = false,
    val isHealthEventDialogOpen: Boolean = false,
    val healthEventToEdit: HealthEventEntity? = null,
    val customSymptoms: List<String> = emptyList(),
    val dailyCheckInEnabled: Boolean = true,
    val lastBackupAt: Long = 0L,
    val healthEventToDelete: HealthEventEntity? = null,
    val isLoading: Boolean = true,
    val adherenceRate: Int = 0,
    val overallTaken: Int = 0,
    val overallTotal: Int = 0,
    val currentStreak: Int = 0,
    val statsPeriod: StatsPeriod = StatsPeriod.DAYS_30,
    val profileStats: List<ProfileStat> = emptyList(),
    val medicationStats: List<MedicationStat> = emptyList(),
    val userName: String = "Usuário Convidado",
    val userEmail: String = "Modo offline (sem sincronização)",
    val isLoggedIn: Boolean = false
)
