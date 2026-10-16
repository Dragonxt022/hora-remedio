package com.example.aprobile.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.aprobile.R
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.HealthEventEntity
import com.example.aprobile.data.HealthProfileEntity
import com.example.aprobile.data.MedicationEntity
import com.example.aprobile.domain.InventoryForecast
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReportPdf {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val BOTTOM = 800f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun generate(
        context: Context,
        medications: List<MedicationEntity>,
        doses: List<DoseRecordEntity>,
        healthProfile: HealthProfileEntity? = null,
        healthEvents: List<HealthEventEntity> = emptyList()
    ): File {
        val document = PdfDocument()

        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true; color = Color.BLACK }
        val sectionPaint = Paint().apply { textSize = 13f; isFakeBoldText = true; color = Color.BLACK }
        val textPaint = Paint().apply { textSize = 10f; color = Color.BLACK }
        val smallPaint = Paint().apply { textSize = 9f; color = Color.DKGRAY }
        val greenPaint = Paint().apply { color = Color.rgb(0, 150, 136) }
        val grayPaint = Paint().apply { color = Color.rgb(224, 224, 224) }
        val redPaint = Paint().apply { color = Color.rgb(198, 60, 60) }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = 60f

        fun newPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page.canvas
            y = 60f
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > BOTTOM) newPage()
        }

        canvas.drawText(context.getString(R.string.pdf_title), MARGIN, y, titlePaint); y += 22f
        canvas.drawText(
            context.getString(R.string.pdf_generated, LocalDateTime.now().format(dateTimeFormatter)),
            MARGIN, y, smallPaint
        ); y += 26f

        val medMap = medications.associateBy { it.id }
        val taken = doses.count { it.status == DoseRecordEntity.STATUS_TAKEN }
        val total = doses.size

        canvas.drawText(context.getString(R.string.pdf_summary), MARGIN, y, sectionPaint); y += 18f
        canvas.drawText(context.getString(R.string.pdf_medications_count, medications.size), MARGIN, y, textPaint); y += 14f
        canvas.drawText(context.getString(R.string.pdf_recorded, total), MARGIN, y, textPaint); y += 14f
        canvas.drawText(context.getString(R.string.pdf_taken, taken), MARGIN, y, textPaint); y += 14f
        canvas.drawText(context.getString(R.string.pdf_not_taken, total - taken), MARGIN, y, textPaint); y += 24f

        // Adesão (barra)
        val percent = if (total > 0) taken * 100 / total else 0
        canvas.drawText(context.getString(R.string.pdf_adherence), MARGIN, y, sectionPaint); y += 16f
        canvas.drawText(context.getString(R.string.pdf_adherence_value, percent, taken, total), MARGIN, y, textPaint); y += 8f
        val barTop = y
        canvas.drawRect(RectF(MARGIN, barTop, MARGIN + CONTENT_WIDTH, barTop + 14f), grayPaint)
        if (total > 0) {
            canvas.drawRect(RectF(MARGIN, barTop, MARGIN + CONTENT_WIDTH * taken / total, barTop + 14f), greenPaint)
        }
        y += 30f

        // Sintomas / registros de saúde
        canvas.drawText(context.getString(R.string.pdf_symptoms), MARGIN, y, sectionPaint); y += 16f
        if (healthEvents.isEmpty()) {
            canvas.drawText(context.getString(R.string.pdf_no_symptoms), MARGIN, y, textPaint); y += 24f
        } else {
            val byType = healthEvents.groupingBy { it.type }.eachCount()
                .toList().sortedByDescending { it.second }
            val maxCount = byType.maxOf { it.second }.coerceAtLeast(1)
            byType.forEach { (type, count) ->
                ensureSpace(30f)
                canvas.drawText("${typeLabel(context, type)} ($count)", MARGIN, y, textPaint); y += 12f
                val w = CONTENT_WIDTH * count / maxCount
                canvas.drawRect(RectF(MARGIN, y, MARGIN + w, y + 8f), greenPaint)
                y += 18f
            }
            y += 6f
        }

        // Estoque
        val stockMeds = medications.filter { it.currentQuantity != null }
        canvas.drawText(context.getString(R.string.pdf_stock), MARGIN, y, sectionPaint); y += 16f
        if (stockMeds.isEmpty()) {
            canvas.drawText(context.getString(R.string.pdf_stock_none), MARGIN, y, textPaint); y += 24f
        } else {
            val today = LocalDate.now()
            stockMeds.forEach { med ->
                ensureSpace(20f)
                val forecast = InventoryForecast.forecast(med.currentQuantity, med.times.size, med.lowStockAlert, today)
                val detail = when {
                    forecast == null -> context.getString(R.string.pdf_stock_untracked)
                    forecast.dosesPerDay <= 0 -> context.getString(R.string.pdf_stock_no_schedule)
                    forecast.daysRemaining <= 1 -> context.getString(R.string.pdf_stock_runs_out_soon)
                    else -> context.getString(R.string.pdf_stock_days, forecast.daysRemaining)
                }
                val line = "${med.name} — ${med.currentQuantity} un. ($detail)"
                canvas.drawText(line, MARGIN, y, if (forecast?.isLow == true) redPaint else textPaint)
                y += 14f
            }
            y += 10f
        }

        if (healthProfile != null) {
            val recordLines = listOfNotNull(
                healthProfile.bloodType?.let { context.getString(R.string.health_blood_type) to it },
                healthProfile.allergies?.let { context.getString(R.string.health_allergies) to it },
                healthProfile.conditions?.let { context.getString(R.string.health_conditions) to it },
                healthProfile.weight?.let { context.getString(R.string.health_weight) to it },
                healthProfile.height?.let { context.getString(R.string.health_height) to it },
                healthProfile.emergencyContact?.let { context.getString(R.string.health_emergency) to it },
                healthProfile.notes?.let { context.getString(R.string.health_notes) to it }
            )
            if (recordLines.isNotEmpty()) {
                ensureSpace(24f + recordLines.size * 14f)
                canvas.drawText(
                    context.getString(R.string.pdf_record_section) + " — " + healthProfile.profile,
                    MARGIN, y, sectionPaint
                ); y += 18f
                recordLines.forEach { (label, value) ->
                    canvas.drawText("$label: $value", MARGIN, y, textPaint); y += 14f
                }
                y += 10f
            }
        }

        ensureSpace(40f)
        canvas.drawText(context.getString(R.string.pdf_history), MARGIN, y, sectionPaint); y += 18f
        canvas.drawText(context.getString(R.string.pdf_table_header), MARGIN, y, smallPaint); y += 14f

        val zone = ZoneId.systemDefault()
        doses.sortedBy { it.scheduledDateTime }.forEach { dose ->
            if (y > BOTTOM) newPage()
            val med = medMap[dose.medicationId]
            val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(dose.scheduledDateTime), zone)
            val line = buildString {
                append(dateTime.format(dateTimeFormatter).padEnd(20))
                append((med?.name ?: "-").take(24).padEnd(26))
                append((med?.profile ?: "-").take(10).padEnd(13))
                append(
                    if (dose.status == DoseRecordEntity.STATUS_TAKEN) context.getString(R.string.status_taken)
                    else context.getString(R.string.status_not_taken)
                )
            }
            canvas.drawText(line, MARGIN, y, textPaint)
            y += 14f
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, "relatorio-hora-do-remedio.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    private fun typeLabel(context: Context, type: String): String = when (type) {
        HealthEventEntity.TYPE_SYMPTOM -> context.getString(R.string.health_type_symptom)
        HealthEventEntity.TYPE_FEVER -> context.getString(R.string.health_type_fever)
        HealthEventEntity.TYPE_PRESSURE -> context.getString(R.string.health_type_pressure)
        HealthEventEntity.TYPE_GLUCOSE -> context.getString(R.string.health_type_glucose)
        HealthEventEntity.TYPE_NOTE -> context.getString(R.string.health_type_note)
        else -> context.getString(R.string.health_type_other)
    }
}
