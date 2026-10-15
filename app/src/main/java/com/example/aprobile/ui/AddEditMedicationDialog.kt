package com.example.aprobile.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aprobile.data.MedicationEntity
import com.example.aprobile.domain.MedicationCatalog
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDisplayTime(timeStr: String): String {
    return try {
        val time = LocalTime.parse(timeStr)
        val hour = time.hour
        val period = when {
            hour < 12 -> "Manhã"
            hour < 18 -> "Tarde"
            else -> "Noite"
        }
        val formatted12 = time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale("pt", "BR")))
        "$formatted12 ($period)"
    } catch (e: Exception) {
        timeStr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationDialog(
    medicationToEdit: MedicationEntity?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dosage: String,
        instructions: String?,
        times: List<String>,
        currentQuantity: Int?,
        lowStockAlert: Int?,
        profile: String,
        colorTag: String,
        soundEnabled: Boolean,
        vibrateEnabled: Boolean
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(medicationToEdit?.name ?: "") }
    var dosage by remember { mutableStateOf(medicationToEdit?.dosage ?: "") }
    var instructions by remember { mutableStateOf(medicationToEdit?.instructions ?: "") }
    var currentQuantityStr by remember { mutableStateOf(medicationToEdit?.currentQuantity?.toString() ?: "") }
    var lowStockAlertStr by remember { mutableStateOf(medicationToEdit?.lowStockAlert?.toString() ?: "") }
    var profile by remember { mutableStateOf(medicationToEdit?.profile ?: "Eu") }
    var colorTag by remember { mutableStateOf(medicationToEdit?.colorTag ?: "Azul") }
    var soundEnabled by remember { mutableStateOf(medicationToEdit?.soundEnabled ?: true) }
    var vibrateEnabled by remember { mutableStateOf(medicationToEdit?.vibrateEnabled ?: true) }

    val profileOptions = listOf("Eu", "Mãe", "Pai", "Pet", "Outro")
    val colorOptions = listOf("Azul", "Verde", "Vermelho", "Roxo", "Laranja")

    var expandedProfile by remember { mutableStateOf(false) }
    var expandedColor by remember { mutableStateOf(false) }

    val times = remember {
        mutableStateListOf<String>().apply {
            if (medicationToEdit != null && medicationToEdit.times.isNotEmpty()) {
                addAll(medicationToEdit.times)
            } else {
                add("08:00")
            }
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun openTimePicker(currentIndex: Int) {
        val initialTime = try {
            LocalTime.parse(times[currentIndex])
        } catch (e: Exception) {
            LocalTime.of(8, 0)
        }

        // is24HourView = false to clearly show AM/PM (Manhã / Tarde)
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedTime = LocalTime.of(hourOfDay, minute)
                val formatted = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                times[currentIndex] = formatted
            },
            initialTime.hour,
            initialTime.minute,
            false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (medicationToEdit == null) "Novo Medicamento" else "Editar Medicamento",
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
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nome do Medicamento *") },
                    placeholder = { Text("Ex: Paracetamol") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                val suggestions = MedicationCatalog.suggest(name).filter { !it.equals(name, ignoreCase = true) }
                if (suggestions.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        suggestions.forEach { suggestion ->
                            Text(
                                text = suggestion,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { name = suggestion }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosagem (opcional)") },
                    placeholder = { Text("Ex: 500 mg ou 1 comprimido") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedProfile,
                    onExpandedChange = { expandedProfile = !expandedProfile },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = profile,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Perfil / Responsável") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProfile) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedProfile,
                        onDismissRequest = { expandedProfile = false }
                    ) {
                        profileOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    profile = option
                                    expandedProfile = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedColor,
                    onExpandedChange = { expandedColor = !expandedColor },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val colVal = getColorTag(colorTag)
                    OutlinedTextField(
                        value = colorTag,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cor de Destaque") },
                        leadingIcon = {
                            Box(modifier = Modifier.width(16.dp).height(16.dp).clip(CircleShape).background(colVal))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColor) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedColor,
                        onDismissRequest = { expandedColor = false }
                    ) {
                        colorOptions.forEach { col ->
                            val cVal = getColorTag(col)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.width(14.dp).height(14.dp).clip(CircleShape).background(cVal))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(col)
                                    }
                                },
                                onClick = {
                                    colorTag = col
                                    expandedColor = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = currentQuantityStr,
                        onValueChange = { currentQuantityStr = it.filter { char -> char.isDigit() } },
                        label = { Text("Estoque Atual") },
                        placeholder = { Text("Ex: 20") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = lowStockAlertStr,
                        onValueChange = { lowStockAlertStr = it.filter { char -> char.isDigit() } },
                        label = { Text("Aviso Baixo") },
                        placeholder = { Text("Ex: 5") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instruções de uso (opcional)") },
                    placeholder = { Text("Ex: Tomar após o almoço") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Som no lembrete", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Tocar som ao notificar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = soundEnabled, onCheckedChange = { soundEnabled = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Vibração no lembrete", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Vibrar ao notificar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = vibrateEnabled, onCheckedChange = { vibrateEnabled = it })
                }

                Text(
                    text = "Horários do Lembrete *",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                times.forEachIndexed { index, time ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Button(
                            onClick = { openTimePicker(index) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = formatDisplayTime(time), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }

                        if (times.size > 1) {
                            IconButton(onClick = { times.removeAt(index) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remover horário",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = {
                        times.add("12:00")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar outro horário")
                }

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
                    if (name.isBlank()) {
                        errorMessage = "Por favor, informe o nome do medicamento."
                        return@Button
                    }
                    if (times.isEmpty()) {
                        errorMessage = "Adicione ao menos um horário."
                        return@Button
                    }
                    val qty = currentQuantityStr.toIntOrNull()
                    val alert = lowStockAlertStr.toIntOrNull()
                    onSave(name.trim(), dosage.trim(), instructions.trim(), times.toList(), qty, alert, profile, colorTag, soundEnabled, vibrateEnabled)
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
