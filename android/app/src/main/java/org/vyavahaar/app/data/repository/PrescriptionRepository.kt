package org.vyavahaar.app.data.repository

import kotlinx.coroutines.flow.firstOrNull
import org.vyavahaar.app.data.local.PreferencesDataStore
import org.vyavahaar.app.data.remote.*

class PrescriptionRepository(
    private val api: VyavahaarApiService,
    private val preferences: PreferencesDataStore
) {
    suspend fun scanPrescription(imageBase64: String): Result<PrescriptionResponseDto> {
        return try {
            val token = preferences.authToken.firstOrNull() ?: ""
            val response = api.scanPrescription("Bearer $token", PrescriptionScanDto(imageBase64))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to scan prescription"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchMedicine(query: String): Result<MedicineSearchDto> {
        return try {
            val response = api.searchMedicine(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Medicine not found in catalog"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
