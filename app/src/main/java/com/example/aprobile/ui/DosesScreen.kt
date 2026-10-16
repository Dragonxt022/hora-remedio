package com.example.aprobile.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.DoseWithMedication
import com.example.aprobile.data.MedicationEntity
import com.example.aprobile.ui.theme.glassBorderColor
import com.example.aprobile.ui.theme.glassSurfaceElevatedColor
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
    onOpenDetail: (MedicationEntity) -> Unit,
    onOpenHealth: () -> Unit
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

        val hp = uiState.activeHealthProfile
        val needsHealth = hp == null || listOf(
            hp.bloodType, hp.allergies, hp.conditions, hp.weight,
            hp.height, hp.emergencyContact, hp.notes
        ).all { it.isNullOrBlank() }
        if (needsHealth) {
            SmartTipCard(
                profileName = uiState.activeHealthProfileName,
                isDarkMode = uiState.isDarkMode,
                onClick = onOpenHealth
            )
        }

        DailySummaryCard(
            takenCount = uiState.takenDosesCount,
            totalCount = uiState.totalDosesCount,
            streak = uiState.currentStreak,
            isDarkMode = uiState.isDarkMode
        )

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

@Composable
private fun SmartTipCard(profileName: String, isDarkMode: Boolean, onClick: () -> Unit) {
    val glassColor = glassSurfaceElevatedColor(isDarkMode)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .border(BorderStroke(1.2.dp, glassBorderColor(isDarkMode)), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.tip_complete_health_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.tip_complete_health_text, profileName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onClick, shape = RoundedCornerShape(12.dp)) {
                Text(stringResource(R.string.tip_complete_health_action))
            }
        }
    }
}
