package com.example.aprobile.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.aprobile.R
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.HealthProfileEntity
import com.example.aprobile.data.MedicationEntity
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReportPdf {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val BOTTOM = 800f

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun generate(
        context: Context,
        medications: List<MedicationEntity>,
        doses: List<DoseRecordEntity>,
        healthProfile: HealthProfileEntity? = null
    ): File {
        val document = PdfDocument()

        val titlePaint = Paint().apply { textSize = 20f; isFakeBoldText = true; color = Color.BLACK }
        val sectionPaint = Paint().apply { textSize = 13f; isFakeBoldText = true; color = Color.BLACK }
        val textPaint = Paint().apply { textSize = 10f; color = Color.BLACK }
        val smallPaint = Paint().apply { textSize = 9f; color = Color.DKGRAY }

        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        var canvas = page.canvas
        var y = 60f

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

        canvas.drawText(context.getString(R.string.pdf_history), MARGIN, y, sectionPaint); y += 18f
        canvas.drawText(context.getString(R.string.pdf_table_header), MARGIN, y, smallPaint); y += 14f

        val zone = ZoneId.systemDefault()
        doses.sortedBy { it.scheduledDateTime }.forEach { dose ->
            if (y > BOTTOM) {
                document.finishPage(page)
                pageNumber++
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                y = 60f
            }
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
}
