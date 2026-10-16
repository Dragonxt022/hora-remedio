package com.example.aprobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.example.aprobile.notifications.AlarmReceiver
import com.example.aprobile.ui.MedicationScreen

class MainActivity : FragmentActivity() {

    private val deepLinkMedicationId = mutableStateOf<Long?>(null)
    private val openAddMedication = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            MedicationScreen(
                initialMedicationId = deepLinkMedicationId.value,
                openAddMedication = openAddMedication.value
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val id = intent?.getLongExtra(AlarmReceiver.EXTRA_MEDICATION_ID, -1L) ?: -1L
        if (id > 0) {
            deepLinkMedicationId.value = id
        }
        if (intent?.getStringExtra("shortcut_action") == "add_medication") {
            openAddMedication.value = true
        }
    }
}
