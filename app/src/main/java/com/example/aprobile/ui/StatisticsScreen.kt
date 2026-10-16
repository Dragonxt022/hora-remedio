package com.example.aprobile.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.aprobile.R
import com.example.aprobile.domain.ReportCsv
import com.example.aprobile.ui.theme.glassBorderColor
import com.example.aprobile.ui.theme.glassSurfaceColor

@Composable
fun StatisticsScreen(
    uiState: MedicationUiState,
    onSelectPeriod: (StatsPeriod) -> Unit,
    onUpgrade: () -> Unit
) {
    val context = LocalContext.current
    val shareReportTitle = stringResource(R.string.share_report)
    val glassColor = glassSurfaceColor(uiState.isDarkMode)
    val borderColor = glassBorderColor(uiState.isDarkMode)
    var detail by remember { mutableStateOf<StatDetail?>(null) }

    val adherenceRows = listOf(
        stringResource(R.string.stats_detail_success) to "${uiState.adherenceRate}%",
        stringResource(R.string.stats_detail_taken) to uiState.overallTaken.toString(),
        stringResource(R.string.stats_detail_missed) to (uiState.overallTotal - uiState.overallTaken).toString(),
        stringResource(R.string.stats_detail_total) to uiState.overallTotal.toString(),
        stringResource(R.string.stats_detail_period) to uiState.statsPeriod.label
    )
    val streakValue = if (uiState.currentStreak > 0) {
        "\uD83D\uDD25 " + pluralStringResource(R.plurals.streak_days, uiState.currentStreak, uiState.currentStreak)
    } else {
        stringResource(R.string.streak_none)
    }
    val streakDetail = StatDetail(
        title = stringResource(R.string.streak_title),
        subtitle = stringResource(R.string.stats_streak_help),
        icon = Icons.Default.Star,
        rows = listOf(stringResource(R.string.stats_streak_current) to streakValue)
    )
    val adherenceDetail = StatDetail(
        title = stringResource(R.string.stats_detail_adherence_title),
        subtitle = stringResource(R.string.stats_detail_subtitle),
        icon = Icons.Default.BarChart,
        rows = adherenceRows
    )
    val comparisonDetail = StatDetail(
        title = stringResource(R.string.stats_comparison),
        subtitle = stringResource(R.string.stats_detail_subtitle),
        icon = Icons.Default.BarChart,
        rows = adherenceRows
    )
    val historyDetail = StatDetail(
        title = stringResource(R.string.stats_history_summary),
        subtitle = stringResource(R.string.stats_detail_subtitle),
        icon = Icons.Default.Info,
        rows = adherenceRows
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = glassColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { detail = streakDetail }
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.streak_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = streakValue,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.currentStreak > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsPeriod.entries.forEach { period ->
                FilterChip(
                    selected = uiState.statsPeriod == period,
                    onClick = { onSelectPeriod(period) },
                    label = { Text(period.label) }
                )
            }
        }

        Text(
            text = stringResource(R.string.stats_overview),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

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
                    .clickable { detail = adherenceDetail }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.stats_pie),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                DonutChart(taken = uiState.overallTaken, total = uiState.overallTotal)
            }
        }

        BarChart(
            taken = uiState.overallTaken,
            total = uiState.overallTotal,
            isDarkMode = uiState.isDarkMode,
            onClick = { detail = comparisonDetail }
        )

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
                    .clickable { detail = historyDetail }
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.stats_history_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(R.string.stats_recorded, uiState.overallTotal))
                Text(stringResource(R.string.stats_taken, uiState.overallTaken))
                Text(stringResource(R.string.stats_missed, uiState.overallTotal - uiState.overallTaken))
            }
        }

        Text(
            text = stringResource(R.string.stats_by_profile),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.profileStats.isEmpty()) {
            StatsEmptyCard(
                icon = Icons.Default.Person,
                message = stringResource(R.string.stats_no_profile),
                glassColor = glassColor,
                borderColor = borderColor
            )
        } else {
            uiState.profileStats.forEach { stat ->
                val profileDetail = StatDetail(
                    title = stat.profile,
                    subtitle = stringResource(R.string.stats_by_profile),
                    icon = Icons.Default.Person,
                    rows = listOf(
                        stringResource(R.string.stats_detail_success) to "${stat.adherence}%",
                        stringResource(R.string.stats_detail_taken) to stat.taken.toString(),
                        stringResource(R.string.stats_detail_missed) to (stat.total - stat.taken).toString(),
                        stringResource(R.string.stats_detail_total) to stat.total.toString()
                    )
                )
                ProfileStatCard(
                    stat = stat,
                    isDarkMode = uiState.isDarkMode,
                    onClick = { detail = profileDetail }
                )
            }
        }

        Text(
            text = stringResource(R.string.stats_by_medication),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.medicationStats.isEmpty()) {
            StatsEmptyCard(
                icon = Icons.Default.Medication,
                message = stringResource(R.string.stats_no_medication),
                glassColor = glassColor,
                borderColor = borderColor
            )
        } else {
            uiState.medicationStats.forEach { stat ->
                val medicationDetail = StatDetail(
                    title = stat.name,
                    subtitle = stringResource(R.string.stats_by_medication),
                    icon = Icons.Default.Medication,
                    rows = listOf(
                        stringResource(R.string.stats_detail_success) to "${stat.adherence}%",
                        stringResource(R.string.stats_detail_taken) to stat.taken.toString(),
                        stringResource(R.string.stats_detail_missed) to (stat.total - stat.taken).toString(),
                        stringResource(R.string.stats_detail_total) to stat.total.toString()
                    )
                )
                MedicationStatCard(
                    stat = stat,
                    isDarkMode = uiState.isDarkMode,
                    onClick = { detail = medicationDetail }
                )
            }
        }

        Button(
            onClick = {
                if (uiState.isPremium) {
                    val csv = ReportCsv.buildCsv(uiState.allMedications, uiState.allDoseRecords)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_SUBJECT, "Relatório - Hora do Remédio")
                        putExtra(Intent.EXTRA_TEXT, csv)
                    }
                    context.startActivity(Intent.createChooser(intent, shareReportTitle))
                } else {
                    onUpgrade()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(
                    if (uiState.isPremium) R.string.export_report else R.string.export_locked
                )
            )
        }

        OutlinedButton(
            onClick = {
                if (uiState.isPremium) {
                    try {
                        val file = ReportPdf.generate(
                            context,
                            uiState.allMedications,
                            uiState.allDoseRecords,
                            uiState.activeHealthProfile
                        )
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, shareReportTitle))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    onUpgrade()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.export_pdf))
        }
    }

    detail?.let {
        StatDetailDialog(detail = it, onDismiss = { detail = null })
    }
}

