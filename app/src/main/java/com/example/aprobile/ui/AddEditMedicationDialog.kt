package com.example.aprobile.ui

import android.app.Activity
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.MedicationEntity
import com.example.aprobile.domain.AllergyMatcher
import com.example.aprobile.domain.MedicationCatalog
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
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
        val formatted12 = time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.forLanguageTag("pt-BR")))
        "$formatted12 ($period)"
    } catch (e: Exception) {
        timeStr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMedicationDialog(
    medicationToEdit: MedicationEntity?,
    allergies: String? = null,
    profileOptions: List<String> = listOf("Eu"),
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
        vibrateEnabled: Boolean,
        scheduleType: String,
        daysOfWeek: String,
        barcode: String?
    ) -> Unit
) {
    val context = LocalContext.current
    val errMedName = stringResource(R.string.err_med_name)
    val errMedTimes = stringResource(R.string.err_med_times)
    val errMedDays = stringResource(R.string.err_med_days)
    val errMedQty = stringResource(R.string.err_med_qty)
    val errMedScan = stringResource(R.string.err_med_scan)

    var name by remember { mutableStateOf(medicationToEdit?.name ?: "") }
    var dosage by remember { mutableStateOf(medicationToEdit?.dosage ?: "") }
    var instructions by remember { mutableStateOf(medicationToEdit?.instructions ?: "") }
    var currentQuantityStr by remember { mutableStateOf(medicationToEdit?.currentQuantity?.toString() ?: "") }
    var lowStockAlertStr by remember { mutableStateOf(medicationToEdit?.lowStockAlert?.toString() ?: "") }
    var profile by remember { mutableStateOf(medicationToEdit?.profile ?: profileOptions.firstOrNull() ?: "Eu") }
    var colorTag by remember { mutableStateOf(medicationToEdit?.colorTag ?: "Azul") }
    var soundEnabled by remember { mutableStateOf(medicationToEdit?.soundEnabled ?: true) }
    var vibrateEnabled by remember { mutableStateOf(medicationToEdit?.vibrateEnabled ?: true) }
    var trackStock by remember { mutableStateOf(medicationToEdit?.currentQuantity != null) }
    var barcode by remember { mutableStateOf(medicationToEdit?.barcode) }
    var scheduleType by remember { mutableStateOf(medicationToEdit?.scheduleType ?: MedicationEntity.SCHEDULE_DAILY) }
    val daysSelected = remember {
        mutableStateListOf<Int>().apply {
            medicationToEdit?.daysOfWeek
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?.let { addAll(it) }
        }
    }

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

    fun startBarcodeScan() {
        val activity = context as? Activity
        if (activity == null) {
            errorMessage = errMedScan
            return
        }
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128
            )
            .build()
        GmsBarcodeScanning.getClient(activity, options).startScan()
            .addOnSuccessListener { scanned ->
                val value = scanned.rawValue
                if (!value.isNullOrBlank()) {
                    barcode = value
                    errorMessage = null
                }
            }
            .addOnFailureListener { errorMessage = errMedScan }
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
                    text = stringResource(if (medicationToEdit == null) R.string.med_dialog_new else R.string.med_dialog_edit),
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
                    label = { Text(stringResource(R.string.med_name_label)) },
                    placeholder = { Text(stringResource(R.string.med_name_placeholder)) },
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

                val allergyMatch = AllergyMatcher.firstMatch(name, allergies)
                if (allergyMatch != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.width(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.med_allergy_warning, allergyMatch),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text(stringResource(R.string.med_dosage_label)) },
                    placeholder = { Text(stringResource(R.string.med_dosage_placeholder)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = barcode ?: "",
                        onValueChange = { barcode = it.ifBlank { null } },
                        label = { Text(stringResource(R.string.barcode_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { startBarcodeScan() }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = stringResource(R.string.barcode_scan_cd),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.barcode_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        label = { Text(stringResource(R.string.med_profile_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProfile) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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
                        label = { Text(stringResource(R.string.med_color_label)) },
                        leadingIcon = {
                            Box(modifier = Modifier.width(16.dp).height(16.dp).clip(CircleShape).background(colVal))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedColor) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.stock_track_title), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.stock_track_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = trackStock, onCheckedChange = { trackStock = it })
                }

                if (trackStock) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = currentQuantityStr,
                            onValueChange = { currentQuantityStr = it.filter { char -> char.isDigit() } },
                            label = { Text(stringResource(R.string.med_stock_label)) },
                            placeholder = { Text(stringResource(R.string.med_stock_placeholder)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = lowStockAlertStr,
                            onValueChange = { lowStockAlertStr = it.filter { char -> char.isDigit() } },
                            label = { Text(stringResource(R.string.med_low_label)) },
                            placeholder = { Text(stringResource(R.string.med_low_placeholder)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text(stringResource(R.string.med_instructions_label)) },
                    placeholder = { Text(stringResource(R.string.med_instructions_placeholder)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.med_sound), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.med_sound_desc),
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
                        Text(text = stringResource(R.string.med_vibrate), style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = stringResource(R.string.med_vibrate_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = vibrateEnabled, onCheckedChange = { vibrateEnabled = it })
                }

                Text(
                    text = stringResource(R.string.med_schedule_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        MedicationEntity.SCHEDULE_DAILY to R.string.schedule_daily,
                        MedicationEntity.SCHEDULE_WEEKLY_DAYS to R.string.schedule_weekly,
                        MedicationEntity.SCHEDULE_AS_NEED to R.string.schedule_as_needed
                    ).forEach { (value, labelRes) ->
                        FilterChip(
                            selected = scheduleType == value,
                            onClick = { scheduleType = value },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }

                if (scheduleType == MedicationEntity.SCHEDULE_WEEKLY_DAYS) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            1 to R.string.weekday_mon, 2 to R.string.weekday_tue, 3 to R.string.weekday_wed,
                            4 to R.string.weekday_thu, 5 to R.string.weekday_fri, 6 to R.string.weekday_sat,
                            7 to R.string.weekday_sun
                        ).forEach { (value, labelRes) ->
                            FilterChip(
                                selected = daysSelected.contains(value),
                                onClick = {
                                    if (daysSelected.contains(value)) daysSelected.remove(value) else daysSelected.add(value)
                                },
                                label = { Text(stringResource(labelRes)) }
                            )
                        }
                    }
                }

                if (scheduleType == MedicationEntity.SCHEDULE_AS_NEED) {
                    Text(
                        text = stringResource(R.string.schedule_as_needed_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = stringResource(R.string.med_times_title),
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
                                        contentDescription = stringResource(R.string.cd_remove_time),
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
                        Text(stringResource(R.string.med_add_time))
                    }
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
                        errorMessage = errMedName
                        return@Button
                    }
                    if (scheduleType != MedicationEntity.SCHEDULE_AS_NEED && times.isEmpty()) {
                        errorMessage = errMedTimes
                        return@Button
                    }
                    if (scheduleType == MedicationEntity.SCHEDULE_WEEKLY_DAYS && daysSelected.isEmpty()) {
                        errorMessage = errMedDays
                        return@Button
                    }
                    if (trackStock && currentQuantityStr.isBlank()) {
                        errorMessage = errMedQty
                        return@Button
                    }
                    val qty = if (trackStock) currentQuantityStr.toIntOrNull() else null
                    val alert = if (trackStock) lowStockAlertStr.toIntOrNull() else null
                    val days = daysSelected.sorted().joinToString(",")
                    onSave(name.trim(), dosage.trim(), instructions.trim(), times.toList(), qty, alert, profile, colorTag, soundEnabled, vibrateEnabled, scheduleType, days, barcode?.trim()?.ifBlank { null })
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
