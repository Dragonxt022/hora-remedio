package com.example.aprobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.MedicationEntity
import com.example.aprobile.domain.DoseAdherence
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")

fun formatDetailDateTime(millis: Long): String {
    return try {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
            .format(dateTimeFormatter)
    } catch (e: Exception) {
        "--"
    }
}

@Composable
fun MedicationDetailScreen(
    medicationId: Long,
    allMedications: List<MedicationEntity>,
    allDoseRecords: List<DoseRecordEntity>,
    isDarkMode: Boolean,
    onTakeNow: (MedicationEntity) -> Unit,
    onSnooze: (MedicationEntity) -> Unit
) {
    val medication = allMedications.firstOrNull { it.id == medicationId }

    if (medication == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.detail_not_found),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val history = allDoseRecords
        .filter { it.medicationId == medicationId }
        .sortedByDescending { it.scheduledDateTime }

    val now = System.currentTimeMillis()
    val elapsed = history.filter { DoseAdherence.isElapsed(it.scheduledDateTime, now) }
    val taken = elapsed.count { it.status == DoseRecordEntity.STATUS_TAKEN }
    val missedOrPending = elapsed.size - taken
    val hasPending = history.any { it.status == DoseRecordEntity.STATUS_PENDING }
    val adherence = DoseAdherence.computeRate(taken, elapsed.size)

    val accentColor = getColorTag(medication.colorTag)
    val glassColor = if (isDarkMode) Color(0xFF16221F).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = glassColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(140.dp)
                            .background(accentColor)
                    )
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = medication.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = medication.profile,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (medication.dosage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.detail_dosage, medication.dosage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.detail_times, medication.times.joinToString(", ")),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (!medication.instructions.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.detail_instructions, medication.instructions),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        medication.currentQuantity?.let { qty ->
                            val alert = medication.lowStockAlert ?: 5
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (qty <= alert) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = stringResource(R.string.inventory_stock, qty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (qty <= alert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (hasPending) {
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
                        Text(
                            text = stringResource(R.string.detail_next_pending),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onTakeNow(medication) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.detail_take_now))
                            }
                            OutlinedButton(
                                onClick = { onSnooze(medication) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.detail_snooze))
                            }
                        }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.detail_adherence),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$adherence%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$taken tomadas",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$missedOrPending pendentes/perdidas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.detail_history),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (history.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.detail_no_history),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(history, key = { it.id }) { record ->
                DoseHistoryRow(record = record, isDarkMode = isDarkMode)
            }
        }
    }
}

@Composable
private fun DoseHistoryRow(record: DoseRecordEntity, isDarkMode: Boolean) {
    val isTaken = record.status == DoseRecordEntity.STATUS_TAKEN
    val isFuture = record.scheduledDateTime > System.currentTimeMillis()
    val glassColor = if (isDarkMode) Color(0xFF16221F).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f)

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
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isTaken) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = if (isTaken) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = if (isTaken) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDetailDateTime(record.scheduledDateTime),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val subtitle = when {
                    isTaken -> record.takenAt?.let { stringResource(R.string.detail_confirmed_at, formatDetailDateTime(it)) }
                        ?: stringResource(R.string.detail_confirmed)
                    isFuture -> stringResource(R.string.detail_scheduled)
                    else -> stringResource(R.string.detail_not_confirmed)
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val statusText: String
            val statusColor: Color
            when {
                isTaken -> {
                    statusText = stringResource(R.string.status_taken)
                    statusColor = MaterialTheme.colorScheme.primary
                }
                isFuture -> {
                    statusText = stringResource(R.string.status_scheduled)
                    statusColor = MaterialTheme.colorScheme.outline
                }
                else -> {
                    statusText = stringResource(R.string.status_not_taken)
                    statusColor = MaterialTheme.colorScheme.error
                }
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}