@Composable
fun ProfileStatCard(stat: ProfileStat, isDarkMode: Boolean, onClick: () -> Unit) {
    StatRow(
        title = stat.profile,
        adherence = stat.adherence,
        taken = stat.taken,
        total = stat.total,
        accentColor = null,
        isDarkMode = isDarkMode,
        onClick = onClick
    )
}

@Composable
fun MedicationStatCard(stat: MedicationStat, isDarkMode: Boolean, onClick: () -> Unit) {
    StatRow(
        title = stat.name,
        adherence = stat.adherence,
        taken = stat.taken,
        total = stat.total,
        accentColor = getColorTag(stat.colorTag),
        isDarkMode = isDarkMode,
        onClick = onClick
    )
}

@Composable
private fun StatRow(
    title: String,
    adherence: Int,
    taken: Int,
    total: Int,
    accentColor: Color?,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val glassColor = glassSurfaceColor(isDarkMode)
    val borderColor = glassBorderColor(isDarkMode)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (accentColor != null) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${adherence}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { if (total > 0) taken.toFloat() / total.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.stats_doses_taken_of, taken, total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsEmptyCard(
    icon: ImageVector,
    message: String,
    glassColor: Color,
    borderColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class StatDetail(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val rows: List<Pair<String, String>>
)

@Composable
private fun StatDetailDialog(detail: StatDetail, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        icon = { Icon(detail.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(detail.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = detail.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                detail.rows.forEach { (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}
