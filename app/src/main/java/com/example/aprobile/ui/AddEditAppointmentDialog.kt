package com.example.aprobile.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprobile.data.AppointmentEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun AddEditAppointmentDialog(
    appointmentToEdit: AppointmentEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, doctor: String?, location: String?, dateTime: Long, notes: String?) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(appointmentToEdit?.title ?: "") }
    var doctor by remember { mutableStateOf(appointmentToEdit?.doctor ?: "") }
    var location by remember { mutableStateOf(appointmentToEdit?.location ?: "") }
    var notes by remember { mutableStateOf(appointmentToEdit?.notes ?: "") }
    var dateTime by remember { mutableLongStateOf(appointmentToEdit?.dateTime ?: System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun openDatePicker() {
        val current = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.systemDefault())
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val updated = current.withYear(year).withMonth(month + 1).withDayOfMonth(day)
                dateTime = updated.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    fun openTimePicker() {
        val current = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.systemDefault())
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val updated = current.withHour(hour).withMinute(minute)
                dateTime = updated.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            },
            current.hour,
            current.minute,
            true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (appointmentToEdit == null) "Nova Consulta" else "Editar Consulta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = { Text("Título *") },
                    placeholder = { Text("Ex: Cardiologista") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { openDatePicker() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Data")
                    }
                    Button(
                        onClick = { openTimePicker() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Hora")
                    }
                }

                Text(
                    text = formatDetailDateTime(dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = doctor,
                    onValueChange = { doctor = it },
                    label = { Text("Médico (opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Local (opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações (opcional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Informe um título."
                        return@Button
                    }
                    onSave(title.trim(), doctor.trim(), location.trim(), dateTime, notes.trim())
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
