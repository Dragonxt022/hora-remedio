package com.example.aprobile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.MedicationEntity

@Composable
fun MedicationsScreen(
    uiState: MedicationUiState,
    onQueryChange: (String) -> Unit,
    onSelectProfile: (String) -> Unit,
    onAddClick: () -> Unit,
    onToggleEnabled: (MedicationEntity) -> Unit,
    onEdit: (MedicationEntity) -> Unit,
    onDelete: (MedicationEntity) -> Unit,
    onOpenDetail: (MedicationEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchAndFilterBar(
            query = uiState.searchQuery,
            onQueryChange = onQueryChange,
            profiles = uiState.availableProfiles,
            selectedProfile = uiState.selectedProfile,
            onSelectProfile = onSelectProfile,
            isDarkMode = uiState.isDarkMode
        )

        if (uiState.isLoading && uiState.medications.isEmpty()) {
            LoadingStateView()
        } else if (uiState.medications.isEmpty()) {
            EmptyStateView(
                message = stringResource(R.string.empty_medications),
                onAddClick = onAddClick,
                isDarkMode = uiState.isDarkMode
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.medications, key = { it.id }) { medication ->
                    MedicationMasterCard(
                        medication = medication,
                        onClick = { onOpenDetail(medication) },
                        onToggleEnabled = { onToggleEnabled(medication) },
                        onEdit = { onEdit(medication) },
                        onDelete = { onDelete(medication) },
                        isDarkMode = uiState.isDarkMode
                    )
                }
            }
        }
    }
}
