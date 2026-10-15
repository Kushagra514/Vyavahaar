package org.vyavahaar.app.data.repository

import kotlinx.coroutines.flow.firstOrNull
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.TransportDto
import org.vyavahaar.app.data.remote.TransportRequestCreateDto
import org.vyavahaar.app.data.remote.VyavahaarApiService

class TransportRepository(
    private val api: VyavahaarApiService,
    private val preferences: PreferencesDataStore
) {
    suspend fun requestCab(pickup: String, destination: String): Result<TransportDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.createTransportRequest(
                "Bearer $token",
                TransportRequestCreateDto(
                    requestType = "CAB",
                    pickupAddress = pickup,
                    destinationAddress = destination
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to request cab"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestAmbulance(pickup: String, hospital: String): Result<TransportDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.createTransportRequest(
                "Bearer $token",
                TransportRequestCreateDto(
                    requestType = "AMBULANCE",
                    pickupAddress = pickup,
                    destinationAddress = hospital
                )
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to dispatch ambulance"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun confirmBooking(transportId: Int): Result<TransportDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.confirmTransportBooking("Bearer $token", transportId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to confirm ride"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelBooking(transportId: Int): Result<TransportDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.cancelTransportBooking("Bearer $token", transportId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to cancel ride"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
