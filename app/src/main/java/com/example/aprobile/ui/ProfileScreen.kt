package com.example.aprobile.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.aprobile.BuildConfig
import com.example.aprobile.R
import java.io.File

@Composable
fun ProfileScreen(
    mainProfileName: String,
    mainProfilePhoto: String?,
    userEmail: String,
    isLoggedIn: Boolean,
    isPremium: Boolean,
    isDarkMode: Boolean,
    onRenameMainProfile: (String) -> Unit,
    onSetProfilePhoto: (String?) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onManageProfiles: () -> Unit,
    onSeedDemo: () -> Unit,
    onTestCheckIn: () -> Unit,
    onUpgrade: () -> Unit,
    onLoginGoogle: () -> Unit,
    onLogout: () -> Unit
) {
    var editableName by remember(mainProfileName) { mutableStateOf(mainProfileName) }
    val context = LocalContext.current
    val versionInfo = remember {
        try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val name = info.versionName ?: "1.0"
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION") info.versionCode.toLong()
            }
            name to code
        } catch (e: Exception) {
            "1.0" to 0L
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val dir = File(context.filesDir, "photos").apply { mkdirs() }
                val file = File(dir, "profile_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                onSetProfilePhoto(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable { photoLauncher.launch("image/*") }
        ) {
            if (mainProfilePhoto != null) {
                AsyncImage(
                    model = File(mainProfilePhoto),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = if (editableName.isNotBlank()) editableName.take(1).uppercase() else "U",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            text = stringResource(R.string.profile_change_photo),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = editableName,
            onValueChange = { editableName = it },
            label = { Text(stringResource(R.string.profile_name)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardActions = KeyboardActions(onDone = { onRenameMainProfile(editableName) }),
            modifier = Modifier.fillMaxWidth()
        )

        if (editableName.trim().isNotEmpty() && editableName.trim() != mainProfileName) {
            Button(
                onClick = { onRenameMainProfile(editableName) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_save_name))
            }
        }

        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        SettingsCard {
            Text(
                text = stringResource(R.string.account_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
            )

            SettingsNavRow(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.notifications_row),
                onClick = onOpenNotifications
            )
            SettingsDivider()
            SettingsNavRow(
                icon = Icons.Default.Group,
                title = stringResource(R.string.manage_profiles),
                onClick = onManageProfiles
            )
            SettingsDivider()
            SettingsToggleRow(
                icon = Icons.Default.DarkMode,
                title = stringResource(R.string.dark_mode_row),
                checked = isDarkMode,
                onCheckedChange = onDarkModeChange
            )
            SettingsDivider()
            SettingsNavRow(
                icon = Icons.Default.Settings,
                title = stringResource(R.string.settings_row),
                onClick = onOpenSettings
            )
            SettingsDivider()
            SettingsNavRow(
                icon = Icons.Default.Security,
                title = stringResource(R.string.privacy_open),
                onClick = onOpenPrivacy
            )
            if (!isPremium) {
                SettingsDivider()
                SettingsNavRow(
                    icon = Icons.Default.WorkspacePremium,
                    title = stringResource(R.string.premium_go),
                    onClick = onUpgrade
                )
            }
            SettingsDivider()
            if (isLoggedIn) {
                SettingsNavRow(
                    icon = Icons.Default.Logout,
                    title = stringResource(R.string.profile_logout),
                    onClick = onLogout
                )
            } else {
                SettingsNavRow(
                    icon = Icons.Default.Login,
                    title = stringResource(R.string.profile_google_login),
                    onClick = onLoginGoogle
                )
            }
        }

        if (BuildConfig.DEBUG) {
            SettingsCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dev_tools),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.dev_seed_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onSeedDemo, shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.dev_seed_year))
                    }
                    Button(onClick = onTestCheckIn, shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.dev_test_checkin))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.profile_version, versionInfo.first, versionInfo.second),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
