package com.example.aprobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.HealthProfileEntity

@Composable
fun AddEditHealthProfileDialog(
    profile: HealthProfileEntity?,
    onDismiss: () -> Unit,
    onSave: (
        bloodType: String?,
        allergies: String?,
        conditions: String?,
        weight: String?,
        height: String?,
        emergencyContact: String?,
        notes: String?
    ) -> Unit
) {
    var bloodType by remember { mutableStateOf(profile?.bloodType ?: "") }
    var allergies by remember { mutableStateOf(profile?.allergies ?: "") }
    var conditions by remember { mutableStateOf(profile?.conditions ?: "") }
    var weight by remember { mutableStateOf(profile?.weight ?: "") }
    var height by remember { mutableStateOf(profile?.height ?: "") }
    var emergencyContact by remember { mutableStateOf(profile?.emergencyContact ?: "") }
    var notes by remember { mutableStateOf(profile?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.health_dialog_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = bloodType,
                    onValueChange = { bloodType = it },
                    label = { Text(stringResource(R.string.health_blood_type)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text(stringResource(R.string.health_allergies)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = conditions,
                    onValueChange = { conditions = it },
                    label = { Text(stringResource(R.string.health_conditions)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.health_weight)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text(stringResource(R.string.health_height)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text(stringResource(R.string.health_emergency)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.health_notes)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        bloodType.trim().ifBlank { null },
                        allergies.trim().ifBlank { null },
                        conditions.trim().ifBlank { null },
                        weight.trim().ifBlank { null },
                        height.trim().ifBlank { null },
                        emergencyContact.trim().ifBlank { null },
                        notes.trim().ifBlank { null }
                    )
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
