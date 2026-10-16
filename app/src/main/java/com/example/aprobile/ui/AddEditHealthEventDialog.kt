package com.example.aprobile.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.HealthEventEntity
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private val eventTypeOptions: List<Pair<String, Int>> = listOf(
    HealthEventEntity.TYPE_SYMPTOM to R.string.health_type_symptom,
    HealthEventEntity.TYPE_FEVER to R.string.health_type_fever,
    HealthEventEntity.TYPE_PRESSURE to R.string.health_type_pressure,
    HealthEventEntity.TYPE_NOTE to R.string.health_type_note,
    "OTHER" to R.string.health_type_other
)

@Composable
fun AddEditHealthEventDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, title: String, value: String?, notes: String?, timestamp: Long, photoUri: String?) -> Unit
) {
    val context = LocalContext.current
    val errTitle = stringResource(R.string.err_health_title)

    var type by remember { mutableStateOf(HealthEventEntity.TYPE_SYMPTOM) }
    var title by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val dir = File(context.filesDir, "photos").apply { mkdirs() }
                val file = File(dir, "evt_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                photoPath = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun openDatePicker() {
        val current = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val updated = current.withYear(year).withMonth(month + 1).withDayOfMonth(day)
                timestamp = updated.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    fun openTimePicker() {
        val current = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        TimePickerDialog(
            context,
            { _, hour, minute ->
                val updated = current.withHour(hour).withMinute(minute)
                timestamp = updated.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            },
            current.hour,
            current.minute,
            true
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.health_add_event), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.health_event_type), style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    eventTypeOptions.forEach { (key, labelRes) ->
                        FilterChip(
                            selected = type == key,
                            onClick = { type = key },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = { Text(stringResource(R.string.health_event_title)) },
                    placeholder = { Text(stringResource(R.string.health_event_title_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(R.string.health_event_value)) },
                    placeholder = { Text(stringResource(R.string.health_event_value_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { openDatePicker() }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.health_event_date))
                    }
                    Button(onClick = { openTimePicker() }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.health_event_time))
                    }
                }

                Text(
                    text = formatDetailDateTime(timestamp),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.health_event_notes)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { photoLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            stringResource(
                                if (photoPath == null) R.string.health_event_photo_add
                                else R.string.health_event_photo_change
                            )
                        )
                    }
                    if (photoPath != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { photoPath = null }) {
                            Text(stringResource(R.string.health_event_photo_remove))
                        }
                    }
                }

                photoPath?.let { path ->
                    AsyncImage(
                        model = File(path),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = errTitle
                        return@Button
                    }
                    onSave(type, title.trim(), value.trim().ifBlank { null }, notes.trim().ifBlank { null }, timestamp, photoPath)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
