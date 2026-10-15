package com.example.aprobile.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.aprobile.data.DoseRecordEntity
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
        doses: List<DoseRecordEntity>
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

        canvas.drawText("Hora do Remédio - Relatório", MARGIN, y, titlePaint); y += 22f
        canvas.drawText(
            "Gerado em " + LocalDateTime.now().format(dateTimeFormatter),
            MARGIN, y, smallPaint
        ); y += 26f

        val medMap = medications.associateBy { it.id }
        val taken = doses.count { it.status == DoseRecordEntity.STATUS_TAKEN }
        val total = doses.size

        canvas.drawText("Resumo", MARGIN, y, sectionPaint); y += 18f
        canvas.drawText("Medicamentos cadastrados: ${medications.size}", MARGIN, y, textPaint); y += 14f
        canvas.drawText("Doses registradas: $total", MARGIN, y, textPaint); y += 14f
        canvas.drawText("Doses tomadas: $taken", MARGIN, y, textPaint); y += 14f
        canvas.drawText("Doses não tomadas: ${total - taken}", MARGIN, y, textPaint); y += 24f

        canvas.drawText("Histórico de Doses", MARGIN, y, sectionPaint); y += 18f
        canvas.drawText("Data/Hora            Medicamento                 Perfil       Status", MARGIN, y, smallPaint); y += 14f

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
                append(if (dose.status == DoseRecordEntity.STATUS_TAKEN) "Tomada" else "Não tomada")
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
