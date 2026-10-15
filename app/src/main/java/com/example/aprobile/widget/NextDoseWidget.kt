package com.example.aprobile.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.aprobile.data.AppDatabase
import com.example.aprobile.data.DoseRecordEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NextDoseWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val (title, subtitle) = loadNextDose(context)
        provideContent {
            WidgetContent(title = title, subtitle = subtitle)
        }
    }

    private suspend fun loadNextDose(context: Context): Pair<String, String> {
        return try {
            val dao = AppDatabase.getDatabase(context).medicationDao()
            val medMap = dao.getAllMedicationsSync().associateBy { it.id }
            val now = System.currentTimeMillis()
            val next = dao.getAllDoseRecordsSync()
                .filter { it.status == DoseRecordEntity.STATUS_PENDING && it.scheduledDateTime >= now }
                .minByOrNull { it.scheduledDateTime }

            if (next == null) {
                "Hora do Remédio" to "Nenhuma dose pendente"
            } else {
                val med = medMap[next.medicationId]
                val time = LocalDateTime.ofInstant(Instant.ofEpochMilli(next.scheduledDateTime), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
                val name = med?.name ?: "Medicamento"
                "Próxima: $name" to "${med?.dosage?.takeIf { it.isNotBlank() }?.let { "$it • " } ?: ""}às $time"
            }
        } catch (e: Exception) {
            "Hora do Remédio" to "Abra o app para atualizar"
        }
    }
}

@Composable
private fun WidgetContent(title: String, subtitle: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF00A878))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFFFFFFFF)), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = subtitle,
            style = TextStyle(color = androidx.glance.unit.ColorProvider(Color(0xFFE0F5EE)), fontSize = 12.sp)
        )
    }
}

class NextDoseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextDoseWidget()
}
