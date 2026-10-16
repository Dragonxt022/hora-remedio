package com.example.aprobile.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.HealthEventEntity
import com.example.aprobile.domain.ReportCsv

@Composable
fun HealthScreen(
    uiState: MedicationUiState,
    onSelectProfile: (String) -> Unit,
    onEditProfile: () -> Unit,
    onDeleteEvent: (HealthEventEntity) -> Unit,
    onUpgrade: () -> Unit
) {
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.share_report)
    val glassColor = if (uiState.isDarkMode) Color(0xFF16221F).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
    val borderColor = if (uiState.isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.availableProfiles.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableProfiles.forEach { profile ->
                        FilterChip(
                            selected = uiState.selectedProfile == profile,
                            onClick = { onSelectProfile(profile) },
                            label = { Text(if (profile == "Todos") stringResource(R.string.profile_all) else profile) }
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = glassColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.health_record),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.health_record_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onEditProfile) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.health_edit))
                        }
                    }

                    val profile = uiState.healthProfile
                    val rows = listOfNotNull(
                        profile?.bloodType?.let { stringResource(R.string.health_blood_type) to it },
                        profile?.allergies?.let { stringResource(R.string.health_allergies) to it },
                        profile?.conditions?.let { stringResource(R.string.health_conditions) to it },
                        profile?.weight?.let { stringResource(R.string.health_weight) to it },
                        profile?.height?.let { stringResource(R.string.health_height) to it },
                        profile?.emergencyContact?.let { stringResource(R.string.health_emergency) to it },
                        profile?.notes?.let { stringResource(R.string.health_notes) to it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    if (rows.isEmpty()) {
                        Text(
                            text = stringResource(R.string.health_record_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        rows.forEach { (label, value) ->
                            Text(
                                text = "$label: $value",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.health_diary),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = {
                        if (uiState.isPremium) {
                            val csv = ReportCsv.buildHealthCsv(
                                events = uiState.healthEvents,
                                doses = uiState.allDoseRecords,
                                medications = uiState.allMedications
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_SUBJECT, "Hora do Remédio - Diário")
                                putExtra(Intent.EXTRA_TEXT, csv)
                            }
                            context.startActivity(Intent.createChooser(intent, shareTitle))
                        } else {
                            onUpgrade()
                        }
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.width(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(if (uiState.isPremium) R.string.health_export else R.string.health_export_locked),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        if (uiState.healthTimeline.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.health_timeline_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(uiState.healthTimeline) { item ->
                when (item) {
                    is HealthTimelineItem.Dose -> TimelineRow(
                        icon = Icons.Default.Medication,
                        title = "${stringResource(R.string.health_dose_taken)}: ${item.medication.name}",
                        subtitle = formatDetailDateTime(item.record.scheduledDateTime) +
                            (if (item.medication.dosage.isNotBlank()) " • ${item.medication.dosage}" else ""),
                        onDelete = null,
                        glassColor = glassColor,
                        borderColor = borderColor
                    )

                    is HealthTimelineItem.Event -> TimelineRow(
                        icon = Icons.Default.FavoriteBorder,
                        title = listOfNotNull(item.event.title, item.event.value).joinToString(" — "),
                        subtitle = formatDetailDateTime(item.event.timestamp) +
                            (item.event.notes?.let { " • $it" } ?: ""),
                        onDelete = { onDeleteEvent(item.event) },
                        glassColor = glassColor,
                        borderColor = borderColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onDelete: (() -> Unit)?,
    glassColor: Color,
    borderColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, borderColor), shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
