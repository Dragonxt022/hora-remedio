package com.example.aprobile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }

    val snoozeMinutes: Flow<Int> = context.dataStore.data.map { it[SNOOZE_MINUTES] ?: DEFAULT_SNOOZE }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_DONE] ?: false }

    val isPremium: Flow<Boolean> = context.dataStore.data.map { it[IS_PREMIUM] ?: false }

    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[APP_LOCK] ?: false }

    val customSymptoms: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_SYMPTOMS]?.split("|")?.filter { it.isNotBlank() }.orEmpty()
    }

    val dailyCheckInEnabled: Flow<Boolean> = context.dataStore.data.map { it[DAILY_CHECKIN] ?: true }

    suspend fun setDarkMode(value: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = value }
    }

    suspend fun setSnoozeMinutes(value: Int) {
        context.dataStore.edit { it[SNOOZE_MINUTES] = value }
    }

    suspend fun setOnboardingDone(value: Boolean) {
        context.dataStore.edit { it[ONBOARDING_DONE] = value }
    }

    suspend fun setPremium(value: Boolean) {
        context.dataStore.edit { it[IS_PREMIUM] = value }
    }

    suspend fun setAppLockEnabled(value: Boolean) {
        context.dataStore.edit { it[APP_LOCK] = value }
    }

    suspend fun addCustomSymptom(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            val current = prefs[CUSTOM_SYMPTOMS]?.split("|")?.filter { it.isNotBlank() }.orEmpty()
            if (current.none { it.equals(trimmed, ignoreCase = true) }) {
                prefs[CUSTOM_SYMPTOMS] = (current + trimmed).joinToString("|")
            }
        }
    }

    suspend fun setDailyCheckInEnabled(value: Boolean) {
        context.dataStore.edit { it[DAILY_CHECKIN] = value }
    }

    suspend fun getSnoozeMinutes(): Int =
        context.dataStore.data.first()[SNOOZE_MINUTES] ?: DEFAULT_SNOOZE

    companion object {
        const val DEFAULT_SNOOZE = 15
        private val DARK_MODE = booleanPreferencesKey("dark_mode")
        private val SNOOZE_MINUTES = intPreferencesKey("snooze_minutes")
        private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val APP_LOCK = booleanPreferencesKey("app_lock")
        private val CUSTOM_SYMPTOMS = stringPreferencesKey("custom_symptoms")
        private val DAILY_CHECKIN = booleanPreferencesKey("daily_checkin")
    }
}
