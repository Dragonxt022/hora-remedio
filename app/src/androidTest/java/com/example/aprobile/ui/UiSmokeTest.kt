package com.example.aprobile.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aprobile.R
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.DoseWithMedication
import com.example.aprobile.data.MedicationEntity
import com.example.aprobile.ui.theme.AprobileTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiSmokeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun dosesEmptyState_showsRegisterButton() {
        composeRule.setContent {
            AprobileTheme {
                DosesScreen(
                    uiState = MedicationUiState(isLoading = false),
                    onQueryChange = {}, onSelectProfile = {}, onAddClick = {},
                    onToggleDose = {}, onEdit = {}, onDelete = {}, onOpenDetail = {},
                    onOpenHealth = {}, onLogAsNeeded = {}, onOpenBackup = {}
                )
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.empty_add_button)).assertIsDisplayed()
    }

    @Test
    fun dosesGroupedMedication_showsProgress() {
        val med = MedicationEntity(
            id = 1, name = "Dipirona", dosage = "500 mg",
            times = listOf("08:00", "12:00", "20:00"), profile = "Eu"
        )
        val base = System.currentTimeMillis()
        val doses = listOf(
            DoseWithMedication(DoseRecordEntity(id = 1, medicationId = 1, scheduledDateTime = base, status = DoseRecordEntity.STATUS_TAKEN), med),
            DoseWithMedication(DoseRecordEntity(id = 2, medicationId = 1, scheduledDateTime = base + 1, status = DoseRecordEntity.STATUS_PENDING), med),
            DoseWithMedication(DoseRecordEntity(id = 3, medicationId = 1, scheduledDateTime = base + 2, status = DoseRecordEntity.STATUS_PENDING), med)
        )
        composeRule.setContent {
            AprobileTheme {
                DosesScreen(
                    uiState = MedicationUiState(
                        isLoading = false,
                        todayDoses = doses,
                        medications = listOf(med),
                        allMedications = listOf(med)
                    ),
                    onQueryChange = {}, onSelectProfile = {}, onAddClick = {},
                    onToggleDose = {}, onEdit = {}, onDelete = {}, onOpenDetail = {},
                    onOpenHealth = {}, onLogAsNeeded = {}, onOpenBackup = {}
                )
            }
        }
        composeRule.onNodeWithText("Dipirona").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.daily_taken_of, 1, 3)).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsOptions() {
        composeRule.setContent {
            AprobileTheme {
                SettingsScreen(
                    snoozeMinutes = 15,
                    appLockEnabled = false,
                    onSnoozeChange = {},
                    onAppLockChange = {}
                )
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.snooze_setting)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.app_lock_setting)).assertIsDisplayed()
    }

    @Test
    fun addMedicationDialog_showsTitleAndScheduleSelector() {
        composeRule.setContent {
            AprobileTheme {
                AddEditMedicationDialog(
                    medicationToEdit = null,
                    onDismiss = {},
                    onSave = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> }
                )
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.med_dialog_new)).assertExists()
        composeRule.onNodeWithText(context.getString(R.string.schedule_daily)).assertExists()
    }
}
