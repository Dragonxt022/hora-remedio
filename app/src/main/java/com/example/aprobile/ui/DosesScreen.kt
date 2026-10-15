package com.example.aprobile.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.DoseWithMedication
import com.example.aprobile.data.MedicationEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun formatScheduledTime(scheduledDateTimeMillis: Long): String {
    return try {
        val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledDateTimeMillis), ZoneId.systemDefault())
        ldt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        "--:--"
    }
}

@Composable
fun DosesScreen(
    uiState: MedicationUiState,
    onQueryChange: (String) -> Unit,
    onSelectProfile: (String) -> Unit,
    onAddClick: () -> Unit,
    onToggleDose: (DoseWithMedication) -> Unit,
    onEdit: (MedicationEntity) -> Unit,
    onDelete: (MedicationEntity) -> Unit,
    onOpenDetail: (MedicationEntity) -> Unit
) {
    val view = LocalView.current

    Column(modifier = Modifier.fillMaxSize()) {
        SearchAndFilterBar(
            query = uiState.searchQuery,
            onQueryChange = onQueryChange,
            profiles = uiState.availableProfiles,
            selectedProfile = uiState.selectedProfile,
            onSelectProfile = onSelectProfile,
            isDarkMode = uiState.isDarkMode
        )

        if (uiState.availableProfiles.size > 1) {
            DailySummaryCard(
                takenCount = uiState.takenDosesCount,
                totalCount = uiState.totalDosesCount,
                streak = uiState.currentStreak,
                isDarkMode = uiState.isDarkMode
            )
        }

        if (uiState.isLoading && uiState.todayDoses.isEmpty()) {
            LoadingStateView()
        } else if (uiState.todayDoses.isEmpty()) {
            EmptyStateView(
                message = stringResource(R.string.empty_doses),
                onAddClick = onAddClick,
                isDarkMode = uiState.isDarkMode
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.todayDoses, key = { it.doseRecord.id }) { doseWithMed ->
                    DoseItemCard(
                        doseWithMed = doseWithMed,
                        formattedTime = formatScheduledTime(doseWithMed.doseRecord.scheduledDateTime),
                        onClick = { onOpenDetail(doseWithMed.medication) },
                        onToggleStatus = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            onToggleDose(doseWithMed)
                        },
                        onEdit = { onEdit(doseWithMed.medication) },
                        onDelete = { onDelete(doseWithMed.medication) },
                        isDarkMode = uiState.isDarkMode
                    )
                }
            }
        }
    }
}
