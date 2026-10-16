package com.example.aprobile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.DoseWithMedication
import com.example.aprobile.data.MedicationEntity
import coil.compose.AsyncImage
import com.example.aprobile.domain.InventoryForecast
import com.example.aprobile.ui.theme.glassBorderColor
import com.example.aprobile.ui.theme.glassSurfaceColor
import java.io.File
import java.time.LocalDate

fun getColorTag(tag: String): Color {
    return when (tag.lowercase()) {
        "verde" -> Color(0xFF00A878)
        "vermelho" -> Color(0xFFE53935)
        "roxo" -> Color(0xFF8E24AA)
        "laranja" -> Color(0xFFFF9800)
        else -> Color(0xFF00838F)
    }
}

@Composable
fun InventoryStatus(medication: MedicationEntity, modifier: Modifier = Modifier) {
    val forecast = InventoryForecast.forecast(
        currentQuantity = medication.currentQuantity,
        dosesPerDay = medication.times.size,
        lowStockAlert = medication.lowStockAlert,
        today = LocalDate.now()
    ) ?: return

    Column(modifier = modifier) {
        medication.currentQuantity?.let { qty ->
            Text(
                text = stringResource(R.string.inventory_stock, qty),
                style = MaterialTheme.typography.bodySmall,
                color = if (forecast.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        forecast.runsOutDate?.let {
            Text(
                text = if (forecast.daysRemaining <= 1) {
                    stringResource(R.string.inventory_runs_out_soon)
                } else {
                    stringResource(R.string.inventory_runs_out, forecast.daysRemaining)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (forecast.isLow) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .width(14.dp)
                        .height(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.inventory_reorder),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    profiles: List<String>,
    selectedProfile: String,
    onSelectProfile: (String) -> Unit,
    isDarkMode: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    color = if (isDarkMode) Color.Black.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        if (profiles.size > 1) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles) { profile ->
                    FilterChip(
                        selected = selectedProfile == profile,
                        onClick = { onSelectProfile(profile) },
                        label = { Text(if (profile == "Todos") stringResource(R.string.profile_all) else profile) }
                    )
                }
            }
        }
    }
}

@Composable
fun DailySummaryCard(takenCount: Int, totalCount: Int, streak: Int, isDarkMode: Boolean) {
    val isComplete = totalCount > 0 && takenCount == totalCount
    val glassColor = glassSurfaceColor(isDarkMode)
    val borderColor = glassBorderColor(isDarkMode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(BorderStroke(1.5.dp, borderColor), shape = RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(if (isComplete) R.string.daily_complete else R.string.daily_progress),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.daily_taken_of, takenCount, totalCount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = if (totalCount > 0) takenCount.toFloat() / totalCount.toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (streak > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "\uD83D\uDD25 " + pluralStringResource(R.plurals.streak_days, streak, streak),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DoseItemCard(
    doseWithMed: DoseWithMedication,
    formattedTime: String,
    onClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDarkMode: Boolean,
    profilePhoto: String? = null,
    showProfileAvatar: Boolean = false
) {
    val isTaken = doseWithMed.doseRecord.status == DoseRecordEntity.STATUS_TAKEN
    val accentColor = getColorTag(doseWithMed.medication.colorTag)

    val glassColor = if (isDarkMode) {
        if (isTaken) Color(0xFF16221F).copy(alpha = 0.4f) else Color(0xFF16221F).copy(alpha = 0.85f)
    } else {
        if (isTaken) Color(0xFFE2F3F0).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.9f)
    }
    val borderColor = glassBorderColor(isDarkMode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTaken) 1.dp else 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(95.dp)
                    .background(if (isTaken) accentColor.copy(alpha = 0.5f) else accentColor)
            )

            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showProfileAvatar) {
                    ProfileAvatar(
                        name = doseWithMed.medication.profile,
                        photoPath = profilePhoto,
                        sizeDp = 34
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = if (isTaken) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = doseWithMed.medication.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isTaken) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isTaken) MaterialTheme.colorScheme.outline.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isTaken) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = doseWithMed.medication.profile,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTaken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (doseWithMed.medication.dosage.isNotBlank()) {
                        Text(
                            text = doseWithMed.medication.dosage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val qty = doseWithMed.medication.currentQuantity
                    val alert = doseWithMed.medication.lowStockAlert ?: 5
                    if (qty != null && qty <= alert && !isTaken) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 3.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.height(14.dp).width(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.low_stock_warning, qty),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                IconButton(onClick = onToggleStatus) {
                    if (isTaken) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.cd_mark_pending),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.cd_mark_taken),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_edit),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun MedicationMasterCard(
    medication: MedicationEntity,
    onClick: () -> Unit,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDarkMode: Boolean
) {
    val accentColor = getColorTag(medication.colorTag)
    val glassColor = glassSurfaceColor(isDarkMode)
    val borderColor = glassBorderColor(isDarkMode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(105.dp)
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = medication.profile,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (medication.enabled) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.cd_active),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(2.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.NotificationsOff,
                                contentDescription = stringResource(R.string.cd_inactive),
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }

                    if (medication.dosage.isNotBlank()) {
                        Text(
                            text = "Dosagem: ${medication.dosage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Horários: ${medication.times.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    InventoryStatus(medication = medication)
                }

                Switch(
                    checked = medication.enabled,
                    onCheckedChange = { onToggleEnabled() }
                )

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    taken: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val pending = maxOf(0, total - taken)
    val takenFloat = taken.toFloat()
    val pendingFloat = pending.toFloat()
    val sum = maxOf(1f, takenFloat + pendingFloat)

    val takenSweep = (takenFloat / sum) * 360f

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    val adherenceContentDescription = stringResource(R.string.adherence_content_description, taken, total)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(160.dp)
            .semantics { contentDescription = adherenceContentDescription }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 32f
            drawArc(
                color = surfaceVariantColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = takenSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${if (total > 0) (taken.toFloat() / total * 100).toInt() else 100}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Sucesso",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BarChart(
    taken: Int,
    total: Int,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val missed = maxOf(0, total - taken)
    val maxVal = maxOf(1, total).toFloat()

    val takenFraction = taken / maxVal
    val missedFraction = missed / maxVal

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val glassColor = glassSurfaceColor(isDarkMode)
    val borderColor = glassBorderColor(isDarkMode)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.stats_comparison),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.stats_bar_taken), modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(takenFraction)
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(primaryColor)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "$taken", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.stats_bar_pending), modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(missedFraction)
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(errorColor)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "$missed", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun LoadingStateView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun EmptyStateView(
    message: String,
    onAddClick: () -> Unit,
    isDarkMode: Boolean
) {
    val glassColor = glassSurfaceColor(isDarkMode)
    val borderColor = glassBorderColor(isDarkMode)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = glassColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.empty_add_button))
                }
            }
        }
    }
}

