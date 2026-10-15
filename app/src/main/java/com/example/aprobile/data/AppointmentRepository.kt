package com.example.aprobile.data

import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val dao: AppointmentDao) {

    val allAppointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments()

    suspend fun insert(appointment: AppointmentEntity): Long = dao.insertAppointment(appointment)

    suspend fun update(appointment: AppointmentEntity): Int = dao.updateAppointment(appointment)

    suspend fun delete(appointment: AppointmentEntity): Int = dao.deleteAppointment(appointment)
}
