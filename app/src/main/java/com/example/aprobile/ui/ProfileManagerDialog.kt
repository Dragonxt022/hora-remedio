package com.example.aprobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aprobile.R

@Composable
fun ProfileManagerDialog(
    profiles: List<String>,
    inUseProfiles: Set<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var newProfile by remember { mutableStateOf("") }
    var renamingName by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = { Text(stringResource(R.string.manage_profiles), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_manage_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                profiles.forEach { profile ->
                    if (renamingName == profile) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = renameText,
                                onValueChange = { renameText = it },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                onRename(profile, renameText)
                                renamingName = null
                            }) {
                                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save))
                            }
                            IconButton(onClick = { renamingName = null }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = profile,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (profile in inUseProfiles) {
                                Text(
                                    text = stringResource(R.string.profile_in_use),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                IconButton(onClick = { onDelete(profile) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.action_delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            IconButton(onClick = {
                                renameText = profile
                                renamingName = profile
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_rename))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newProfile,
                        onValueChange = { newProfile = it },
                        placeholder = { Text(stringResource(R.string.profile_new_placeholder)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAdd(newProfile)
                            newProfile = ""
                        },
                        enabled = newProfile.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.action_add))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