@Composable
fun DoseGroupCard(
    doses: List<DoseWithMedication>,
    onClick: () -> Unit,
    onToggleDose: (DoseWithMedication) -> Unit,
    isDarkMode: Boolean,
    profilePhoto: String? = null,
    showProfileAvatar: Boolean = false
) {
    val med = doses.first().medication
    val accentColor = getColorTag(med.colorTag)
    val glassColor = glassSurfaceColor(isDarkMode)
    val borderColor = glassBorderColor(isDarkMode)
    val takenCount = doses.count { it.doseRecord.status == DoseRecordEntity.STATUS_TAKEN }
    val total = doses.size
    val progress = if (total > 0) takenCount.toFloat() / total.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.2.dp, borderColor), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(IntrinsicSize.Min)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showProfileAvatar) {
                        ProfileAvatar(name = med.profile, photoPath = profilePhoto, sizeDp = 30)
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = med.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = med.profile,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (med.dosage.isNotBlank()) {
                    Text(
                        text = med.dosage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.daily_taken_of, takenCount, total),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    doses.forEach { doseWithMed ->
                        val isTaken = doseWithMed.doseRecord.status == DoseRecordEntity.STATUS_TAKEN
                        FilterChip(
                            selected = isTaken,
                            onClick = { onToggleDose(doseWithMed) },
                            label = { Text(formatScheduledTime(doseWithMed.doseRecord.scheduledDateTime)) },
                            leadingIcon = if (isTaken) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAvatar(name: String, photoPath: String?, sizeDp: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = if (name.isNotBlank()) name.take(1).uppercase() else "?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
