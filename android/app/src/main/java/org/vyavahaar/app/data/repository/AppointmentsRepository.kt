package org.vyavahaar.app.data.repository

import kotlinx.coroutines.flow.firstOrNull
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.AppointmentCreateDto
import org.vyavahaar.app.data.remote.AppointmentDto
import org.vyavahaar.app.data.remote.VyavahaarApiService

class AppointmentsRepository(
    private val api: VyavahaarApiService,
    private val preferences: PreferencesDataStore
) {
    suspend fun getAppointments(): Result<List<AppointmentDto>> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.getAppointments("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to load appointments"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAppointment(
        doctorName: String,
        specialty: String,
        clinic: String,
        time: String,
        recurrence: String
    ): Result<AppointmentDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.createAppointment(
                "Bearer $token",
                AppointmentCreateDto(
                    doctorName = doctorName,
                    specialty = specialty,
                    clinicOrHospital = clinic,
                    appointmentTime = time,
                    recurrenceRule = recurrence
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to book appointment"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
