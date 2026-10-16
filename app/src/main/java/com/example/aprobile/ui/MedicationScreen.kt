package com.example.aprobile.ui

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aprobile.R
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aprobile.ui.theme.AprobileTheme

private const val DETAIL_ROUTE = "medication/{medicationId}"

private fun detailRoute(medicationId: Long) = "medication/$medicationId"

enum class AppDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    @param:StringRes val titleRes: Int,
    val icon: ImageVector,
    val showFab: Boolean
) {
    DOSES("doses", R.string.nav_doses, R.string.title_doses, Icons.Default.Today, true),
    MEDICATIONS("medications", R.string.nav_medications, R.string.title_medications, Icons.Default.Medication, true),
    APPOINTMENTS("appointments", R.string.nav_appointments, R.string.title_appointments, Icons.Default.Event, true),
    HEALTH("health", R.string.nav_health, R.string.title_health, Icons.Default.FavoriteBorder, true),
    STATISTICS("statistics", R.string.nav_statistics, R.string.title_statistics, Icons.Default.BarChart, false),
    PROFILE("profile", R.string.nav_profile, R.string.title_profile, Icons.Default.Person, false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    viewModel: MedicationViewModel = viewModel(),
    initialMedicationId: Long? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (!uiState.onboardingDone) {
        AprobileTheme(darkTheme = uiState.isDarkMode) {
            OnboardingScreen(onFinish = { viewModel.completeOnboarding() })
        }
        return
    }

    if (uiState.appLockEnabled && !uiState.isAppUnlocked) {
        AprobileTheme(darkTheme = uiState.isDarkMode) {
            LockScreen(onUnlock = { viewModel.unlockApp() })
        }
        return
    }

    if (uiState.isPrivacyOpen) {
        AprobileTheme(darkTheme = uiState.isDarkMode) {
            PrivacyScreen(
                onDismiss = { viewModel.closePrivacy() },
                onExport = { viewModel.exportBackup() },
                onImport = { viewModel.importBackup(it) },
                onDeleteAll = { viewModel.deleteAllData() }
            )
        }
        return
    }

    PermissionHandler()

    val navController = rememberNavController()

    LaunchedEffect(initialMedicationId) {
        if (initialMedicationId != null && initialMedicationId > 0) {
            navController.navigate(detailRoute(initialMedicationId))
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val currentDestination = AppDestination.entries.firstOrNull { it.route == currentRoute }
        ?: AppDestination.DOSES
    val isDetail = currentRoute?.startsWith("medication/") == true

    AprobileTheme(darkTheme = uiState.isDarkMode) {
        val bgColors = if (uiState.isDarkMode) {
            listOf(Color(0xFF0F1715), Color(0xFF1B2A26))
        } else {
            listOf(Color(0xFFE8F4F0), Color(0xFFD2EBE4))
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(bgColors))
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isDetail) Icons.Default.Medication else currentDestination.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = stringResource(if (isDetail) R.string.title_detail else currentDestination.titleRes),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            navigationIcon = {
                                if (isDetail || currentRoute == AppDestination.PROFILE.route) {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.action_back),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    navController.navigate(AppDestination.PROFILE.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = stringResource(R.string.title_profile),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                IconButton(onClick = { viewModel.toggleDarkMode() }) {
                                    Icon(
                                        imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = stringResource(R.string.action_toggle_theme),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
                        ) {
                            AppDestination.entries.filter { it != AppDestination.PROFILE }.forEach { dest ->
                                NavigationBarItem(
                                    selected = currentRoute == dest.route,
                                    onClick = {
                                        if (currentRoute != dest.route) {
                                            navController.navigate(dest.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(dest.icon, contentDescription = stringResource(dest.labelRes)) },
                                    label = { Text(stringResource(dest.labelRes)) }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentDestination.showFab && !isDetail) {
                            FloatingActionButton(
                                onClick = {
                                    when (currentDestination) {
                                        AppDestination.APPOINTMENTS -> viewModel.openAddAppointmentDialog()
                                        AppDestination.HEALTH -> viewModel.openAddHealthEventDialog()
                                        else -> viewModel.openAddMedicationDialog()
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_medication))
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = AppDestination.DOSES.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(AppDestination.DOSES.route) {
                            DosesScreen(
                                uiState = uiState,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                onSelectProfile = { viewModel.selectProfile(it) },
                                onAddClick = { viewModel.openAddMedicationDialog() },
                                onToggleDose = { viewModel.toggleDoseStatus(it) },
                                onEdit = { viewModel.openEditMedicationDialog(it) },
                                onDelete = { viewModel.requestDeleteMedication(it) },
                                onOpenDetail = { navController.navigate(detailRoute(it.id)) },
                                onOpenHealth = {
                                    navController.navigate(AppDestination.HEALTH.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        composable(AppDestination.MEDICATIONS.route) {
                            MedicationsScreen(
                                uiState = uiState,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                onSelectProfile = { viewModel.selectProfile(it) },
                                onAddClick = { viewModel.openAddMedicationDialog() },
                                onToggleEnabled = { viewModel.toggleMedicationEnabled(it) },
                                onEdit = { viewModel.openEditMedicationDialog(it) },
                                onDelete = { viewModel.requestDeleteMedication(it) },
                                onOpenDetail = { navController.navigate(detailRoute(it.id)) }
                            )
                        }

                        composable(AppDestination.APPOINTMENTS.route) {
                            AppointmentsScreen(
                                appointments = uiState.appointments,
                                isDarkMode = uiState.isDarkMode,
                                onEdit = { viewModel.openEditAppointmentDialog(it) },
                                onDelete = { viewModel.requestDeleteAppointment(it) },
                                onAddToCalendar = { addAppointmentToCalendar(context, it) }
                            )
                        }

                        composable(AppDestination.HEALTH.route) {
                            HealthScreen(
                                uiState = uiState,
                                onSelectHealthProfile = { viewModel.selectHealthProfile(it) },
                                onEditProfile = { viewModel.openEditHealthProfileDialog() },
                                onDeleteEvent = { viewModel.requestDeleteHealthEvent(it) },
                                onUpgrade = { viewModel.openPaywall() }
                            )
                        }

                        composable(AppDestination.STATISTICS.route) {
                            StatisticsScreen(
                                uiState = uiState,
                                onSelectPeriod = { viewModel.setStatsPeriod(it) },
                                onUpgrade = { viewModel.openPaywall() }
                            )
                        }

                        composable(AppDestination.PROFILE.route) {
                            ProfileScreen(
                                userName = uiState.userName,
                                userEmail = uiState.userEmail,
                                isLoggedIn = uiState.isLoggedIn,
                                snoozeMinutes = uiState.snoozeMinutes,
                                isPremium = uiState.isPremium,
                                appLockEnabled = uiState.appLockEnabled,
                                onUpdateName = { viewModel.updateUserName(it) },
                                onSnoozeChange = { viewModel.setSnoozeMinutes(it) },
                                onAppLockChange = { viewModel.setAppLockEnabled(it) },
                                onOpenPrivacy = { viewModel.openPrivacy() },
                                onUpgrade = { viewModel.openPaywall() },
                                onLoginGoogle = { viewModel.loginWithGoogle() },
                                onLogout = { viewModel.logout() }
                            )
                        }

                        composable(
                            route = DETAIL_ROUTE,
                            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
                        ) { entry ->
                            val medicationId = entry.arguments?.getLong("medicationId") ?: -1L
                            MedicationDetailScreen(
                                medicationId = medicationId,
                                allMedications = uiState.allMedications,
                                allDoseRecords = uiState.allDoseRecords,
                                isDarkMode = uiState.isDarkMode,
                                onTakeNow = { viewModel.markNextDoseAsTaken(it) },
                                onSnooze = { viewModel.snoozeNextDose(it) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isAddEditOpen) {
            AddEditMedicationDialog(
                medicationToEdit = uiState.medicationToEdit,
                allergies = uiState.activeHealthProfile?.allergies,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave = { name, dosage, instructions, times, currentQuantity, lowStockAlert, profile, colorTag, soundEnabled, vibrateEnabled ->
                    viewModel.saveMedication(name, dosage, instructions, times, currentQuantity, lowStockAlert, profile, colorTag, soundEnabled, vibrateEnabled)
                }
            )
        }

        if (uiState.isAppointmentDialogOpen) {
            AddEditAppointmentDialog(
                appointmentToEdit = uiState.appointmentToEdit,
                onDismiss = { viewModel.closeAppointmentDialog() },
                onSave = { title, doctor, location, dateTime, notes ->
                    viewModel.saveAppointment(title, doctor, location, dateTime, notes)
                }
            )
        }

        uiState.appointmentToDelete?.let { appointment ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteAppointment() },
                shape = RoundedCornerShape(24.dp),
                title = { Text(stringResource(R.string.delete_appt_title)) },
                text = { Text(stringResource(R.string.delete_appt_text, appointment.title)) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDeleteAppointment() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteAppointment() }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        if (uiState.isHealthProfileDialogOpen) {
            AddEditHealthProfileDialog(
                profile = uiState.healthProfile,
                onDismiss = { viewModel.closeHealthProfileDialog() },
                onSave = { bloodType, allergies, conditions, weight, height, emergencyContact, notes ->
                    viewModel.saveHealthProfile(bloodType, allergies, conditions, weight, height, emergencyContact, notes)
                }
            )
        }

        if (uiState.isHealthEventDialogOpen) {
            AddEditHealthEventDialog(
                onDismiss = { viewModel.closeHealthEventDialog() },
                onSave = { type, title, value, notes, timestamp ->
                    viewModel.saveHealthEvent(type, title, value, notes, timestamp)
                }
            )
        }

        uiState.healthEventToDelete?.let {
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteHealthEvent() },
                shape = RoundedCornerShape(24.dp),
                title = { Text(stringResource(R.string.health_delete_title)) },
                text = { Text(stringResource(R.string.health_delete_text)) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDeleteHealthEvent() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteHealthEvent() }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        if (uiState.isPaywallOpen) {
            val activity = context as? Activity
            PaywallScreen(
                onDismiss = { viewModel.closePaywall() },
                onSubscribe = { viewModel.purchasePremium(activity) },
                onRestore = { viewModel.restorePremium(activity) }
            )
        }

        uiState.medicationToDelete?.let { medToDelete ->
            AlertDialog(
                onDismissRequest = { viewModel.cancelDeleteMedication() },
                shape = RoundedCornerShape(24.dp),
                title = { Text(stringResource(R.string.delete_med_title)) },
                text = { Text(stringResource(R.string.delete_med_text, medToDelete.name)) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDeleteMedication() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(stringResource(R.string.action_delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelDeleteMedication() }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }
}
