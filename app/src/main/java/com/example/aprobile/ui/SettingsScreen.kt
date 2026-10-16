package com.example.aprobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aprobile.R

@Composable
fun SettingsScreen(
    snoozeMinutes: Int,
    appLockEnabled: Boolean,
    onSnoozeChange: (Int) -> Unit,
    onAppLockChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCard {
            SettingsSectionTitle(stringResource(R.string.snooze_setting))
            Text(
                text = stringResource(R.string.snooze_setting_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(5, 10, 15, 30).forEach { minutes ->
                    FilterChip(
                        selected = snoozeMinutes == minutes,
                        onClick = { onSnoozeChange(minutes) },
                        label = {
                            Text(
                                text = stringResource(R.string.snooze_minutes, minutes),
                                maxLines = 1
                            )
                        }
                    )
                }
            }
        }

        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.app_lock_setting),
                subtitle = stringResource(R.string.app_lock_setting_desc),
                checked = appLockEnabled,
                onCheckedChange = onAppLockChange
            )
        }
    }
}
