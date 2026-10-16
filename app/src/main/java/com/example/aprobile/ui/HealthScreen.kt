package com.example.aprobile.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onEditEvent: (HealthEventEntity) -> Unit,
    onDeleteEvent: (HealthEventEntity) -> Unit,
    onUpgrade: () -> Unit
) {
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.share_report)
    val glassColor = glassSurfaceColor(uiState.isDarkMode)
    val borderColor = glassBorderColor(uiState.isDarkMode)
    val realProfiles = uiState.availableProfiles.filter { it != "Todos" }

    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf<String?>(null) }

    val filteredTimeline = uiState.healthTimeline.filter { item ->
        val typeOk = when (item) {
            is HealthTimelineItem.Event -> typeFilter == null || item.event.type == typeFilter
            is HealthTimelineItem.Dose -> typeFilter == null
        }
        val textOk = query.isBlank() || when (item) {
            is HealthTimelineItem.Event ->
                listOfNotNull(item.event.title, item.event.value, item.event.notes)
                    .any { it.contains(query, ignoreCase = true) }
            is HealthTimelineItem.Dose ->
                item.medication.name.contains(query, ignoreCase = true) ||
                    item.medication.dosage.contains(query, ignoreCase = true)
        }
        typeOk && textOk
    }

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
                            label = { Text(if (profile == "Todos") stringResource(R.string.profile_all) else profile) }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.search_diary)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val typeOptions = listOf(
                    null to R.string.filter_all,
                    HealthEventEntity.TYPE_SYMPTOM to R.string.health_type_symptom,
                    HealthEventEntity.TYPE_FEVER to R.string.health_type_fever,
                    HealthEventEntity.TYPE_PRESSURE to R.string.health_type_pressure,
                    HealthEventEntity.TYPE_NOTE to R.string.health_type_note
                )
                typeOptions.forEach { (type, labelRes) ->
                    FilterChip(
                        selected = typeFilter == type,
                        onClick = { typeFilter = type },
                        label = { Text(stringResource(labelRes)) }
                    )
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
        val glucosePoints = HealthMetrics.glucosePoints(uiState.healthEvents)
        val pressurePoints = HealthMetrics.pressurePoints(uiState.healthEvents)

        if (temperaturePoints.size >= 2) {
            item {
                MetricChartCard(
                    title = stringResource(R.string.health_temperature),
                    unit = "°C",
                    points = temperaturePoints,
                    glassColor = glassColor,
                    borderColor = borderColor
                )
            }
        }
        if (glucosePoints.size >= 2) {
            item {
                MetricChartCard(
                    title = stringResource(R.string.health_glucose_chart),
                    unit = "mg/dL",
                    points = glucosePoints,
                    glassColor = glassColor,
                    borderColor = borderColor
                )
            }
        }
        if (pressurePoints.size >= 2) {
            item {
                PressureChartCard(
                    points = pressurePoints,
                    glassColor = glassColor,
                    borderColor = borderColor
                )
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.health_diary),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(if (uiState.isPremium) R.string.health_export else R.string.health_export_locked),
                        maxLines = 1
                    )
                }
            }
        }

        if (filteredTimeline.isEmpty()) {
            item {
                Text(
                    text = if (uiState.healthTimeline.isEmpty()) {
                        stringResource(R.string.health_timeline_empty)
                    } else {
                        stringResource(R.string.health_no_results)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(filteredTimeline) { item ->
                when (item) {
                    is HealthTimelineItem.Dose -> TimelineRow(
                        title = "${stringResource(R.string.health_dose_taken)}: ${item.medication.name}",
                        subtitle = formatDetailDateTime(item.record.scheduledDateTime) +
                            (if (item.medication.dosage.isNotBlank()) " • ${item.medication.dosage}" else ""),
                        onClick = null,
                        onDelete = null,
                        photoPath = null,
                        profileName = item.medication.profile,
                        profilePhoto = uiState.profilePhotos[item.medication.profile],
                        glassColor = glassColor,
                        borderColor = borderColor
                    )

                    is HealthTimelineItem.Event -> TimelineRow(
                        title = listOfNotNull(item.event.title, item.event.value).joinToString(" — "),
                        subtitle = formatDetailDateTime(item.event.timestamp) +
                            (item.event.notes?.let { " • $it" } ?: ""),
                        onClick = { onEditEvent(item.event) },
                        onDelete = { onDeleteEvent(item.event) },
                        photoPath = item.event.photoUri,
                        profileName = item.event.profile,
                        profilePhoto = uiState.profilePhotos[item.event.profile],
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
    title: String,
    subtitle: String,
    onClick: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    photoPath: String?,
    profileName: String,
    profilePhoto: String?,
    glassColor: Color,
    borderColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
            ProfileAvatar(name = profileName, photoPath = profilePhoto, sizeDp = 40)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (photoPath != null) {
                Spacer(modifier = Modifier.width(8.dp))
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
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
private fun MetricChartCard(
    title: String,
    unit: String,
    points: List<Pair<Long, Float>>,
    glassColor: Color,
    borderColor: Color
) {
    val values = points.map { it.second }
    val minValue = values.minOrNull() ?: return
    val maxValue = values.maxOrNull() ?: return
    val lastValue = points.last().second
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatTemp(lastValue)} $unit   •   min ${formatTemp(minValue)}  •  max ${formatTemp(maxValue)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)) {
                val width = size.width
                val height = size.height
                val verticalSpan = (maxValue - minValue).let { if (it < 0.5f) 1f else it }
                val minTime = points.first().first
                val maxTime = points.last().first
                val timeSpan = (maxTime - minTime).let { if (it <= 0L) 1L else it }
                val pad = 14f

                fun px(t: Long) = pad + (t - minTime).toFloat() / timeSpan.toFloat() * (width - 2 * pad)
                fun py(v: Float) = height - pad - (v - minValue) / verticalSpan * (height - 2 * pad)

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

@Composable
private fun PressureChartCard(
    points: List<Triple<Long, Int, Int>>,
    glassColor: Color,
    borderColor: Color
) {
    val systolicColor = MaterialTheme.colorScheme.primary
    val diastolicColor = MaterialTheme.colorScheme.tertiary
    val maxValue = points.maxOf { it.second }
    val minValue = points.minOf { it.third }
    val lastSys = points.last().second
    val lastDia = points.last().third

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
                text = stringResource(R.string.health_pressure_chart),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$lastSys/$lastDia mmHg   •   $minValue–$maxValue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)) {
                val width = size.width
                val height = size.height
                val span = (maxValue - minValue).let { if (it < 5) 10f else it.toFloat() }
                val minTime = points.first().first
                val maxTime = points.last().first
                val timeSpan = (maxTime - minTime).let { if (it <= 0L) 1L else it }
                val pad = 14f

                fun px(t: Long) = pad + (t - minTime).toFloat() / timeSpan.toFloat() * (width - 2 * pad)
                fun py(v: Int) = height - pad - (v - minValue) / span * (height - 2 * pad)

                val sysPath = Path()
                val diaPath = Path()
                points.forEachIndexed { index, p ->
                    val x = px(p.first)
                    val sysY = py(p.second)
                    val diaY = py(p.third)
                    if (index == 0) {
                        sysPath.moveTo(x, sysY)
                        diaPath.moveTo(x, diaY)
                    } else {
                        sysPath.lineTo(x, sysY)
                        diaPath.lineTo(x, diaY)
                    }
                }
                drawPath(path = sysPath, color = systolicColor, style = Stroke(width = 4f))
                drawPath(path = diaPath, color = diastolicColor, style = Stroke(width = 4f))
                points.forEach { p ->
                    drawCircle(color = systolicColor, radius = 7f, center = Offset(px(p.first), py(p.second)))
                    drawCircle(color = diastolicColor, radius = 7f, center = Offset(px(p.first), py(p.third)))
                }
            }
        }
    }
}

private fun formatTemp(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else String.format("%.1f", value)
