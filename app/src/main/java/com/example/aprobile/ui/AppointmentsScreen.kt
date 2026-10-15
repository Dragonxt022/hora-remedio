package com.example.aprobile.ui

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aprobile.R
import com.example.aprobile.data.AppointmentEntity

fun addAppointmentToCalendar(context: Context, appointment: AppointmentEntity) {
    val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).apply {
        putExtra(CalendarContract.Events.TITLE, appointment.title)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, appointment.dateTime)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, appointment.dateTime + 3_600_000L)
        appointment.location?.let { putExtra(CalendarContract.Events.EVENT_LOCATION, it) }
        appointment.notes?.let { putExtra(CalendarContract.Events.DESCRIPTION, it) }
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun AppointmentsScreen(
    appointments: List<AppointmentEntity>,
    isDarkMode: Boolean,
    onEdit: (AppointmentEntity) -> Unit,
    onDelete: (AppointmentEntity) -> Unit,
    onAddToCalendar: (AppointmentEntity) -> Unit
) {
    if (appointments.isEmpty()) {
        EmptyAppointmentsView(isDarkMode = isDarkMode)
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(appointments, key = { it.id }) { appointment ->
            AppointmentCard(
                appointment = appointment,
                isDarkMode = isDarkMode,
                onEdit = { onEdit(appointment) },
                onDelete = { onDelete(appointment) },
                onAddToCalendar = { onAddToCalendar(appointment) }
            )
        }
    }
}

@Composable
private fun AppointmentCard(
    appointment: AppointmentEntity,
    isDarkMode: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToCalendar: () -> Unit
) {
    val glassColor = if (isDarkMode) Color(0xFF16221F).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDetailDateTime(appointment.dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                appointment.doctor?.takeIf { it.isNotBlank() }?.let {
                    Text(text = "Médico: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                appointment.location?.takeIf { it.isNotBlank() }?.let {
                    Text(text = "Local: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                appointment.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            IconButton(onClick = onAddToCalendar) {
                Icon(Icons.Default.Event, contentDescription = "Adicionar ao calendário")
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun EmptyAppointmentsView(isDarkMode: Boolean) {
    val glassColor = if (isDarkMode) Color(0xFF16221F).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)
    val borderColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.2.dp, borderColor), shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = glassColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.empty_appointments),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
