package com.example.aprobile.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.aprobile.R
import com.example.aprobile.data.HealthEventEntity
import com.example.aprobile.domain.HealthMetrics
import com.example.aprobile.domain.ReportCsv
import com.example.aprobile.ui.theme.glassBorderColor
import com.example.aprobile.ui.theme.glassSurfaceColor
import java.io.File

@Composable
fun HealthScreen(
    uiState: MedicationUiState,
    onSelectHealthProfile: (String) -> Unit,
    onEditProfile: () -> Unit,
    onDeleteEvent: (HealthEventEntity) -> Unit,
    onUpgrade: () -> Unit
) {
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.share_report)
    val glassColor = glassSurfaceColor(uiState.isDarkMode)
    val borderColor = glassBorderColor(uiState.isDarkMode)
    val realProfiles = uiState.availableProfiles.filter { it != "Todos" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (realProfiles.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    realProfiles.forEach { profile ->
                        FilterChip(
                            selected = uiState.healthProfileName == profile,
                            onClick = { onSelectHealthProfile(profile) },
                            label = { Text(profile) }
                        )
                    }
                }
            }
        }

        item {
            HealthRecordCard(
                uiState = uiState,
                glassColor = glassColor,
                borderColor = borderColor,
                onEditProfile = onEditProfile
            )
        }

        val temperaturePoints = HealthMetrics.temperaturePoints(uiState.healthEvents)
        if (temperaturePoints.size >= 2) {
            item {
                TemperatureChartCard(
                    points = temperaturePoints,
                    glassColor = glassColor,
                    borderColor = borderColor
                )
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
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
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
                        photoPath = null,
                        glassColor = glassColor,
                        borderColor = borderColor
                    )

                    is HealthTimelineItem.Event -> TimelineRow(
                        icon = Icons.Default.FavoriteBorder,
                        title = listOfNotNull(item.event.title, item.event.value).joinToString(" — "),
                        subtitle = formatDetailDateTime(item.event.timestamp) +
                            (item.event.notes?.let { " • $it" } ?: ""),
                        onDelete = { onDeleteEvent(item.event) },
                        photoPath = item.event.photoUri,
                        glassColor = glassColor,
                        borderColor = borderColor
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthRecordCard(
    uiState: MedicationUiState,
    glassColor: Color,
    borderColor: Color,
    onEditProfile: () -> Unit
) {
    val profile = uiState.healthProfile
    val fields = listOf(
        Triple(Icons.Default.Bloodtype, R.string.health_blood_type, profile?.bloodType),
        Triple(Icons.Default.Warning, R.string.health_allergies, profile?.allergies),
        Triple(Icons.Default.MedicalServices, R.string.health_conditions, profile?.conditions),
        Triple(Icons.Default.MonitorWeight, R.string.health_weight, profile?.weight),
        Triple(Icons.Default.Height, R.string.health_height, profile?.height),
        Triple(Icons.Default.ContactPhone, R.string.health_emergency, profile?.emergencyContact),
        Triple(Icons.Default.Notes, R.string.health_notes, profile?.notes)
    ).filter { !it.third.isNullOrBlank() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${stringResource(R.string.health_record)} — ${uiState.healthProfileName}",
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

            if (fields.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.health_record_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onEditProfile,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.tip_complete_health_action))
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                fields.forEach { (icon, labelRes, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(labelRes),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = value!!,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onDelete: (() -> Unit)?,
    photoPath: String?,
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
            if (photoPath != null) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
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

@Composable
private fun TemperatureChartCard(
    points: List<Pair<Long, Float>>,
    glassColor: Color,
    borderColor: Color
) {
    val temps = points.map { it.second }
    val minTemp = temps.minOrNull() ?: return
    val maxTemp = temps.maxOrNull() ?: return
    val lastTemp = points.last().second
    val lineColor = MaterialTheme.colorScheme.primary

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
                text = stringResource(R.string.health_temperature),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatTemp(lastTemp)} °C   •   min ${formatTemp(minTemp)}  •  max ${formatTemp(maxTemp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)) {
                val width = size.width
                val height = size.height
                val verticalSpan = (maxTemp - minTemp).let { if (it < 0.5f) 1f else it }
                val minTime = points.first().first
                val maxTime = points.last().first
                val timeSpan = (maxTime - minTime).let { if (it <= 0L) 1L else it }
                val pad = 14f

                fun px(t: Long) = pad + (t - minTime).toFloat() / timeSpan.toFloat() * (width - 2 * pad)
                fun py(v: Float) = height - pad - (v - minTemp) / verticalSpan * (height - 2 * pad)

                val path = Path()
                points.forEachIndexed { index, (t, v) ->
                    val x = px(t)
                    val y = py(v)
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path = path, color = lineColor, style = Stroke(width = 4f))
                points.forEach { (t, v) ->
                    drawCircle(color = lineColor, radius = 7f, center = Offset(px(t), py(v)))
                }
            }
        }
    }
}

private fun formatTemp(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else String.format("%.1f", value)
