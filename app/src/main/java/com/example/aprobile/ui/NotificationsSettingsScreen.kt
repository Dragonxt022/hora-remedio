package com.example.aprobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aprobile.BuildConfig
import com.example.aprobile.R

@Composable
fun NotificationsSettingsScreen(
    dailyCheckInEnabled: Boolean,
    onDailyCheckInChange: (Boolean) -> Unit,
    onTestCheckIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCard {
            SettingsToggleRow(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.checkin_setting),
                subtitle = stringResource(R.string.checkin_setting_desc),
                checked = dailyCheckInEnabled,
                onCheckedChange = onDailyCheckInChange
            )
        }

        if (BuildConfig.DEBUG) {
            SettingsCard {
                SettingsNavRow(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.dev_test_checkin),
                    onClick = onTestCheckIn
                )
            }
        }
    }
}
