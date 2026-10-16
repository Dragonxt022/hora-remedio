package com.example.aprobile.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.streak_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (uiState.currentStreak > 0) {
                        "\uD83D\uDD25 " + pluralStringResource(R.plurals.streak_days, uiState.currentStreak, uiState.currentStreak)
                    } else {
                        stringResource(R.string.streak_none)
                    },
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
            isDarkMode = uiState.isDarkMode
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = glassColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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
            Text(
                text = stringResource(R.string.stats_no_profile),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            uiState.profileStats.forEach { stat ->
                ProfileStatCard(stat = stat, isDarkMode = uiState.isDarkMode)
            }
        }

        Text(
            text = stringResource(R.string.stats_by_medication),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.medicationStats.isEmpty()) {
            Text(
                text = stringResource(R.string.stats_no_medication),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            uiState.medicationStats.forEach { stat ->
                MedicationStatCard(stat = stat, isDarkMode = uiState.isDarkMode)
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
}

@Composable
fun ProfileStatCard(stat: ProfileStat, isDarkMode: Boolean) {
    StatRow(
        title = stat.profile,
        adherence = stat.adherence,
        taken = stat.taken,
        total = stat.total,
        accentColor = null,
        isDarkMode = isDarkMode
    )
}

@Composable
fun MedicationStatCard(stat: MedicationStat, isDarkMode: Boolean) {
    StatRow(
        title = stat.name,
        adherence = stat.adherence,
        taken = stat.taken,
        total = stat.total,
        accentColor = getColorTag(stat.colorTag),
        isDarkMode = isDarkMode
    )
}

@Composable
private fun StatRow(
    title: String,
    adherence: Int,
    taken: Int,
    total: Int,
    accentColor: Color?,
    isDarkMode: Boolean
) {
    val glassColor = glassSurfaceColor(isDarkMode)
    val borderColor = glassBorderColor(isDarkMode)

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
