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
import androidx.compose.material.icons.filled.Add
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
import com.example.aprobile.ui.theme.glassSurfaceColor
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
    onOpenHealth: () -> Unit,
    onLogAsNeeded: (MedicationEntity) -> Unit
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

        val sosMeds = uiState.medications.filter { it.scheduleType == MedicationEntity.SCHEDULE_AS_NEED }
        if (sosMeds.isNotEmpty()) {
            SosSection(meds = sosMeds, isDarkMode = uiState.isDarkMode, onLog = onLogAsNeeded)
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
                val doseGroups = uiState.todayDoses
                    .groupBy { it.medication.id }
                    .values
                    .map { group -> group.sortedBy { it.doseRecord.scheduledDateTime } }
                items(doseGroups, key = { it.first().medication.id }) { group ->
                    if (group.size == 1) {
                        val doseWithMed = group.first()
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
                            isDarkMode = uiState.isDarkMode,
                            profilePhoto = uiState.profilePhotos[doseWithMed.medication.profile],
                            showProfileAvatar = uiState.selectedProfile == "Todos"
                        )
                    } else {
                        DoseGroupCard(
                            doses = group,
                            onClick = { onOpenDetail(group.first().medication) },
                            onToggleDose = {
                                view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                                onToggleDose(it)
                            },
                            isDarkMode = uiState.isDarkMode,
                            profilePhoto = uiState.profilePhotos[group.first().medication.profile],
                            showProfileAvatar = uiState.selectedProfile == "Todos"
                        )
                    }
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

@Composable
private fun SosSection(meds: List<MedicationEntity>, isDarkMode: Boolean, onLog: (MedicationEntity) -> Unit) {
    val glassColor = glassSurfaceColor(isDarkMode)
    val borderColor = glassBorderColor(isDarkMode)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(
            text = stringResource(R.string.sos_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        meds.forEach { med ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(BorderStroke(1.2.dp, borderColor), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = glassColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = med.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (med.dosage.isNotBlank()) {
                            Text(
                                text = med.dosage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(onClick = { onLog(med) }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.width(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.sos_log))
                    }
                }
            }
        }
    }
}
