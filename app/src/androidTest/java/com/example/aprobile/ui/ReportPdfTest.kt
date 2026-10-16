package com.example.aprobile.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.aprobile.data.DoseRecordEntity
import com.example.aprobile.data.HealthEventEntity
import com.example.aprobile.data.MedicationEntity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportPdfTest {

    @Test
    fun generatesPdfWithChartsAndHistory() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val meds = listOf(
            MedicationEntity(
                id = 1,
                name = "Paracetamol",
                dosage = "500 mg",
                times = listOf("08:00", "20:00"),
                currentQuantity = 4,
                lowStockAlert = 5
            )
        )
        val now = System.currentTimeMillis()
        val doses = listOf(
            DoseRecordEntity(medicationId = 1, scheduledDateTime = now - 3600_000, status = DoseRecordEntity.STATUS_TAKEN, takenAt = now),
            DoseRecordEntity(medicationId = 1, scheduledDateTime = now, status = DoseRecordEntity.STATUS_PENDING)
        )
        val events = listOf(
            HealthEventEntity(profile = "Eu", timestamp = now, type = HealthEventEntity.TYPE_FEVER, title = "Febre", value = "38.5"),
            HealthEventEntity(profile = "Eu", timestamp = now, type = HealthEventEntity.TYPE_SYMPTOM, title = "Dor de cabeça")
        )

        val file = ReportPdf.generate(context, meds, doses, null, events)

        assertTrue(file.exists())
        assertTrue(file.length() > 0)
    }
}
